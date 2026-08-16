<!-- repository: packages/java/observability | kind: PACKAGE | stack: java-maven -->

# observability — Skill: Package Development

> Workflow for observability (packages/java/observability). Execute this workflow before, during, and
> after changes in this repository.

## Repository Facts

- Kind: Shared Package
- Package: `com.omnixys:observability` (version: 1.0.4)
- Runtime: Java 26 (Maven)
- Description: Omnixys Observability Core Package — tracing, metrics, OpenTelemetry integration.
- Architecture: src/main/java/com/omnixys/observability/{tracing, metrics, autoconfigure}
- Database: n/a; Migrations: n/a
- API: n/a
- Messaging: n/a
- Tests: JUnit 5 + Mockito (src/test/java)


## Workflow

### 1. Understand the change

- Identify consumers of this package across `omnixys/services` and other packages.
- This package is published (`com.omnixys:observability`); consumers pin versions and rely on SemVer.

### 2. Implement

- Keep the public API surface explicit and intentional.
- For TypeScript packages, generated/transpiled output (e.g. `dist/`) must not be hand-edited.
- Reuse established Omnixys packages where relevant.

### 3. Write tests

- Unit tests exercise public API behavior and edge cases.
- Type tests are included where the package defines a `type-tests` suite.
- Verify exports compile from a consumer perspective.

### 4. Validate

## Validation

Run each applicable check and record the result as `PASS`, `FAIL`, `PRE-EXISTING
FAILURE`, or `NOT RUN` (with a reason). Never convert `NOT RUN` into `PASS`.

  - `mvn -B dependency:go-offline (requires GitHub Packages credentials for some repos)`
  - `mvn -B checkstyle:check (when configured)`
  - `compile phase of mvn verify`
  - `mvn -B test`
  - `mvn -B clean verify`

## Commit

- Use Conventional Commits (`<type>(<scope>): <summary>`), e.g. `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `perf`.
- Stage only files belonging to the logical change. Run `git diff --check` before committing.
- Commit locally; never push.

## Definition of Done

See the "Definition of Done" section in `AGENTS.md`. Before finishing, confirm
`AGENTS.md` and `SKILL.md` remain accurate for this repository.
