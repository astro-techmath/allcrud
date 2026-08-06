# Agent Instructions for Allcrud

This file guides AI coding assistants (Claude Code, Cursor, Copilot, etc.)
working in this repository. It captures durable project discipline, not
session-specific state.

## Core principles

- **Verify empirically before assuming.** Don't assume how a library,
  framework, or tool behaves — read its source or test it directly. Several
  important decisions in this project changed direction only after this kind
  of verification. This applies to Spring, Hibernate, Gradle/Maven internals,
  and any third-party dependency.

- **Real validation over assumption, for anything with runtime implications.**
  A change to bean registration, serialization, dependency injection, or any
  other Spring-context-dependent behavior needs to be validated against a real
  Spring context (an actual test loading the application, or a real consumer
  project), not just unit tests with mocks or reflection-only assertions.
  Compile-only success does not prove runtime correctness.

- **Fail fast, with a clear message.** Prefer throwing a clear exception over
  silently ignoring invalid input, falling back unexpectedly, or producing
  output that "sort of" works. If something is misconfigured or unsupported,
  say so explicitly at the earliest point possible.

- **Hardcode before generalizing.** Don't add configuration options,
  abstraction layers, or parameterization for a use case that doesn't exist
  yet. Prove a concrete case works first; generalize only when a second real
  need appears. Avoid speculative flexibility (YAGNI).

- **SOLID, Clean Code, DRY, KISS.** Favor small, single-responsibility
  classes and methods. Avoid duplication, but don't abstract prematurely to
  avoid it (see above).

## Code style

- All code comments are in English, with no exceptions.
- Avoid the "it's not X, it's Y" rhetorical construction in comments,
  commit messages, and documentation — state things directly instead.

## Testing

- New behavior needs tests. Runtime-affecting changes need validation with a
  real Spring context or a real external consumer project, not only
  mocked/reflection-based tests — each contributor can choose their own way
  to set this up (a throwaway Gradle/Maven project, an existing example repo,
  etc.), but "it compiles" alone is not sufficient proof for anything that
  touches bean wiring, serialization, or request handling.