# Security Policy

## Supported versions

Security fixes are prioritized for the latest published LastHavenPlus release. Users should update from the official GitHub Releases page: https://github.com/iNeonyne/LastHavenPlus/releases

## Reporting a vulnerability

Please do not publish exploitable vulnerability details in a public issue.

Use GitHub's Report a vulnerability option on the repository Security tab when private vulnerability reporting is available. If unavailable, contact the repository owner through the GitHub profile and request a private disclosure channel before sending technical details.

Include the affected version and commit, Android version and device architecture, reproduction steps and impact, proof-of-concept details that do not expose real credentials or user data, and any suggested mitigation.

Please allow reasonable time to investigate and prepare a fix before public disclosure.

## Scope

Sensitive areas include updater URL and redirect validation; release metadata, APK and differential-patch size limits; APK package identity, checksum and signing-certificate verification; GeckoView navigation, content permissions and profile handling; diagnostic log redaction and local data handling; and GitHub Actions permissions and release signing secrets.

Never include passwords, cookies, session tokens, keystores or private keys in a report.
