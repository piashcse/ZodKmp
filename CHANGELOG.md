# Changelog

The full, per-release history lives in [docs/changelog.md](docs/changelog.md).

## 2.0.0

The first release of the rewritten validation core. It contains breaking changes.

### Breaking changes

- Implicit coercion was removed; use `Zod.coerce.number()/boolean()/long()/date()`.
- `Zod.set` now requires a `Set` instead of converting lists.
- `parse`/`parseSuspend` throw `ZodValidationException` (still an `IllegalArgumentException`).
- `ZodError` is a class exposing structured `issues`; the old `List<String>` constructor is gone.
- `ZodString` and `ZodObjectSchema` are no longer data classes; `ZodOptional`/`ZodNullable` are
  no longer value classes.
- `merge` lets later keys win instead of throwing; `strict`/`strip`/`passthrough` are last-call-wins.
- `.regex()` matches substrings, string checks run in declaration order and lengths count code
  points.

### Added

- Structured errors: `ZodIssue`, `ZodIssueCode`, `ZodPathSegment`, `ZodError.issues` and
  `ZodError.flatten()`.
- `ZodValidationException`, thrown by `parse`/`parseSuspend`, carrying the full `ZodError`.
- `ZodResult.getOrNull()`, `getOrThrow()`, `map()`, `fold()`, `isSuccess`, `isFailure`.
- Explicit coercion via `Zod.coerce.number()/boolean()/long()/date()`.
- `Zod.lazy { ... }` for recursive schemas.
- `JsonElement` interop: `parseJson`, `safeParseJson`, `toZodValue`.
- `Zod.date()` factory.
- `toJsonSchema()` for structural JSON Schema generation with `$ref`/`$defs` for recursion.
- `ZodConfig.errorMap` for custom/localized messages, plus bundled `ZodLocales` (en/es/fr/bn).
- Optional `zodkmp-ksp` module: `@ZodModel` code generation.
- Typed `refine()` on string, number, boolean, array, set, object and date schemas.
- Object intersection overload that merges shapes, and `ZodError.prepend` path support.

### Changed

- Numeric, boolean, long and date schemas no longer coerce silently; use `Zod.coerce`.
- `Zod.set` requires a `Set`; lists are no longer converted.
- `merge` lets later keys win instead of throwing; `strict`/`strip`/`passthrough` are last-call-wins.
- Object and record keys must be strings.
- `.regex()` matches substrings like Zod.
- String checks run in declaration order; lengths count code points.
- `deepPartial` preserves collection constraints.

### Fixed

- URL validator rejected query strings, ports, localhost and mixed case.
- `datetime()` accepted date-only input.
- `cuid2()`/`nanoid()` accepted values of any length.
- `multipleOf()` failed on decimal steps and silently ignored zero.
- Suspend refinements threw when nested in objects, arrays or wrappers.
- Missing object properties are reported as `Required`.
- `optionalString`/`optionalNumber`/`optionalBoolean` helpers did not compile.

### Tests

- 327 common tests run on JVM, JS, Wasm, iOS Simulator and macOS, plus 8 KSP codegen tests.
- Line coverage is 99.2% for the library module (Kover).

### Dependencies

- Kotlin 2.4.21, Gradle 9.9.2, AGP 9.5.3, Compose Multiplatform 1.12.2.
- Android compile/target SDK 37.
- AndroidX activity 1.13.0, appcompat 1.8.0, core 1.19.1, lifecycle 2.11.0.
- binary-compatibility-validator 0.18.2; Dokka, Kover, ktlint, KSP and serialization updated
  to their latest stable releases.
- Example app migrated to the AutoMirrored Material icons and current Compose preview import.

### Tooling

- Gradle 9.4.1, JDK 21 toolchain with Foojay auto-provisioning.
- `apiDump`/`apiCheck` via the binary compatibility validator.
- Dokka and Kover plugins; coverage report in CI.
- CI now checks all native targets, the API dump, Maven Local publishing and caching.
- Release workflow tags, creates the GitHub release and publishes to Maven Central in one run.
- Added Dependabot, SECURITY, CODE_OF_CONDUCT, issue/PR templates and `.editorconfig`.
- Strict `explicitApi()` and ktlint checks.
