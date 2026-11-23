// Browse tab: combines curated lists with automatic scanning of 3.3.5a GitHub repos.
import { SOURCES, REPOSITORIES, ORIGINS, releaseRepo } from "./catalog.js";
import { LIMITS, buildZip, readStreamLimited } from "./archive.js";
import { tocMeta } from "./toc.js";
import { AddonError } from "./errors.js";
export { ORIGINS };

const FRESH_MS = 24 * 60 * 60 * 1000;
const CATALOG_SCHEMA = 3;
const repoTreeCache = new Map();

export const DOWNLOAD_ORIGINS = [
  "https://api.github.com/*",
  "https://github.com/*",
  "https://raw.githubusercontent.com/*",
  "https://codeload.github.com/*",
  "https://objects.githubusercontent.com/*",
  "https://release-assets.githubusercontent.com/*",
  "https://media.githubusercontent.com/*",
  "https://gitlab.com/*",
];

export async function hasAccess() {
  try {
    return await browser.permissions.contains({ origins: DOWNLOAD_ORIGINS });
  } catch {
    // Do not assume optional network permissions were granted if the API fails.
    return false;
  }
}
// Must be called straight from a tap handler. Game-site permissions are separate.
export const requestAccess = () => browser.permissions.request({ origins: DOWNLOAD_ORIGINS });

const rawUrl = (owner, repo, branch, path) =>
  `https://raw.githubusercontent.com/${owner}/${repo}/${encodeURIComponent(branch)}/${path
    .split("/")
    .map(encodeURIComponent)
    .join("/")}`;

async function getRepoTree(source) {
  if (repoTreeCache.has(source.id)) return repoTreeCache.get(source.id);
  const infoUrl = `https://api.github.com/repos/${source.owner}/${source.repo}`;
  const infoRes = await fetch(infoUrl, {
    cache: "no-cache",
    headers: { Accept: "application/vnd.github+json" },
  });
  if (!infoRes.ok) throw new Error(`${source.owner}/${source.repo}: HTTP ${infoRes.status}`);
  const info = await infoRes.json();
  const branch = info.default_branch;
  if (!branch) throw new Error(`${source.owner}/${source.repo}: default branch unavailable`);
  const treeUrl = `https://api.github.com/repos/${source.owner}/${source.repo}/git/trees/${encodeURIComponent(branch)}?recursive=1`;
  const treeRes = await fetch(treeUrl, {
    cache: "no-cache",
    headers: { Accept: "application/vnd.github+json" },
  });
  if (!treeRes.ok) throw new Error(`${source.owner}/${source.repo}: tree HTTP ${treeRes.status}`);
  const tree = await treeRes.json();
  if (!Array.isArray(tree.tree)) throw new Error(`${source.owner}/${source.repo}: invalid repository tree`);
  const result = { branch, files: tree.tree, truncated: Boolean(tree.truncated) };
  repoTreeCache.set(source.id, result);
  return result;
}

async function mapLimit(items, limit, fn) {
  const out = new Array(items.length);
  let next = 0;
  const workers = Array.from({ length: Math.min(limit, items.length) }, async () => {
    while (true) {
      const index = next++;
      if (index >= items.length) return;
      out[index] = await fn(items[index], index);
    }
  });
  await Promise.all(workers);
  return out;
}

function addonRootCandidates(files) {
  const result = [];
  for (const file of files) {
    if (file.type !== "blob" || file.mode === "120000" || !/\.toc$/i.test(file.path)) continue;
    const slash = file.path.lastIndexOf("/");
    if (slash < 1) continue; // A root-level TOC cannot be packaged safely on its own.
    const root = file.path.slice(0, slash);
    const folder = root.slice(root.lastIndexOf("/") + 1);
    const tocName = file.path.slice(slash + 1, -4);
    // WoW 3.3.5 add-on folders must match their TOC filename.
    if (folder.toLowerCase() !== tocName.toLowerCase()) continue;
    if (!/^[A-Za-z0-9_-]+$/.test(folder) || folder.toLowerCase().startsWith("blizzard_")) continue;
    result.push({ root, folder, tocPath: file.path });
  }
  result.sort((a, b) => a.root.split("/").length - b.root.split("/").length);
  const selected = [];
  for (const candidate of result) {
    if (selected.some((parent) => candidate.root.toLowerCase().startsWith(parent.root.toLowerCase() + "/")))
      continue;
    if (!selected.some((old) => old.root.toLowerCase() === candidate.root.toLowerCase()))
      selected.push(candidate);
  }
  return selected;
}

async function scanRepository(source) {
  const tree = await getRepoTree(source);
  if (tree.truncated)
    throw new Error("GitHub truncated this repository tree; skipped to avoid incomplete add-on packages");
  const candidates = addonRootCandidates(tree.files);
  const compatible = await mapLimit(candidates, 8, async (candidate) => {
    try {
      const response = await fetch(rawUrl(source.owner, source.repo, tree.branch, candidate.tocPath), {
        cache: "no-cache",
      });
      if (!response.ok) return null;
      const meta = tocMeta(await response.text());
      const interfaces = meta.interface.split(/[\s,]+/).filter(Boolean);
      if (!interfaces.includes("30300")) return null;
      return { ...candidate, meta };
    } catch (error) {
      console.warn("Could not inspect TOC", candidate.tocPath, error);
      return null;
    }
  });

  return compatible.filter(Boolean).map((addon) => {
    const slug = addon.root.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
    // Prefer an explicitly named screenshot/preview in the add-on folder.
    const images = tree.files.filter((file) => file.type === "blob" && file.path.startsWith(addon.root + "/") && /\.(png|jpe?g|webp|gif)$/i.test(file.path));
    images.sort((a, b) => {
      const rank = (path) => /screenshot|preview|in.?game|demo/i.test(path) ? 0 : /icon|logo|banner/i.test(path) ? 2 : 1;
      return rank(a.path) - rank(b.path) || a.path.localeCompare(b.path);
    });
    const screenshot = images[0] ? rawUrl(source.owner, source.repo, tree.branch, images[0].path) : "";
    return {
      screenshot,
      id: `${source.id}-${slug}`,
      name: addon.meta.title || addon.folder,
      category: source.name,
      description: addon.meta.notes || `Verified TOC interface 30300 · ${source.owner}/${source.repo}`,
      requires: addon.meta.dependencies || [],
      version: addon.meta.version || "",
      folder: addon.folder,
      repository: `${source.owner}/${source.repo}`,
      downloads: [
        {
          label: "Install",
          kind: "github-addon",
          owner: source.owner,
          repo: source.repo,
          branch: tree.branch,
          root: addon.root,
          folder: addon.folder,
        },
      ],
      source: {
        label: "View source",
        url: `https://github.com/${source.owner}/${source.repo}/tree/${encodeURIComponent(tree.branch)}/${addon.root
          .split("/")
          .map(encodeURIComponent)
          .join("/")}`,
      },
      scanWarning: tree.truncated ? "Repository tree was truncated by GitHub; this scan may be incomplete." : "",
    };
  });
}

async function scanRepositories() {
  const results = await Promise.allSettled(REPOSITORIES.map((source) => scanRepository(source)));
  const items = [];
  const warnings = [];
  results.forEach((result, index) => {
    if (result.status === "fulfilled") items.push(...result.value);
    else {
      const source = REPOSITORIES[index];
      warnings.push(`${source.owner}/${source.repo}: ${result.reason?.message || "scan failed"}`);
      console.warn("Repository scan failed", source.id, result.reason);
    }
  });
  return { items, warnings };
}

export async function loadCatalog(force = false) {
  const { catalog } = await browser.storage.local.get("catalog");
  if (
    !force &&
    catalog?.schema === CATALOG_SCHEMA &&
    catalog?.items?.length &&
    Date.now() - catalog.fetchedAt < FRESH_MS
  ) return { ...catalog, stale: false };

  if (force) repoTreeCache.clear();
  const items = [];
  const warnings = [];
  for (const source of SOURCES) {
    try {
      const res = await fetch(source.url, { cache: "no-cache" });
      if (!res.ok) throw new Error("HTTP " + res.status);
      for (const item of source.parse(await res.text()))
        if (!source.skip?.has(item.id)) items.push({ ...item, source_id: source.id });
    } catch (error) {
      warnings.push(`${source.name}: ${error.message || "catalog load failed"}`);
      console.warn("Curated catalog load failed", source.id, error);
    }
  }
  const scanned = await scanRepositories();
  items.push(...scanned.items);
  warnings.push(...scanned.warnings);
  if (!items.length) {
    if (catalog?.items?.length) return { ...catalog, stale: true };
    throw new Error("No add-ons could be discovered");
  }
  const next = { schema: CATALOG_SCHEMA, fetchedAt: Date.now(), items, warnings };
  await browser.storage.local.set({ catalog: next });
  return { ...next, stale: false };
}

const pickZip = (zips) =>
  (zips.find((z) => !/source|src/i.test(z.name)) || zips[0])?.url;
// Finds the newest release's ZIP. The GitHub API allows only 60 anonymous requests an hour,
// so when it refuses, read the release page instead.
async function releaseZipUrl(url) {
  const { owner, repo } = releaseRepo(url);
  try {
    const res = await fetch(`https://api.github.com/repos/${owner}/${repo}/releases/latest`, {
      headers: { Accept: "application/vnd.github+json" },
    });
    if (res.ok) {
      const found = pickZip(
        ((await res.json()).assets || [])
          .filter((a) => /\.zip$/i.test(a.name))
          .map((a) => ({ name: a.name, url: a.browser_download_url })),
      );
      if (found) return found;
      throw new AddonError("noZipRelease");
    }
  } catch (error) {
    if (error instanceof AddonError) throw error;
  }
  const page = await fetch(`https://github.com/${owner}/${repo}/releases/latest`);
  const tag = page.ok && /\/releases\/tag\/([^/?#]+)/.exec(page.url)?.[1];
  if (!tag) throw new AddonError("downloadFailed", { detail: "release page" });
  const assets = await fetch(`https://github.com/${owner}/${repo}/releases/expanded_assets/${tag}`);
  if (!assets.ok) throw new AddonError("downloadFailed", { detail: "HTTP " + assets.status });
  const html = await assets.text(),
    zips = [...html.matchAll(/href="(\/[^\"]*\/releases\/download\/[^\"]*\.zip)"/gi)].map((m) => ({
      name: decodeURIComponent(m[1].split("/").pop()),
      url: "https://github.com" + m[1],
    }));
  const found = pickZip(zips);
  if (!found) throw new AddonError("noZipRelease");
  return found;
}

async function fetchGithubAddonZip(download) {
  const source = REPOSITORIES.find((r) => r.owner === download.owner && r.repo === download.repo) || {
    id: `${download.owner}/${download.repo}`,
    owner: download.owner,
    repo: download.repo,
  };
  let tree = repoTreeCache.get(source.id);
  if (!tree || tree.branch !== download.branch) {
    const apiUrl = `https://api.github.com/repos/${download.owner}/${download.repo}/git/trees/${encodeURIComponent(download.branch)}?recursive=1`;
    const res = await fetch(apiUrl, { cache: "no-cache", headers: { Accept: "application/vnd.github+json" } });
    if (!res.ok) throw new AddonError("downloadFailed", { detail: `GitHub tree HTTP ${res.status}` });
    const data = await res.json();
    if (!Array.isArray(data.tree) || data.truncated)
      throw new AddonError("downloadFailed", { detail: "Repository tree unavailable or truncated" });
    tree = { branch: download.branch, files: data.tree, truncated: false };
    repoTreeCache.set(source.id, tree);
  }
  const prefix = download.root + "/";
  const files = tree.files.filter(
    (file) => file.type === "blob" && file.mode !== "120000" && file.path.startsWith(prefix),
  );
  if (!files.length || files.length > LIMITS.count)
    throw new AddonError("downloadFailed", { detail: "No files found or add-on file count limit exceeded" });
  const estimated = files.reduce((sum, file) => sum + (Number(file.size) || 0), 0);
  if (estimated > LIMITS.expanded || files.some((file) => Number(file.size) > LIMITS.file))
    throw new AddonError("errorExpanded");

  const budget = { used: 0, limit: LIMITS.expanded };
  const entries = await mapLimit(files, 6, async (file) => {
    const response = await fetch(rawUrl(download.owner, download.repo, download.branch, file.path));
    if (!response.ok) throw new AddonError("downloadFailed", { detail: `${file.path}: HTTP ${response.status}` });
    const bytes = await readStreamLimited(response.body, LIMITS.file, "errorExpanded", budget);
    if (bytes.length !== Number(file.size))
      throw new AddonError("downloadFailed", { detail: `${file.path}: file changed during download` });
    return [`${download.folder}/${file.path.slice(prefix.length)}`, bytes];
  });
  const archive = buildZip(entries);
  if (archive.length > LIMITS.archive) throw new AddonError("errorArchiveSize");
  return archive;
}

export async function fetchAddonZip(download) {
  if (download.kind === "github-addon") return fetchGithubAddonZip(download);
  const url = download.kind === "release" ? await releaseZipUrl(download.url) : download.url;
  let res;
  try {
    res = await fetch(url, { redirect: "follow" });
  } catch (error) {
    throw new AddonError("downloadFailed", { detail: error.message || "network" });
  }
  if (!res.ok) throw new AddonError("downloadFailed", { detail: "HTTP " + res.status });
  if (Number(res.headers.get("content-length")) > LIMITS.archive)
    throw new AddonError("errorArchiveSize");
  return readStreamLimited(res.body, LIMITS.archive, "errorArchiveSize");
}

// Remember which folders each catalog entry installed, to show "Installed".
export async function readInstalled() {
  return (await browser.storage.local.get("browseInstalled")).browseInstalled || {};
}
export async function rememberInstalled(id, folders) {
  const map = await readInstalled();
  map[id] = folders;
  await browser.storage.local.set({ browseInstalled: map });
}
