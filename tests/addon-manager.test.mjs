import test from "node:test";
import assert from "node:assert/strict";
import { webcrypto } from "node:crypto";
import { readFile } from "node:fs/promises";
import vm from "node:vm";
import { zipSync } from "../app/src/main/assets/extensions/lh-addon-manager/vendor/fflate.js";
import { importZip, exportZip, readStreamLimited } from "../app/src/main/assets/extensions/lh-addon-manager/archive.js";
import {
  emptyLibrary,
  loadOrder,
  mergeAddons,
  missingDependencies,
  sameVersionCopy,
} from "../app/src/main/assets/extensions/lh-addon-manager/library.js";
import { tocMeta } from "../app/src/main/assets/extensions/lh-addon-manager/toc.js";
import { hasAccess, requestAccess, DOWNLOAD_ORIGINS } from "../app/src/main/assets/extensions/lh-addon-manager/browse.js";
import { commitStagedGeneration } from "../app/src/main/assets/extensions/lh-addon-manager/staged-storage.js";
import { createMigrationArchive, MIGRATION_FORMAT, MIGRATION_SCHEMA } from "../app/src/main/assets/extensions/lh-addon-manager/migration-export.js";
import { unzipSync } from "../app/src/main/assets/extensions/lh-addon-manager/vendor/fflate.js";

if (!globalThis.crypto) globalThis.crypto = webcrypto;

const bytes = (value) => new TextEncoder().encode(value);

test("TOC metadata parses title, version, interface and dependency variants", () => {
  const meta = tocMeta([
    "## Title: Example Addon",
    "## Version: 2.4.1",
    "## Interface: 30300, 30400",
    "## Dependencies: Core, Shared",
    "## OptionalDeps: Extras",
    "## Dep10: Last",
    "## Dep2: MoreCore",
    "## Dep1: Base",
    "## LoadOnDemand: 1",
  ].join("\n"));

  assert.equal(meta.title, "Example Addon");
  assert.equal(meta.version, "2.4.1");
  assert.equal(meta.interface, "30300, 30400");
  assert.deepEqual(meta.dependencies, ["Core", "Shared", "Base", "MoreCore", "Last"]);
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
    addons: [{
      folder: "Example",
      version: "1.0",
      size: 10,
      enabled: true,
      storageFolder: "lh_old_generation",
    }],
  };
  const merged = mergeAddons(current, [
    {
      folder: "example",
      version: "2.0",
      size: 20,
      enabled: false,
      storageFolder: "lh_new_generation",
    },
  ]);

  assert.equal(merged.addons.length, 1);
  assert.equal(merged.addons[0].version, "2.0");
  assert.equal(merged.addons[0].enabled, true);
  assert.equal(merged.addons[0].storageFolder, "lh_new_generation");
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


test("stream reader accepts data within the configured byte limit", async () => {
  const stream = new ReadableStream({
    start(controller) {
      controller.enqueue(bytes("abc"));
      controller.enqueue(bytes("de"));
      controller.close();
    },
  });
  assert.deepEqual([...await readStreamLimited(stream, 5)], [...bytes("abcde")]);
});

test("stream reader rejects actual bytes beyond archive and shared expanded limits", async () => {
  const oversizedArchive = new ReadableStream({
    start(controller) {
      controller.enqueue(bytes("1234"));
      controller.enqueue(bytes("56"));
      controller.close();
    },
  });
  await assert.rejects(
    readStreamLimited(oversizedArchive, 5, "errorArchiveSize"),
    (error) => error.code === "errorArchiveSize",
  );

  const budget = { used: 3, limit: 5 };
  const oversizedTotal = new ReadableStream({
    start(controller) {
      controller.enqueue(bytes("abc"));
      controller.close();
    },
  });
  await assert.rejects(
    readStreamLimited(oversizedTotal, 10, "errorExpanded", budget),
    (error) => error.code === "errorExpanded",
  );
  assert.equal(budget.used, 3);
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
  let requested;
  globalThis.browser = {
    permissions: {
      contains: async () => { throw new Error("API unavailable"); },
      request: async (value) => { requested = value; return true; },
    },
  };
  assert.equal(await hasAccess(), false);

  globalThis.browser.permissions.contains = async (value) => {
    assert.deepEqual(value.origins, DOWNLOAD_ORIGINS);
    return true;
  };
  assert.equal(await hasAccess(), true);

  globalThis.browser.permissions.contains = async () => false;
  assert.equal(await hasAccess(), false);
  assert.equal(await requestAccess(), true);
  assert.deepEqual(requested.origins, DOWNLOAD_ORIGINS);
  assert.ok(DOWNLOAD_ORIGINS.includes("https://api.github.com/*"));
  assert.ok(DOWNLOAD_ORIGINS.includes("https://raw.githubusercontent.com/*"));
  delete globalThis.browser;
});

test("metadata commit failure keeps the previous addon generation and rolls back staged files", async () => {
  const previousLibrary = {
    revision: "stable-revision",
    addons: [{ folder: "Example", version: "1.0", enabled: true, storageFolder: "lh_previous" }],
  };
  const previousFiles = new Map([["lh_previous/Example.lua", bytes("old addon data")]]);
  const stagedFiles = new Map();
  let library = previousLibrary;

  await assert.rejects(
    commitStagedGeneration({
      stage: async () => stagedFiles.set("lh_staged/Example.lua", bytes("new addon data")),
      commit: async () => {
        throw new Error("simulated metadata storage failure");
      },
      rollback: () => [
        Promise.resolve().then(() => stagedFiles.delete("lh_staged/Example.lua")),
      ],
      cleanup: async () => {
        throw new Error("cleanup must not run after failed commit");
      },
    }),
    /simulated metadata storage failure/,
  );

  assert.equal(library, previousLibrary);
  assert.equal(library.addons[0].storageFolder, "lh_previous");
  assert.equal(new TextDecoder().decode(previousFiles.get("lh_previous/Example.lua")), "old addon data");
  assert.equal(stagedFiles.has("lh_staged/Example.lua"), false);
});


test("migration export includes library state, manager preferences, and checksummed raw files", async () => {
  const sourceLibrary = {
    revision: "revision-123",
    addons: [{
      folder: "Example",
      storageFolder: "lh_generation_1",
      version: "2.3.4",
      enabled: true,
      dependencies: ["Core"],
      optionalDeps: ["Extras"],
      size: 38,
      fileCount: 2,
    }],
  };
  const sourceFiles = {
    "Example.toc": bytes("## Interface: 30300\n"),
    "Example.lua": bytes("print('migration')"),
  };
  const { archive, manifest } = await createMigrationArchive({
    library: sourceLibrary,
    preferences: {
      settings: { badge: false },
      language: "fr",
      browseInstalled: { "catalog-entry": ["Example"] },
    },
    getFiles: async (folder) => {
      assert.equal(folder, "lh_generation_1");
      return sourceFiles;
    },
    exportedAt: 123456,
  });

  assert.equal(manifest.format, MIGRATION_FORMAT);
  assert.equal(manifest.schema, MIGRATION_SCHEMA);
  assert.equal(manifest.exportedAt, 123456);
  assert.equal(manifest.library.revision, "revision-123");
  assert.equal(manifest.library.addons[0].enabled, true);
  assert.deepEqual(manifest.library.addons[0].dependencies, ["Core"]);
  assert.deepEqual(manifest.preferences.settings, { badge: false });
  assert.equal(manifest.preferences.language, "fr");
  assert.deepEqual(manifest.preferences.browseInstalled, { "catalog-entry": ["Example"] });
  assert.equal(manifest.totals.files, 2);
  assert.equal(manifest.totals.expandedBytes, 38);

  const unpacked = unzipSync(archive);
  const exportedManifest = JSON.parse(new TextDecoder().decode(unpacked["manifest.json"]));
  assert.deepEqual(exportedManifest, manifest);
  assert.deepEqual([...unpacked["files/lh_generation_1/Example.lua"]], [...sourceFiles["Example.lua"]]);
  assert.match(manifest.files[0].sha256, /^[a-f0-9]{64}$/);
  assert.equal(manifest.files.find((file) => file.path === "Example.toc").size, sourceFiles["Example.toc"].length);
  assert.deepEqual(sourceLibrary.addons[0], {
    folder: "Example", storageFolder: "lh_generation_1", version: "2.3.4",
    enabled: true, dependencies: ["Core"], optionalDeps: ["Extras"], size: 38, fileCount: 2,
  });
});

test("migration export rejects unsafe paths, missing TOCs, and mismatched metadata", async () => {
  const addon = { folder: "Example", size: 3, fileCount: 1 };
  await assert.rejects(
    createMigrationArchive({
      library: { revision: "x", addons: [addon] },
      getFiles: async () => ({ "../escape.lua": bytes("bad"), "Example.toc": bytes("x") }),
    }),
    /Unsafe or duplicate file path/,
  );
  await assert.rejects(
    createMigrationArchive({
      library: { revision: "x", addons: [addon] },
      getFiles: async () => ({ "Other.toc": bytes("x") }),
    }),
    /TOC file is missing/,
  );

  await assert.rejects(
    createMigrationArchive({
      library: { revision: "x", addons: [{ folder: "Example", storageFolder: "same" }, { folder: "Second", storageFolder: "SAME" }] },
      getFiles: async () => ({ "Example.toc": bytes("x") }),
    }),
    /invalid or duplicate storage folder/,
  );
  await assert.rejects(
    createMigrationArchive({
      library: { revision: "x", addons: [{ folder: "Example" }] },
      getFiles: async () => ({ "Example.toc": bytes("x"), "nested\\\\escape.lua": bytes("x") }),
    }),
    /Unsafe or duplicate file path/,
  );
  await assert.rejects(
    createMigrationArchive({
      library: { revision: "x", addons: [{ ...addon, size: 99 }] },
      getFiles: async () => ({ "Example.toc": bytes("x") }),
    }),
    /file size does not match/,
  );
});

test("empty migration export is valid and preserves the empty library revision", async () => {
  const { archive, manifest } = await createMigrationArchive({
    library: { revision: "empty-revision", addons: [] },
    getFiles: async () => ({}),
  });
  assert.equal(manifest.library.revision, "empty-revision");
  assert.equal(manifest.totals.addons, 0);
  assert.equal(manifest.totals.files, 0);
  const unpacked = unzipSync(archive);
  assert.equal(JSON.parse(new TextDecoder().decode(unpacked["manifest.json"])).format, MIGRATION_FORMAT);
});


test("WebView game bridge rejects foreign origins and streams native add-on files", async () => {
  const source = await readFile(
    new URL("../app/src/main/assets/webview-game-bridge.js", import.meta.url),
    "utf8",
  );
  const origin = "https://lasthavens.com";
  const windowListeners = [];
  const nativeListeners = [];
  const nativeRequests = [];
  const posted = [];
  const window = {
    location: { origin },
    addEventListener(type, listener) {
      if (type === "message") windowListeners.push(listener);
    },
    postMessage(data) {
      posted.push(data);
    },
  };
  window.top = window;
  const native = {
    addEventListener(type, listener) {
      if (type === "message") nativeListeners.push(listener);
    },
    postMessage(raw) {
      const request = JSON.parse(raw);
      nativeRequests.push(request);
      queueMicrotask(() => {
        if (request.kind === "payload") {
          const payload = {
            revision: "native-rev",
            badge: false,
            addons: [{
              folder: "Example",
              storageFolder: "native_example",
              files: [{ path: "Example.toc", size: 4 }],
            }],
          };
          nativeListeners.forEach((listener) => listener({
            data: JSON.stringify({ kind: "payload", payload }),
          }));
        } else if (request.kind === "chunk") {
          const buffer = vm.runInContext("new ArrayBuffer(4)", context);
          new Uint8Array(buffer).set([1, 2, 3, 4]);
          nativeListeners.forEach((listener) => listener({ data: buffer }));
        }
      });
    },
  };
  window.LastHavenNative = native;
  const context = vm.createContext({ window, location: window.location, console, setTimeout, clearTimeout });
  vm.runInContext(source, context);

  const dispatch = (messageOrigin, data) => {
    const event = { source: window, origin: messageOrigin, data };
    windowListeners.forEach((listener) => listener(event));
  };
  dispatch("https://attacker.example", { channel: "lh-addons-v2", kind: "hello" });
  assert.equal(nativeRequests.length, 0);

  dispatch(origin, { channel: "lh-addons-v2", kind: "hello" });
  await new Promise((resolve) => setTimeout(resolve, 0));

  assert.deepEqual(nativeRequests.map((request) => request.kind), ["payload", "chunk"]);
  assert.equal(nativeRequests[1].storageFolder, "native_example");
  const delivered = posted.find((message) => message?.kind === "payload");
  assert.ok(delivered);
  assert.equal(delivered.payload.revision, "native-rev");
  assert.equal(delivered.payload.badge, false);
  assert.deepEqual(Array.from(delivered.payload.addons[0].files["Example.toc"]), [1, 2, 3, 4]);
});
