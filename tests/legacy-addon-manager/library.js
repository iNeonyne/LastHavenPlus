import { LIMITS } from "./limits.js";
export const emptyLibrary = () => ({ revision: "empty", addons: [] });
const same = (a, b) => a.toLowerCase() === b.toLowerCase();
const versionKey = (value) => String(value || "").trim().toLowerCase();

// Detect an identical release even when it was imported from a different source.
export function sameVersionCopy(library, incoming) {
  const version = versionKey(incoming.version);
  if (!version) return null;
  return (
    library.addons.find(
      (addon) =>
        same(addon.folder, incoming.folder) &&
        versionKey(addon.version) === version,
    ) || null
  );
}
export function mergeAddons(library, items) {
  const next = library.addons.map((a) => ({ ...a }));
  for (const item of items) {
    const at = next.findIndex((a) => same(a.folder, item.folder));
    const addon = { ...item, enabled: at >= 0 ? next[at].enabled : false };
    if (at >= 0) next[at] = addon;
    else next.push(addon);
  }
  if (next.reduce((n, a) => n + a.size, 0) > LIMITS.total)
    throw Error("libraryLimit");
  return { ...library, addons: next };
}
export function toggleAddon(library, folder, enabled) {
  return {
    ...library,
    addons: library.addons.map((a) =>
      a.folder === folder ? { ...a, enabled } : a,
    ),
  };
}
export function removeAddon(library, folder) {
  return {
    ...library,
    addons: library.addons.filter((a) => a.folder !== folder),
  };
}
const isEnabled = (library, name) =>
  library.addons.some((a) => a.enabled && same(a.folder, name));
export function missingDependencies(addon, library) {
  return (addon.dependencies || []).filter((d) => !isEnabled(library, d));
}
export function missingOptional(addon, library) {
  return (addon.optionalDeps || []).filter((d) => !isEnabled(library, d));
}
// Dependencies (required, then optional) come before the addons that use
// them; otherwise the library order is kept. Cycles are reported, not fatal.
export function loadOrder(addons) {
  const byName = new Map(addons.map((a) => [a.folder.toLowerCase(), a]));
  const state = new Map(),
    order = [],
    cycles = [];
  const visit = (a) => {
    const key = a.folder.toLowerCase();
    if (state.get(key) === 2) return;
    if (state.get(key) === 1) {
      cycles.push(a.folder);
      return;
    }
    state.set(key, 1);
    for (const dep of [...(a.dependencies || []), ...(a.optionalDeps || [])]) {
      const target = byName.get(dep.toLowerCase());
      if (target) visit(target);
    }
    state.set(key, 2);
    order.push(a);
  };
  addons.forEach(visit);
  return { order, cycles };
}
