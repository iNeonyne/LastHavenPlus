# GitHub Actions supply-chain and permissions policy

## Least-privilege permissions

- Workflow-level permissions default to `contents: read`.
- In `.github/workflows/android.yml`, only `create_tag` and `publish_release` receive `contents: write`; build and PR-validation jobs are read-only.
- `.github/workflows/quality.yml` and `.github/workflows/markdown-quality.yml` are read-only.
- `.github/workflows/agent-ownership.yml` grants only `issues: write` to the issue-labeling job.
- Pull-request validation does not reference Android signing secrets. It builds an unsigned release APK and uploads test reports only to GitHub Actions artifact storage. It does not publish GitHub releases or signed APKs.

## Immutable action pins

All third-party `uses:` references in the four workflows are pinned to full commit SHAs. The values below were checked against the corresponding upstream tag refs on 2026-11-26. The Gradle v4 tag is annotated; its peeled commit is listed.

| Action | Release tag | Verified commit SHA |
| --- | --- | --- |
| `actions/checkout` | v7.0.1 | `3d3c42e5aac5ba805825da76410c181273ba90b1` |
| `actions/setup-java` | v6.3.0 | `d1374f8ac74474200d16ef8ecee22c7ae7a51880` |
| `android-actions/setup-android` | v4.0.4 | `be39fa834029ff78f1a44aa3bb0819b8fc2bd8fd` |
| `gradle/actions/setup-gradle` | v6.4.1 | `8da46b539cd8fe6ab27ca75128919a18d757604c` |
| `actions/upload-artifact` | v7.0.2 | `cf430e030ddbb5b0abf93d22962f4752f3646cd9` |
| `actions/download-artifact` | v4 | `d3f86a106a0bac45b974a628896c90dbdf5c8093` |
| `softprops/action-gh-release` | v3.0.4 | `8a0cd651e5b2365bd640854a3fa5dfc70e4875f3` |
| `DavidAnson/markdownlint-cli2-action` | v25.1.0 | `3ce8f712b5d8f31acd8e201be6d97e3d2bb23eb7` |

The ownership workflow currently uses no third-party actions. The Markdown quality workflow runs the pinned markdownlint-cli2 action across all repository Markdown files; its lint configuration documents narrowly scoped legacy-style exclusions. A matching tag ref establishes that the pin points at the named upstream release; it is not by itself a claim that every upstream tag is cryptographically signed.

## Automated updates

`.github/dependabot.yml` enables weekly GitHub Actions dependency checks, targets `beta`, and limits the number of open update PRs to three. Dependabot updates must still be reviewed; automation does not authorize merging.

## Update procedure

1. Review the Dependabot PR and identify the upstream action release it proposes.
2. Read the upstream release notes/changelog. Confirm the release is intentional (including prerelease status) and check breaking changes in workflow inputs, outputs, runtime requirements, and behavior used here.
3. Resolve the tag to its immutable commit SHA. For a lightweight tag, the ref points directly to the commit. For an annotated tag, peel it to the commit:

   ```sh
   git ls-remote https://github.com/<owner>/<repo>.git "refs/tags/<tag>^{}"
   ```

4. Independently verify the tag ref and peeled commit through the upstream GitHub repository. Update the `uses:` SHA and trailing version comment together.
5. For release-affecting actions (especially `softprops/action-gh-release`), verify outputs such as `id`, draft/publish semantics, and asset behavior against the calling workflow. Keep such changes isolated from unrelated fixes.
6. Run the full PR validation against `beta`. Review exact-head CI and workflow permissions before Chaty merges.

Never put signing secrets in pull-request validation, broaden build/test token permissions, or merge a workflow/action update solely because Dependabot opened it.
