# Changelog

The full, per-release history lives in [docs/changelog.md](docs/changelog.md).

## 1.4.0

### Added

- Structured errors: `ZodIssue`, `ZodIssueCode`, `ZodPathSegment`, `ZodError.issues` and
  `ZodError.flatten()`.
- `ZodValidationException`, thrown by `parse`/`parseSuspend`, carrying the full `ZodError`.
- `ZodResult.getOrNull()`, `getOrThrow()`, `map()`, `fold()`, `isSuccess`, `isFailure`.
- Explicit coercion via `Zod.coerce.number()/boolean()/long()/date()`.
- `Zod.lazy { ... }` for recursive schemas.
- `JsonElement` interop: `parseJson`, `safeParseJson`, `toZodValue`.
- `Zod.date()` factory.
- `toJsonSchema()` for structural JSON Schema generation.
- `ZodConfig.errorMap` for custom/localized messages.
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

### Tooling

- Gradle 9.4.1, JDK 21 toolchain with Foojay auto-provisioning.
- `apiDump`/`apiCheck` via the binary compatibility validator.
- Dokka and Kover plugins; coverage report in CI.
- CI now checks all native targets, the API dump, Maven Local publishing and caching.
- Release workflow tags, creates the GitHub release and publishes to Maven Central in one run.
- Added Dependabot, SECURITY, CODE_OF_CONDUCT, issue/PR templates and `.editorconfig`.
- Strict `explicitApi()` and ktlint checks.
