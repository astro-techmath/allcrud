# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.1] - 2026-08-17

### Changed
- Upgraded build tooling to Gradle 9.7.0

### Security
- Hardened Gradle dependency verification metadata for broader coverage
  against supply-chain tampering

## [0.2.0] - 2026-08-06

### Changed
- Upgraded to Spring Boot 4.1.0 / Spring Framework 7 (from 3.5.6, whose OSS
  support line reached end-of-life)

### Fixed
- 3 critical Apache Tomcat CVEs (HTTP/2 header validation, Digest
  authenticator bypass, security constraints not applied), resolved via the
  Spring Boot upgrade
- Removed obsolete jackson-datatype-jsr310 dependency, eliminating a
  duplicate Jackson 2 tree on the classpath (4 Dependabot alerts)
- Upgraded rest-assured spring-mock-mvc to 6.0.0 for real Jackson 3 support
- AbstractCompositeIdConverter now compiles and works correctly under
  Jackson 3 (previously only compiled by accident via a stray Jackson 2
  dependency, never exercised by a real test)
- Added missing junit-platform-launcher dependency

### Added
- CI: automated build/test on every push/PR (GitHub Actions)
- CI: SonarQube Cloud static analysis
- CI: JaCoCo test coverage reporting (100% line coverage on core testable
  classes)
- CodeQL and secret scanning enabled
- Dependabot grouped dependency updates
- Signed commits required on protected branches

## [0.1.0-beta] - 2025-12-01

### Added
- Initial beta release
- Generic CRUD operations with Spring Boot
- Support for any ID type (Long, UUID, String, composite keys)
- Generic `CrudService`, `CrudController`, and `Converter`
- Pagination and filtering with VO as query parameters
- Reusable test fixtures for service and integration tests
- Bean Validation integration
- MIT license and full documentation

### Known Issues
- Beta release - API may change in future versions
- Community feedback welcomed