import test from "node:test";
import assert from "node:assert/strict";
import { webcrypto } from "node:crypto";
import { zipSync } from "../app/src/main/assets/extensions/lh-addon-manager/vendor/fflate.js";
import { importZip, exportZip } from "../app/src/main/assets/extensions/lh-addon-manager/archive.js";
import {
  emptyLibrary,
  loadOrder,
  mergeAddons,
  missingDependencies,
  sameVersionCopy,
} from "../app/src/main/assets/extensions/lh-addon-manager/library.js";
import { tocMeta } from "../app/src/main/assets/extensions/lh-addon-manager/toc.js";
import { hasAccess } from "../app/src/main/assets/extensions/lh-addon-manager/browse.js";

if (!globalThis.crypto) globalThis.crypto = webcrypto;

const bytes = (value) => new TextEncoder().encode(value);

test("TOC metadata parses title, version, interface and dependency variants", () => {
  const meta = tocMeta([
    "## Title: Example Addon",
    "## Version: 2.4.1",
    "## Interface: 30300, 30400",
    "## Dependencies: Core, Shared",
    "## OptionalDeps: Extras",
    "## Dep2: MoreCore",
    "## Dep1: Base",
    "## LoadOnDemand: 1",
  ].join("\n"));

  assert.equal(meta.title, "Example Addon");
  assert.equal(meta.version, "2.4.1");
  assert.equal(meta.interface, "30300, 30400");
  assert.deepEqual(meta.dependencies, ["Core", "Shared", "Base", "MoreCore"]);
  assert.deepEqual(meta.optionalDeps, ["Extras"]);
  assert.equal(meta.loadOnDemand, true);
});

test("load order puts dependencies before dependants and reports cycles", () => {
  const addons = [
    { folder: "Feature", dependencies: ["Core"] },
    { folder: "Core", dependencies: [] },
  ];
  assert.deepEqual(loadOrder(addons).order.map((a) => a.folder), ["Core", "Feature"]);

  const cyclic = loadOrder([
    { folder: "A", dependencies: ["B"] },
    { folder: "B", dependencies: ["A"] },
  ]);
  assert.equal(cyclic.order.length, 2);
  assert.ok(cyclic.cycles.length > 0);
});

test("merge replaces matching folders case-insensitively and preserves enabled state", () => {
  const current = {
    ...emptyLibrary(),
    addons: [{ folder: "Example", version: "1.0", size: 10, enabled: true }],
  };
  const merged = mergeAddons(current, [
    { folder: "example", version: "2.0", size: 20, enabled: false },
  ]);

  assert.equal(merged.addons.length, 1);
  assert.equal(merged.addons[0].version, "2.0");
  assert.equal(merged.addons[0].enabled, true);
});

test("same-version detection is case-insensitive and dependencies use enabled state", () => {
  const library = {
    ...emptyLibrary(),
    addons: [
      { folder: "Example", version: "1.0", enabled: true },
      { folder: "Core", version: "1.0", enabled: false },
    ],
  };
  assert.equal(sameVersionCopy(library, { folder: "example", version: "1.0" }).folder, "Example");
  assert.deepEqual(
    missingDependencies({ dependencies: ["Example", "Core", "Missing"] }, library),
    ["Core", "Missing"],
  );
});

test("ZIP export and import round-trip preserves addon file bytes and metadata", async () => {
  const archive = exportZip({
    folder: "Example",
    files: {
      "Example.toc": bytes("## Title: Example\n## Version: 1.2.3\n## Interface: 30300"),
      "Example.lua": bytes("print('hello')"),
    },
  });
  const [addon] = await importZip(archive);

  assert.equal(addon.folder, "Example");
  assert.equal(addon.title, "Example");
  assert.equal(addon.version, "1.2.3");
  assert.equal(addon.fileCount, 2);
  assert.deepEqual([...addon.files["Example.lua"]], [...bytes("print('hello')")]);
});

test("ZIP import rejects path traversal before storing addon files", async () => {
  const archive = zipSync({
    "../Outside/Outside.toc": bytes("## Interface: 30300"),
  });
  await assert.rejects(importZip(archive), (error) => error.code === "errorPath");
});

test("ZIP import rejects duplicate paths that differ only by case", async () => {
  const archive = zipSync({
    "Example/Example.toc": bytes("## Interface: 30300"),
    "example/example.TOC": bytes("## Interface: 30300"),
  });
  await assert.rejects(importZip(archive), (error) => error.code === "errorDuplicate");
});

test("Browse permission checks fail closed when the permissions API errors", async () => {
  globalThis.browser = { permissions: { contains: async () => { throw new Error("API unavailable"); } } };
  assert.equal(await hasAccess(), false);

  globalThis.browser = { permissions: { contains: async () => true } };
  assert.equal(await hasAccess(), true);

  globalThis.browser = { permissions: { contains: async () => false } };
  assert.equal(await hasAccess(), false);
  delete globalThis.browser;
});
