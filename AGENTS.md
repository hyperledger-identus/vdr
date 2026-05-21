# AGENTS.md — VDR Agent Guide

## Project Overview

Kotlin JVM library (Kotlin 1.9.23, JVM toolchain 19). Published as Maven artifact `org.hyperledger.identus:vdr`. Verifiable Data Registry — unified API for storing, mutating, retrieving, and removing verifiable data.

## Build Commands

Always use the Gradle wrapper (`./gradlew`), never system Gradle.

```bash
./gradlew build          # Compile + test
./gradlew test           # Run tests only
./gradlew check          # Run all verification tasks (test + other checks)
./gradlew build -x test  # Compile without running tests (used in CI)
```

CI (`ci.yml`) runs `./gradlew build -x test` then `./gradlew test` as separate steps, with Java 19 Temurin.

## Architecture

Pluggable **Drivers** + **URL Managers** pattern. Key interfaces live in `org.hyperledger.identus.vdr.interfaces`:

- **`Driver`** — storage plugin contract: `create`, `update`, `read`, `delete`, `verify`, `storeResultState`
- **`URLManager`** — URL construction and resolution: `create`, `resolve`, `canResolve`
- **`VDR`** — top-level proxy interface
- **`Proof`** — cryptographic proof data class

Implementations:

| Component | Class | File |
|-----------|-------|------|
| VDR proxy | `VDRProxyMultiDrivers` | `proxy/VDRProxyMultiDrivers.kt` |
| URL manager | `BaseUrlManager` | `urlManagers/BaseUrlManager.kt` |
| In-memory driver | `InMemoryDriver` | `drivers/InMemoryDriver.kt` |
| Database driver | `DatabaseDriver` | `drivers/DatabaseDriver.kt` |

`VDRProxyMultiDrivers` selects the correct Driver based on URL query parameters `drid` (driver identifier) and `drf` (driver family). The `drv` (driver version) and `m` (mutability flag) parameters are included in constructed URLs but are **not** used for driver selection. When only one Driver is registered, it is used directly.

Driver family concept: Drivers in the same family share URL interpretation and data handling standards, ensuring consistent results across implementations.

## Testing

JUnit 5 (`useJUnitPlatform()` in `build.gradle.kts`).

**Unit tests** use H2 in-memory database via HikariCP:
- `DatabaseDriverHikariTest` — connects to `jdbc:h2:mem:test-<uuid>`, no Docker needed

**Integration tests** use PostgreSQL via Testcontainers:
- `DatabaseDriverPostgresTest` — spins up `postgres:16-alpine` container, requires **Docker running**
- Uses HikariCP for connection pooling to the containerized PostgreSQL

Other test classes: `InMemoryDriverTest`, `BaseUrlManagerTests`, `VDRProxyMultiDriversTests`.

## Publishing

`release.yml` is a manual workflow dispatch. Publishes to Maven Central via Sonatype staging:
```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository -PreleaseVersion=<version>
```
Required GitHub secrets and their env-var mappings:

| GitHub Secret | Env Var (Gradle) |
|---|---|
| `OSSRH_USERNAME` | `OSSRH_USERNAME` |
| `OSSRH_PASSWORD` | `OSSRH_PASSWORD` |
| `HYP_BOT_GPG_PRIVATE` | `OSSRH_GPG_SECRET_KEY` |
| `HYP_BOT_GPG_PASSWORD` | `OSSRH_GPG_SECRET_KEY_PASSWORD` |

Signing is skipped if GPG keys are not set.

Version is set via `releaseVersion` Gradle property (defaults to `0.1.0`).

## No Codegen

Plain Kotlin/JVM project. No generated sources, no annotation processing, no special env loading.