import { importZip, exportZip, buildZip, LIMITS, readStreamLimited } from "./archive.js";
import { AddonError } from "./errors.js";
import {
  mergeAddons,
  toggleAddon,
  removeAddon,
  missingDependencies,
  missingOptional,
  loadOrder,
  sameVersionCopy,
} from "./library.js";
import { readLibrary, writeLibrary, withLock } from "./repository.js";
import { putAddons, deleteAddon, getAllFiles, gc } from "./db.js";
import { commitStagedGeneration } from "./staged-storage.js";
import { languages, setLocale, t, formatSize, formatDate } from "./i18n.js";
import {
  hasAccess,
  requestAccess,
  loadCatalog,
  fetchAddonZip,
  readInstalled,
  rememberInstalled,
} from "./browse.js";
const $ = (s) => document.querySelector(s),
  el = (tag, text, className) => {
    const n = document.createElement(tag);
    if (text) n.textContent = text;
    if (className) n.className = className;
    return n;
  };
let library = await readLibrary(),
  busy = false,
  messageKey = "",
  details = "",
  view = "mine",
  catalog = null,
  catalogError = "",
  installedMap = await readInstalled(),
  installing = "",
  query = "",
  category = "",
  statusFilter = "",
  selectedSources = new Map(),
  errorLog = [],
  progressText = "";
let session = await browser.storage.session.get(["lastStatus", "seen", "gameStorage"]);
try {
  // Drop stored files that no library entry points to (e.g. after a crash).
  await withLock("lh-library", async () => {
    library = await readLibrary();
    await gc(library.addons.map((a) => a.storageFolder || a.folder));
  });
} catch (error) {
  console.warn("Storage cleanup skipped", error);
}
const pref = await browser.storage.local.get("language");
await setLocale(pref.language || browser.i18n.getUILanguage());
for (const [code, name] of Object.entries(languages)) {
  const option = el("option", name);
  option.value = code;
  $("#language").append(option);
}
$("#language").value = document.documentElement.lang.replace("-", "_");
$("#version").textContent = " · v" + browser.runtime.getManifest().version;
function showMessage(key, extra = "") {
  messageKey = key;
  details = extra;
  $("#message").textContent = key
    ? t(key, typeof extra === "object" ? extra : {})
    : "";
}
async function download(addon) {
  const files = await getAllFiles(addon.storageFolder || addon.folder);
  const url = URL.createObjectURL(
    new Blob([exportZip({ folder: addon.folder, files })], {
      type: "application/zip",
    }),
  );
  const link = el("a");
  link.href = url;
  link.download = addon.folder + ".zip";
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 60000);
}
async function commit(next) {
  library = await writeLibrary(next);
  showMessage("saved");
}
// Import an archive while recognizing an existing copy from any source.
// Addon folder + TOC version is the stable identity available in WoW 3.3.5a.
const folderKey = (value) => String(value || "").trim().toLowerCase();
const versionParts = (value) => String(value || "").match(/[0-9]+|[a-z]+/gi) || [];
function compareVersions(a, b) {
  const aa = versionParts(a), bb = versionParts(b), n = Math.max(aa.length, bb.length);
  for (let i = 0; i < n; i++) {
    const x = aa[i] ?? "0", y = bb[i] ?? "0";
    if (x === y) continue;
    const xn = /^\d+$/.test(x), yn = /^\d+$/.test(y);
    if (xn && yn) return Number(x) - Number(y);
    if (xn !== yn) return xn ? 1 : -1;
    return x.localeCompare(y);
  }
  return 0;
}
function groupCatalog(items) {
  const groups = new Map();
  for (const item of items) {
    const key = item.folder ? folderKey(item.folder) : `id:${item.id}`;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(item);
  }
  return [...groups.entries()].map(([key, candidates]) => {
    candidates.sort((a, b) => compareVersions(b.version, a.version));
    const preferred = candidates[0];
    return { ...preferred, groupKey: key, candidates };
  });
}
function installedFor(group) {
  return group.folder ? library.addons.find((a) => folderKey(a.folder) === folderKey(group.folder)) : null;
}
function updateFor(group) {
  const installed = installedFor(group);
  return Boolean(installed?.version && group.version && compareVersions(group.version, installed.version) > 0);
}
function renderDashboard() {
  $("#stat-installed").textContent = library.addons.length;
  if (!catalog?.items?.length) {
    $("#stat-updates").textContent = "—";
    $("#stat-available").textContent = t("notLoaded");
    return;
  }
  const groups = groupCatalog(catalog.items);
  $("#stat-updates").textContent = library.addons.filter((addon) => {
    const group = groups.find((g) => folderKey(g.folder) === folderKey(addon.folder));
    return group && updateFor(group);
  }).length;
  $("#stat-available").textContent = groups.filter((g) => !installedFor(g)).length;
}
function setProgress(message) {
  progressText = message || "";
  $("#progress-wrap").hidden = !progressText;
  $("#progress-label").textContent = progressText;
}
function recordError(error, context = "") {
  const detail = error?.stack || error?.message || String(error);
  errorLog.unshift(`${new Date().toLocaleString()}${context ? ` · ${context}` : ""}\n${detail}`);
  errorLog = errorLog.slice(0, 30);
}
async function addArchive(bytes) {
  const parts = await importZip(bytes);
  const accepted = [];
  const duplicates = [];
  const folders = [];
  for (const part of parts) {
    const existing = sameVersionCopy(library, part);
    const current = library.addons.find((addon) => folderKey(addon.folder) === folderKey(part.folder));
    const cannotSafelyReplace = current && (
      !part.version || (current.version && compareVersions(part.version, current.version) <= 0)
    );
    if (existing || cannotSafelyReplace) {
      duplicates.push({ folder: current?.folder || existing.folder, version: current?.version || existing.version });
      folders.push(current?.folder || existing.folder);
      continue;
    }
    accepted.push(part);
    folders.push(part.folder);
  }
  if (accepted.length) {
    const staged = accepted.map((part) => ({
      ...part,
      // A fresh storage key keeps the currently installed files intact until commit.
      storageFolder: "lh_" + crypto.randomUUID().replaceAll("-", ""),
    }));
    const next = mergeAddons(
      library,
      staged.map(({ files: _files, ...meta }) => meta),
    );
    await commitStagedGeneration({
      stage: () =>
        putAddons(
          staged.map(({ storageFolder, files }) => ({ folder: storageFolder, files })),
        ),
      commit: () => commit(next),
      rollback: () => staged.map((part) => deleteAddon(part.storageFolder)),
      // If cleanup is interrupted, startup GC removes the unreferenced generation.
      cleanup: () =>
        gc(next.addons.map((addon) => addon.storageFolder || addon.folder)).catch((error) =>
          console.warn("Old add-on generation cleanup skipped", error),
        ),
    });
  }
  return { folders, duplicates, imported: accepted.length };
}
async function action(fn) {
  if (busy) return;
  busy = true;
  render();
  try {
    // One operation at a time across manager tabs, always on fresh data.
    await withLock("lh-library", async () => {
      library = await readLibrary();
      await fn();
    });
  } catch (error) {
    recordError(error, "Addon manager operation");
    if (error instanceof AddonError) showMessage(error.code, error.params);
    else if (error.message === "libraryLimit") showMessage("libraryLimit");
    else {
      console.error("Addon manager operation failed", error);
      const detail = String(error?.message || error || "Unknown error").slice(0, 240);
      showMessage("importError", { detail });
    }
  } finally {
    busy = false;
    setProgress("");
    render();
  }
}
$("#remove-dialog").addEventListener("close", () => {
  if ($("#remove-dialog").returnValue !== "remove") return;
  const folder = $("#remove-dialog").dataset.folder;
  const addon = library.addons.find((item) => item.folder === folder);
  if (!addon) return;
  const makeBackup = $("#remove-backup").checked;
  action(async () => {
    if (makeBackup) await download(addon);
    await commit(removeAddon(library, addon.folder));
    await deleteAddon(addon.storageFolder || addon.folder);
  });
});
function render() {
  document.title = t("title") + " · Lasthavens";
  for (const node of document.querySelectorAll("[data-i18n]"))
    node.textContent = t(node.dataset.i18n);
  for (const node of document.querySelectorAll("[data-i18n-placeholder]"))
    node.placeholder = t(node.dataset.i18nPlaceholder);
  $("#import").disabled = busy;
  renderDashboard();
  $("#error-panel").hidden = !errorLog.length;
  renderGameStorage();
  $("#error-text").textContent = errorLog.join("\n\n--------------------\n\n");
  for (const id of ["#scan", "#update-all", "#backup", "#restore"]) $(id).disabled = busy;
  const current =
    session.lastStatus && session.lastStatus.revision === library.revision
      ? session.lastStatus
      : null;
  const { cycles } = loadOrder(library.addons.filter((a) => a.enabled));
  $("#count").textContent =
    `${library.addons.length} ${t("addons")} · ${library.addons.filter((a) => a.enabled).length} ${t("active")} · ${formatSize(library.addons.reduce((s, a) => s + a.size, 0))}`;
  const list = $("#list");
  list.replaceChildren();
  if (!library.addons.length) list.append(el("div", t("empty"), "empty"));
  for (const addon of library.addons) {
    const card = el("article");
    const row = el("div", "", "row");
    const title = el("div");
    title.append(
      el("h2", addon.title),
      el(
        "p",
        `${addon.folder} · ${addon.version || t("noVersion")} · ${addon.fileCount} ${t("files")} · ${formatSize(addon.size)}`,
        "meta",
      ),
    );
    const label = el("label", "", "switch-control");
    const input = el("input");
    input.type = "checkbox";
    input.role = "switch";
    input.checked = addon.enabled;
    input.disabled = busy;
    input.setAttribute("aria-label", addon.title);
    input.onchange = () =>
      action(() => commit(toggleAddon(library, addon.folder, input.checked)));
    const track = el("span", "", "switch-track");
    track.setAttribute("aria-hidden", "true");
    label.append(
      input,
      track,
      el("span", t(addon.enabled ? "enabled" : "disabled"), "switch-text"),
    );
    row.append(title, label);
    card.append(row, el("p", t("hint"), "apply-note"));
    if (addon.notes) card.append(el("p", addon.notes));
    if (addon.warnings?.includes("interface"))
      card.append(el("p", t("interfaceWarning"), "warning"));
    const missing = missingDependencies(addon, library);
    if (missing.length)
      card.append(
        el("p", t("dependencies", { names: missing.join(", ") }), "warning"),
      );
    const optional = missingOptional(addon, library);
    if (optional.length)
      card.append(el("p", t("optionalMissing", { names: optional.join(", ") }), "meta"));
    if (addon.loadOnDemand) card.append(el("p", t("loadOnDemand"), "meta"));
    if (addon.enabled && cycles.includes(addon.folder))
      card.append(el("p", t("dependencyCycle"), "warning"));
    if (addon.enabled && current) {
      const same = (f) => f.toLowerCase() === addon.folder.toLowerCase();
      const failure = current.failed?.find((f) => same(f.folder));
      if (failure)
        card.append(el("p", t("loadFailed", { message: failure.message }), "warning"));
      else if (current.loaded?.some(same))
        card.append(el("p", t("loadedInGame"), "meta"));
      else if (current.prepared?.some(same))
        card.append(el("p", t("preparedForGame"), "meta"));
    }
    const buttons = el("div", "", "actions");
    for (const [key, fn] of [
      ["exportZip", () => action(() => download(addon))],
      [
        "remove",
        () => {
          const dialog = $("#remove-dialog");
          $("#remove-description").textContent = t("removeAddonPrompt", { name: addon.title });
          $("#remove-backup").checked = true;
          dialog.dataset.folder = addon.folder;
          dialog.showModal();
        },
      ],
    ]) {
      const button = el("button", t(key));
      button.disabled = busy;
      button.onclick = fn;
      buttons.append(button);
    }
    card.append(buttons);
    list.append(card);
  }
  showMessage(messageKey, details);
  status();
  access();
  renderBrowse();
}
const ORIGINS = ["https://*.lasthavens.com/*"];
async function access() {
  let granted = false;
  try {
    granted = await browser.permissions.contains({ origins: ORIGINS });
  } catch (error) {
    console.warn("Could not check game-site permissions", error);
  }
  $("#perm").hidden = granted;
}
$("#grant").onclick = async () => {
  try {
    await browser.permissions.request({ origins: ORIGINS });
  } catch (e) {
    console.error("Permission request failed", e);
  }
  access();
};
function renderGameStorage() {
  const value = $("#game-storage-value");
  const detail = $("#game-storage-detail");
  const data = session.gameStorage;
  if (!data || !Number.isSafeInteger(data.usage) || data.usage < 0) {
    value.textContent = "—";
    detail.textContent = t("gameStorageWaiting");
    return;
  }
  value.textContent = formatSize(data.usage);
  const quota = Number.isSafeInteger(data.quota) && data.quota >= data.usage
    ? t("gameStorageQuota", { used: formatSize(data.usage), quota: formatSize(data.quota) })
    : "";
  detail.textContent = t("gameStorageMeasured", {
    time: new Date(data.at).toLocaleString(),
    origin: data.origin || "game site",
    quota
  });
}
function status() {
  const { lastStatus: s, seen } = session;
  $("#seen").textContent = seen?.length
    ? t("seenOn", { pages: seen.join(" · ") })
    : t("neverSeen");
  $("#status").textContent = !s
    ? t("waiting")
    : s.state === "error"
      ? t("loadError")
      : s.state === "pending" || s.revision !== library.revision
        ? t("changed")
        : s.failed?.length
          ? t("someFailed", { count: s.loaded?.length ?? 0, failed: s.failed.length })
          : t("ready", { count: s.loaded?.length ?? 0 });
}
$("#import").onchange = (e) => {
  const files = [...(e.target.files || [])];
  if (!files.length) {
    showMessage("importCancelled");
    return;
  }
  if (files.some((file) => file.size > LIMITS.archive)) {
    showMessage("errorArchiveSize");
    e.target.value = "";
    return;
  }
  showMessage("importReading", { count: files.length });
  action(async () => {
    let imported = 0;
    const duplicates = [];
    for (const file of files) {
      if (typeof file.stream !== "function")
        throw new AddonError("importFileUnsupported");
      const bytes = await readStreamLimited(file.stream(), LIMITS.archive, "errorArchiveSize");
      const result = await addArchive(bytes);
      imported += result.imported;
      duplicates.push(...result.duplicates);
    }
    if (duplicates.length && !imported)
      showMessage("sameVersionAlready", { names: duplicates.map((x) => x.folder).join(", ") });
    else if (duplicates.length)
      showMessage("importedSomeSkipped", { count: imported, names: duplicates.map((x) => x.folder).join(", ") });
    else showMessage("imported");
  }).finally(() => (e.target.value = ""));
};
// Live Game Scan: refresh the latest runtime diagnostics received from the game page.
// Actual file requests are observed by the injected overlay; the manager never claims
// an addon is loaded merely because it was packaged for injection.
$("#scan").onclick = async () => {
  session = await browser.storage.session.get(["lastStatus", "seen", "gameStorage"]);
  const current = session.lastStatus;
  if (!current) {
    showMessage("scanWaiting");
  } else if (current.revision !== library.revision) {
    showMessage("changed");
  } else {
    showMessage("liveScanDone", {
      loaded: current.loaded?.length || 0,
      prepared: current.prepared?.length || 0,
      failed: current.failed?.length || 0,
    });
  }
  render();
};

$("#backup").onclick = () => action(async () => {
  setProgress(t("backupProgress"));
  if (!library.addons.length) { showMessage("empty"); return; }
  const entries = [];
  for (const addon of library.addons) {
    const files = await getAllFiles(addon.storageFolder || addon.folder);
    for (const [path, bytes] of Object.entries(files)) entries.push([`${addon.folder}/${path}`, bytes]);
  }
  const expandedSize = entries.reduce((total, [, bytes]) => total + bytes.length, 0);
  if (expandedSize > LIMITS.expanded) throw new AddonError("errorExpanded");
  const archive = buildZip(entries);
  if (archive.length > LIMITS.archive) throw new AddonError("errorArchiveSize");
  const url = URL.createObjectURL(new Blob([archive], { type: "application/zip" }));
  const link = el("a");
  link.href = url;
  link.download = `lasthavens-addon-backup-${new Date().toISOString().slice(0,10)}.zip`;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 60000);
  showMessage("backupDone");
});
$("#restore").onchange = (e) => {
  const file = e.target.files?.[0];
  if (!file) return;
  if (file.size > LIMITS.archive) {
    showMessage("errorArchiveSize");
    e.target.value = "";
    return;
  }
  action(async () => {
    setProgress(t("restoreProgress"));
    const result = await addArchive(await readStreamLimited(file.stream(), LIMITS.archive, "errorArchiveSize"));
    showMessage("restoreDone", { count: result.imported });
  }).finally(() => { e.target.value = ""; });
};

$("#update-all").onclick = async () => {
  let granted = false;
  try { granted = await requestAccess(); } catch (error) { console.warn("Update permission was not granted", error); }
  if (!granted) { showMessage("browsePerm"); return; }
  return action(async () => {
  setProgress(t("updateProgress"));
  showMessage("updateProgress");
  if (!catalog || catalog.stale) catalog = await loadCatalog(true);
  const groups = groupCatalog(catalog.items);
  let updated = 0, failed = 0;
  for (const installed of [...library.addons]) {
    const group = groups.find((g) => folderKey(g.folder) === folderKey(installed.folder));
    if (!group || !installed.version || !group.version || compareVersions(group.version, installed.version) <= 0) continue;
    const candidate = group.candidates.find((c) => c.version === group.version && c.downloads?.some((d) => d.kind !== "page"));
    const dl = candidate?.downloads?.find((d) => d.kind !== "page");
    if (!dl) continue;
    try {
      const result = await addArchive(await fetchAddonZip(dl));
      if (result.imported) updated++;
    } catch (error) {
      failed++;
      recordError(error, `Update ${installed.folder}`);
      console.warn("Could not update addon", installed.folder, error);
    }
  }
  showMessage(updated && failed ? "updateDoneWithFailures" : updated ? "updateDone" : failed ? "errorUnexpected" : "updateNone", { count: updated, failed });
  });
};

$("#language").onchange = async (e) => {
  await setLocale(e.target.value);
  await browser.storage.local.set({ language: e.target.value });
  render();
};
$("#badge").checked = (await browser.storage.local.get("settings")).settings?.badge !== false;
$("#badge").onchange = async (e) => {
  const { settings } = await browser.storage.local.get("settings");
  await browser.storage.local.set({
    settings: { ...settings, badge: e.target.checked },
  });
};
$("#play").onclick = () =>
  browser.tabs.create({ url: "https://lasthavens.com/play" });
browser.storage.onChanged.addListener(async (changes, area) => {
  if (area === "session") {
    session = await browser.storage.session.get(["lastStatus", "seen", "gameStorage"]);
    render();
  }
  if (area === "local" && changes.library && !busy) {
    library = await readLibrary();
    render();
  }
});
const openTab = (url) => browser.tabs.create({ url });
function showView(next) {
  view = next;
  $("#view-mine").hidden = next !== "mine";
  $("#view-browse").hidden = next !== "browse";
  for (const [id, name] of [
    ["#tab-mine", "mine"],
    ["#tab-browse", "browse"],
  ]) {
    $(id).setAttribute("aria-selected", String(name === next));
    $(id).classList.toggle("active", name === next);
  }
  if (next === "browse") openBrowse();
}
async function openBrowse(force = false) {
  const granted = await hasAccess();
  $("#bperm").hidden = granted;
  $("#bmain").hidden = !granted;
  if (!granted) return;
  catalogError = "";
  if (!catalog || force) {
    $("#bstatus").textContent = t("catalogLoading");
    try {
      catalog = await loadCatalog(force);
    } catch {
      catalogError = "catalogError";
    }
  }
  renderBrowse();
}
function fillCategories() {
  const names = [...new Set(catalog.items.map((i) => i.category))],
    select = $("#bcat"),
    stamp = t("allCategories") + "|" + names.join("|");
  if (select.dataset.stamp === stamp) return;
  select.dataset.stamp = stamp;
  select.replaceChildren();
  for (const [value, label] of [["", t("allCategories")], ...names.map((n) => [n, n])]) {
    const option = el("option", label);
    option.value = value;
    select.append(option);
  }
  select.value = names.includes(category) ? category : "";
}
function fillStatusFilter() {
  const select = $("#bfilter");
  const stamp = ["", "installed", "available", "updates"].map((v) => `${v}:${t(v ? `filter${v[0].toUpperCase()}${v.slice(1)}` : "filterAll")}`).join("|");
  if (select.dataset.stamp !== stamp) {
    select.dataset.stamp = stamp;
    select.replaceChildren();
    for (const [value, key] of [["", "filterAll"], ["installed", "filterInstalled"], ["available", "filterAvailable"], ["updates", "filterUpdates"]]) {
      const option = el("option", t(key));
      option.value = value;
      select.append(option);
    }
  }
  select.value = statusFilter;
}
function renderBrowse() {
  if (view !== "browse" || $("#bmain").hidden) return;
  const list = $("#blist"),
    info = $("#bstatus");
  list.replaceChildren();
  if (!catalog) {
    info.textContent = t(catalogError || "catalogLoading");
    return;
  }
  fillCategories();
  fillStatusFilter();
  renderDashboard();
  const q = query.trim().toLowerCase(),
    shown = groupCatalog(catalog.items).filter(
      (i) => {
        const installed = Boolean(installedFor(i));
        const update = updateFor(i);
        return (!category || i.candidates.some((c) => c.category === category)) &&
          (!q || i.candidates.some((c) => `${c.name} ${c.description} ${c.category} ${c.folder || ""} ${c.version || ""} ${c.repository || ""}`.toLowerCase().includes(q))) &&
          (!statusFilter || (statusFilter === "installed" && installed) || (statusFilter === "available" && !installed) || (statusFilter === "updates" && update));
      },
    );
  info.textContent = t(catalog.stale ? "catalogOffline" : "catalogInfo", {
    count: shown.length,
    time: formatDate(catalog.fetchedAt),
  });
  if (!catalog.stale && catalog.warnings?.length)
    info.textContent += ` · ${catalog.warnings.length} source scan warning(s)`;
  if (!shown.length) list.append(el("div", t("noResults"), "empty"));
  for (const group of shown) {
    const selectedId = selectedSources.get(group.groupKey);
    const entry = group.candidates.find((c) => c.id === selectedId) || group;
    const card = el("article"),
      head = el("div", "", "row"),
      title = el("div"),
      installedAddon = entry.folder
        ? library.addons.find((a) => folderKey(a.folder) === folderKey(entry.folder))
        : null,
      updateAvailable = installedAddon && installedAddon.version && entry.version && compareVersions(entry.version, installedAddon.version) > 0,
      folders = group.candidates.flatMap((candidate) => installedMap[candidate.id] || []).filter((f) =>
        library.addons.some((a) => folderKey(a.folder) === folderKey(f)),
      );
    const meta = [entry.folder, entry.version, `${group.candidates.length} source(s)`].filter(Boolean).join(" · ");
    title.append(el("h2", entry.name), el("p", meta, "meta"));
    head.append(title);
    if (folders.length || installedAddon) head.append(el("span", t("installedBadge"), "badge"));
    card.append(head);
    if (updateAvailable) card.append(el("p", `${t("updateAvailable")}: ${installedAddon.version} → ${entry.version}`, "update-tag"));
    if (entry.description) card.append(el("p", entry.description));
    const screenshotUrl = entry.screenshot || entry.image || entry.image_url || "";
    if (/^https:\/\//i.test(screenshotUrl)) {
      const preview = el("img", "", "addon-screenshot");
      preview.src = screenshotUrl;
      preview.alt = `${entry.name} screenshot`;
      preview.loading = "lazy";
      preview.decoding = "async";
      preview.title = "Open screenshot";
      preview.tabIndex = 0;
      preview.setAttribute("role", "button");
      const openScreenshot = () => openTab(screenshotUrl);
      preview.addEventListener("click", openScreenshot);
      preview.addEventListener("keydown", (event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); openScreenshot(); } });
      preview.addEventListener("error", () => preview.remove(), { once: true });
      card.append(preview);
    }
    if (group.candidates.length > 1) {
      const sourceSelect = el("select", "", "source-select");
      sourceSelect.setAttribute("aria-label", t("chooseSource"));
      for (const candidate of group.candidates) {
        const option = el("option", `${candidate.repository || candidate.category} · ${candidate.version || t("noVersion")}`);
        option.value = candidate.id;
        option.selected = candidate.id === entry.id;
        sourceSelect.append(option);
      }
      sourceSelect.onchange = () => { selectedSources.set(group.groupKey, sourceSelect.value); renderBrowse(); };
      card.append(sourceSelect);
    }
    if (entry.requires.length)
      card.append(el("p", t("requiresNote", { names: entry.requires.join(", ") }), "warning"));
    const buttons = el("div", "", "actions"),
      many = entry.downloads.length > 1;
    for (const dl of entry.downloads) {
      const key = entry.id + "|" + dl.url,
        button = el("button"),
        label = many && dl.label ? ": " + dl.label : "";
      if (dl.kind === "page") {
        button.textContent = t("openPage") + label;
        button.onclick = () => openTab(dl.url);
      } else {
        button.textContent =
          installing === key
            ? t("installing")
            : t(folders.length || installedAddon ? "reinstall" : "install") + label;
        button.disabled = busy;
        button.onclick = () => installEntry(entry, dl, key);
      }
      buttons.append(button);
    }
    if (entry.source) {
      const button = el("button", t("sourceLink"));
      button.onclick = () => openTab(entry.source.url);
      buttons.append(button);
    }
    card.append(buttons);
    list.append(card);
  }
}
function installEntry(entry, dl, key) {
  installing = key;
  action(async () => {
    setProgress(t("installProgress"));
    const result = await addArchive(await fetchAddonZip(dl));
    await rememberInstalled(entry.id, result.folders);
    installedMap = await readInstalled();
    if (result.duplicates.length && !result.imported)
      showMessage("sameVersionAlready", { names: result.duplicates.map((x) => x.folder).join(", ") });
    else if (result.duplicates.length)
      showMessage("installedSomeSkipped", { name: entry.name, names: result.duplicates.map((x) => x.folder).join(", ") });
    else showMessage("installedFrom", { name: entry.name });
  }).finally(() => {
    installing = "";
    render();
  });
}
$("#tab-mine").onclick = () => showView("mine");
$("#tab-browse").onclick = () => showView("browse");
$("#bgrant").onclick = async () => {
  try {
    await requestAccess();
  } catch (e) {
    console.error("Permission request failed", e);
  }
  openBrowse();
};
$("#brefresh").onclick = () => openBrowse(true);
$("#bsearch").oninput = (e) => {
  query = e.target.value;
  renderBrowse();
};
$("#bcat").onchange = (e) => {
  category = e.target.value;
  renderBrowse();
};
$("#bfilter").onchange = (e) => {
  statusFilter = e.target.value;
  renderBrowse();
};
$("#copy-errors").onclick = async () => {
  try {
    await navigator.clipboard.writeText(errorLog.join("\n\n--------------------\n\n"));
    showMessage("errorsCopied");
  } catch (error) {
    recordError(error, "Copy error log");
    showMessage("copyErrorsFailed");
    render();
  }
};

render();
