# Technical notes — build.gradle.kts

Forensic findings and non-obvious internal rationale, not decisions about
this project's own architecture.

## Coverage exclusions — 3 abstract extension points with no logic of their own

`CrudController`/`CrudService` are pure delegation to Spring MVC/JPA with
zero decision logic of their own; the concrete type parameters and any real
behavior only exist once a consumer writes a concrete subclass -
`AbstractGlobalExceptionHandler` is the same shape, its `@ExceptionHandler`
methods only do something once wired into a real Spring MVC
exception-handling flow. They're only ever exercised via consumers' own
test suites (e.g. allcrud-generator's external smoke tests, which extend
them with real entities/controllers) - never inside this repo. A fake
subclass here just to move the coverage number would test nothing real (no
logic of this class's own would be exercised, only Spring's plumbing) -
confirmed by reading each class, not assumed from the JaCoCo report alone.

## java:S119 suppression — VO/ID/DTO generic names are intentional

Multi-letter type parameter names used deliberately throughout this
module's public API (`CrudController<T, VO, ID>`, `Converter<T, VO, ID>`,
`AbstractEntity<ID>`, etc.) and documented as intentional in the README's
Design Decisions section: readability in a public API matters more here
than the single-letter convention the rule enforces.
`sonar.issue.ignore.multicriteria` (not a per-line `NOSONAR`) because this
is 11 occurrences across 8 files, all the same rule, all the same
reasoning - a single centralized suppression is more maintainable than
repeating the same comment 11 times, and covers any future class following
the same VO/ID/DTO convention too.

## extendsFrom chaining avoids redeclaring dependencies across configurations

Avoids redeclaring the same dependency under multiple configurations
(`implementation` + `testFixturesImplementation` + `testImplementation`)
just to make it visible in each scope - each dependency is declared exactly
once, in its lowest/most specific scope.
