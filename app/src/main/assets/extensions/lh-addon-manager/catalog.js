// Addon sources for the Browse tab. Each source turns a hosted page into a list of
// { id, name, description, downloads: [{ label, url, kind }], source }.
// kind "zip": the manager can download and import it. kind "page": open it in a tab.
export const SOURCES = [
  {
    id: "nom0re",
    name: "NoM0Re 3.3.5a addon list",
    url: "https://raw.githubusercontent.com/NoM0Re/WoW-3.3.5a-Addons/main/README.md",
    parse: parseReadme,
    // Desktop tools, not addons.
    skip: new Set(["warperia-app", "addon-update-tool"]),
  },
];

// Public repositories that explicitly target WoW 3.3.5/3.3.5a. The scanner
// verifies each addon's own TOC interface before exposing it in Browse.
export const REPOSITORIES = [
  { id: "trinitycore", name: "TrinityCore", owner: "TrinityCore", repo: "wow_335a_addons" },
  { id: "plan414", name: "Plan414", owner: "Plan414", repo: "World-of-Warcraft-3.3.5-Addons" },
  { id: "qyh214", name: "qyh214", owner: "qyh214", repo: "addons-335a-collection" },
  { id: "cookta2012", name: "cookta2012", owner: "cookta2012", repo: "3.3.5a-Addons" },
  { id: "jwverzijden", name: "jwverzijden", owner: "jwverzijden", repo: "wowaddons" },
  { id: "locus313", name: "locus313", owner: "locus313", repo: "WoW-3.3.5a-Addons" },
  { id: "crallen", name: "crallen", owner: "crallen", repo: "wow-addons-3.3.5a" },
  { id: "getparanoid", name: "GetParanoid", owner: "GetParanoid", repo: "335_Addons" },
  { id: "matejkadlec", name: "Warmane AddOns", owner: "matejkadlec", repo: "warmane-addons" },
  { id: "nom0re2", name: "NoM0Re Extended", owner: "NoM0Re", repo: "WoW-3.3.5a-Addons2" },
];

// Hosts the manager may download from (kept in step with optional_host_permissions).
export const ORIGINS = [
  "https://github.com/*",
  "https://raw.githubusercontent.com/*",
  "https://codeload.github.com/*",
  "https://objects.githubusercontent.com/*",
  "https://release-assets.githubusercontent.com/*",
  "https://media.githubusercontent.com/*",
  "https://gitlab.com/*",
  "https://api.github.com/*",
];
const ZIP_HOSTS = new Set([
  "github.com",
  "raw.githubusercontent.com",
  "codeload.github.com",
  "gitlab.com",
]);
// Pages that GitHub redirects to the newest release; resolved to a zip asset at install time.
const RELEASE_PAGE = /^https:\/\/github\.com\/([^/]+)\/([^/]+)\/releases(\/latest)?\/?$/;
export function releaseRepo(url) {
  const m = RELEASE_PAGE.exec(url);
  return m ? { owner: m[1], repo: m[2] } : null;
}
export function linkKind(url) {
  if (releaseRepo(url)) return "release";
  let u;
  try {
    u = new URL(url);
  } catch {
    return "page";
  }
  return u.protocol === "https:" &&
    ZIP_HOSTS.has(u.hostname) &&
    /\.zip$/i.test(u.pathname)
    ? "zip"
    : "page";
}
const plain = (s) =>
  s
    .replace(/!\[[^\]]*\]\([^)]*\)/g, "")
    .replace(/\[([^\]]*)\]\([^)]*\)/g, "$1")
    .replace(/[*_`]+/g, "")
    .replace(/<[^>]+>/g, "")
    .replace(/\s+/g, " ")
    .trim();
export function parseReadme(text) {
  // Layout: "# Category" headings (the first "# " is the page title) holding "### Addon" blocks.
  const parts = text.split(/^# +/m).slice(2),
    items = [],
    ids = new Set();
  for (const part of parts) {
    const nl = part.indexOf("\n"),
      category = plain(part.slice(0, nl < 0 ? undefined : nl));
    for (const block of part.split(/^### +/m).slice(1)) {
      const bnl = block.indexOf("\n"),
        name = plain(block.slice(0, bnl < 0 ? undefined : bnl)),
        rest = bnl < 0 ? "" : block.slice(bnl + 1),
        downloads = [],
        requires = [];
      let source = null;
      for (const m of rest.matchAll(/\[([^\]]+)\]\((https:\/\/[^)\s]+)\)/g)) {
        const label = plain(m[1]),
          url = m[2];
        if (/^download/i.test(label)) {
          if (!downloads.some((d) => d.url === url))
            downloads.push({
              label: label.replace(/^download\s*/i, ""),
              url,
              kind: linkKind(url),
            });
        } else if (!/BackToContents/.test(m[0])) source = { label, url };
      }
      for (const line of rest.split("\n")) {
        const r = line.match(/^>\s*\**\s*Requires?:\s*(.*?)\**\s*$/i);
        if (r) requires.push(plain(r[1]));
      }
      if (!name || !downloads.length) continue;
      // Description: the text before the first note, link line or image.
      const cut = rest.search(/^(>|🔗|!\[|<)/m);
      let description = plain(cut < 0 ? rest : rest.slice(0, cut));
      if (description.length > 220)
        description = description.slice(0, 217).replace(/\s+\S*$/, "") + "…";
      let id = name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
      while (ids.has(id)) id += "-2";
      ids.add(id);
      items.push({ id, name, category, description, downloads, requires, source });
    }
  }
  return items;
}
