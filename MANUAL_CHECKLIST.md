# LastHavenPlus — Owner-only Actions

This file is limited to decisions and actions that require the owner's account, private signing material, legal approval or explicit release authorization. Implementation and automated validation tasks belong in [TODO.md](TODO.md).

## Distribution permission

- [ ] Obtain written guidance from the Lasthavens team confirming whether this independent Android client and native Addon Manager may be distributed under the current Terms of Use.
- [ ] Keep the app clearly identified as unofficial and unaffiliated with Lasthavens or Blizzard Entertainment.

## Signing-key custody

- [ ] Keep at least two verified offline backups of the permanent release keystore.
- [ ] Store recovery passwords and alias information separately in a secure location.
- [ ] Never commit or share keystore files, passwords, aliases or signing material.

## Release approval

- [ ] Review the generated draft GitHub Release, APK name/ABI, version metadata, signing identity, SHA-256 checksum and release notes.
- [ ] Explicitly approve publication. A successful CI build or draft creation is not publication approval.
- [ ] Confirm the README and CHANGELOG describe only features present in the release.

No physical-device test tasks are assigned here. The development workflow should use automated unit, integration, screenshot and emulator-based validation wherever available; remaining platform uncertainty must be reported accurately.
