# Automatic stable releases

Stable releases are produced from merges into `main`. The Android workflow calculates
the next SemVer version from tags that are reachable from the exact main commit, creates
the tag on that commit, builds and signs the ARM64 APK, verifies tests/signature/ABI,
generates the updater payload, and creates a **draft** GitHub Release only after every
required build and asset check succeeds.

## Release version rules

Use a Conventional Commit title for release-bearing changes:

- `feat: ...` or `feat(scope): ...` increments the minor version.
- `fix: ...`, `perf: ...`, `refactor: ...`, `revert: ...`, or `security: ...`
  increments the patch version.
- Add `!` before the colon (for example `feat(api)!: ...`) or include a
  `BREAKING CHANGE:` footer to increment the major version.
- `docs:`, `chore:`, `ci:`, `test:`, and `style:` commits do not create a
  release; they still run the normal main-branch CI build.

The next version is based on the highest valid `vMAJOR.MINOR.PATCH` tag reachable
from the current main commit. Beta-only tags are excluded unless their commits are
actually part of main history. This also lets a later main merge build on a previous
main draft release, even before that draft is published.

An explicit `[release:X.Y.Z]` marker and the manual `workflow_dispatch` version input
remain available for controlled promotions. The workflow rejects version regressions
and never rewrites a published release.

## Publication

The workflow creates a draft, not a public release. GitHub-generated notes are scoped
to changes since the previous tag, and the draft includes the verified ARM64 APK,
updater manifest, and an optional differential patch when its integrity and size checks
pass. Inspect the draft and publish it when the release is approved.
