# GitHub Actions supply-chain and permissions policy

## Least-privilege permissions

- Workflow-level `permissions` is `contents: read`. Jobs must declare any write scope they need.
- Only `create_tag` (tag create/retarget/delete-draft) and `publish_release` (release publish and asset cleanup) hold `contents: write`.
- Pull-request validation and the quality gate are read-only, never access signing secrets, and never publish artifacts outside CI artifact storage.

## Immutable action pins

Every third-party action is pinned to a full commit SHA with a trailing comment naming the release tag or version the SHA corresponds to, for example:

```yaml
uses: actions/checkout@11d5960a326750d5838078e36cf38b85af677262 # v4
```

Pinned actions as of 2026-11-25:

| Action | Pinned version | Notes |
| --- | --- | --- |
| `actions/checkout` | v4 | |
| `actions/setup-java` | v6.3.0 | |
| `actions/upload-artifact` | v4 | |
| `actions/download-artifact` | v4 | publish job only |
| `android-actions/setup-android` | v3 | |
| `gradle/actions/setup-gradle` | v4 | |
| `softprops/action-gh-release` | v3.0.4 | upgraded from v2 (final v2 release 2.6.2 is unmaintained and uses the deprecated Node 20 runtime); v3 keeps the `id` output and honors `draft: true` by leaving reused drafts unpublished |

## Update procedure

1. Dependabot opens `github-actions` update PRs against the pinned SHAs with a limit of 3 open PRs and a weekly schedule (`.github/dependabot.yml` lives on the default branch, so its settings apply repository-wide). Review the PR before its bump.
2. Verify the target release: read the upstream release notes/changelog, confirm the release is not a prerelease (unless intentionally adopted) and check for breaking changes in behavior used by our workflows (outputs such as `id`, `draft` semantics, inputs).
3. Resolve the immutable commit SHA of the target tag. For lightweight tags `refs/tags/<tag>` is already the commit; for annotated tags use the peeled ref:

   ```sh
   git ls-remote https://github.com/<owner>/<repo>.git "refs/tags/<tag>^{}"
   ```

4. Update the `uses:` line to the new SHA and update the trailing tag comment so the comment always matches the pinned SHA.
5. For release-affecting actions (`softprops/action-gh-release`), verify changed outputs or draft/publish behavior against the steps that validate release assets before merging, and keep release changes out of unrelated bug-fix PRs.
6. CI must pass (PR validation on a beta PR) before merging the update.
