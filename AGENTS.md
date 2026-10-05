# Repository instructions

These instructions apply to the entire repository.

## Project scope

- Maintain Minecraft 26.2 only, using Fabric and JDK 25.
- Work on `ver/26.2`, the default development branch.
- Keep `litematica-printer` as the mod ID. The display name is Litematica Printer 4 / 打印机四改.
- Do not restore other Minecraft version directories or the multi-version wrapper.

## Conversation and commits

- Use Simplified Chinese in user conversations unless the user requests another language.
- Use English only in all new commit subjects, bodies, and explanatory footers.
- Follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/): `type(scope): description`. Scope is optional; use `!` or a `BREAKING CHANGE:` footer for breaking changes.
- Use lowercase types such as `feat`, `fix`, `docs`, `build`, `ci`, `test`, `refactor`, `perf`, `style`, or `chore`. Write a concise, imperative English description.
- Group files and changes into separate commits by topic. When a file contains changes for multiple topics, stage the corresponding hunks separately.
- Examples: `fix(placement): prefer real support faces`, `feat(fill): destroy blocking blocks before placement`, `docs: add English and Simplified Chinese READMEs`.
- Apply the English rule to new commits. Do not rewrite published history merely to translate existing messages.

## Documentation

- `README.md` is the English README; `README.zh_CN.md` is the Simplified Chinese README.
- Keep the two versions equivalent in scope and behavior, and retain their language switch links.
- Preserve the project introduction, special thanks, and AGPL-3.0 license attribution.
- Keep examples, settings, Minecraft compatibility, dependencies, and build commands synchronized with the implementation.
- Update issue template links when moving documentation or changing section headings.

## Implementation and validation

- Read the affected code before changing behavior and keep changes focused on the requested work.
- Respect selection bounds, working range, material availability, and configured breaking restrictions.
- Send valid game actions; server confirmation determines whether breaking or placement succeeds.
- For Java or Gradle changes, run `./gradlew :26.2:build` (Windows: `.\gradlew.bat :26.2:build`) with JDK 25. Add regression tests when they exercise meaningful behavior.
- For documentation-only changes, check Markdown links, language consistency, and `git diff --check`; a full game build is unnecessary.
- State validation accurately. Do not claim in-game verification unless Minecraft was actually used to test the change.
- Keep generated jars, dependencies, caches, temporary scripts, and credentials out of commits.

## Version changes

- The mod version is configured in `gradle.properties`; generated versions and jar names are defined in `buildSrc/src/main/kotlin/ModProjectExtension.kt` and the Gradle build files.
- Distinguish the mod version, Minecraft compatibility, release tags, and build identifiers.
- Release mod metadata uses the base SemVer from `mod_version`; tags use `v<mod_version>` and jars use `litematica-printer-<mod_version>-mc26.2.jar`. Non-release builds append channel, build ID, and commit identifiers as build metadata and filename suffixes.
- Treat version naming proposals as proposals until adopted; update the build, release workflows, version comparison, and both READMEs together when implementing a new scheme.
