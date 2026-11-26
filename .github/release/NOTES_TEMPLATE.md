# Release notes template

Use this structure for every user-facing GitHub Release and matching version entry in `CHANGELOG.md`. Keep the language clear, concise and focused on what users will notice. Describe behavior, not PRs, issue numbers, commits or internal implementation details.

# LastHavenPlus vX.Y.Z — Short, descriptive title

One or two sentences explaining the main benefit of the update.

## New features

- Describe user-visible capabilities and what they let people do.

## Bug fixes and reliability

- Describe corrected behavior and reliability improvements in plain language.

## Small changes

- List visual polish, convenience improvements and other minor changes.

## Compatibility

- State supported Android version, supported CPU architecture and browser engine when relevant.
- Mention important upgrade, data/profile or migration notes.
- State meaningful feature limitations clearly.
- Distinguish automated CI results from physical-device testing; never imply hardware testing that was not performed.

## Writing rules

- Include only changes that are present in the release candidate.
- Use short bullets and explain technical terms when they matter to users.
- Put incomplete or unavailable functionality in a clear limitation note; do not imply it works.
- Do not include internal agent activity, PR/issue references, raw commit lists or CI job inventories.
- Keep the same headings and user-focused tone in the GitHub Release body and the version section in `CHANGELOG.md`.
