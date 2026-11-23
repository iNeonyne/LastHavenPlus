const list = (s) => (s || "").split(/[,\s]+/).filter(Boolean);
const unique = (a) => [...new Set(a)];
const clean = (s) => (s || "").replace(/\|c[0-9a-f]{8}|\|r/gi, "");
export function tocMeta(text) {
  const m = {};
  for (const line of text.split(/\r?\n/)) {
    const x = line.match(/^##\s*([^:]+):\s*(.*)$/);
    if (x) m[x[1].trim().toLowerCase()] = x[2].trim();
  }
  const numbered = Object.keys(m)
    .filter((k) => /^dep\d+$/.test(k))
    .sort((a, b) => Number(a.slice(3)) - Number(b.slice(3)))
    .flatMap((k) => list(m[k]));
  return {
    title: clean(m.title),
    version: clean(m.version),
    notes: clean(m.notes),
    interface: m.interface || "",
    dependencies: unique([
      ...list(m.dependencies || m.requireddeps || m.deps),
      ...numbered,
    ]),
    optionalDeps: unique(list(m.optionaldeps)),
    loadOnDemand: m.loadondemand === "1",
  };
}
