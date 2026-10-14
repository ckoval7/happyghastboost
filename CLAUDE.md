# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Happy Ghast Speed Boost: a NeoForge mod for Minecraft 1.21.8 (NeoForge 21.8.47, Java 21, Parchment mappings via NeoGradle userdev). Feeding a Happy Ghast "Ghast Kibble" raises a stamina value; while ridden, stamina decays and drives the ghast's flying speed. Must be installed on both client and server. GPLv3.

## Commands

The machine's default `java` is Java 8, which cannot run this build. Point Gradle at JDK 21:

```bash
JAVA_HOME="/c/Program Files/Microsoft/jdk-21.0.8.9-hotspot" ./gradlew build
```

- `./gradlew build` — jar lands in `build/libs/happyghastboost-<mod_version>.jar`
- `./gradlew runClient` / `./gradlew runServer` — dev instances in `runs/client` and `runs/server`
- There are no tests or gametests; verify gameplay changes in `runClient`.

The repo lives under OneDrive; a "Failed to clean up stale outputs" build error is usually a transient sync lock — rerun.

Mod metadata (id, version, authors, description, version ranges) lives in `gradle.properties` and is expanded into `src/main/resources/META-INF/neoforge.mods.toml`. Bump `mod_version` there.

## Architecture

Everything is wired from the `@Mod` constructor in `HappyGhastBoost.java`: config, item and attachment registration, payload registration, and the game-bus event handlers (registered as instances, so `@SubscribeEvent` methods are non-static).

Data flow for stamina:

- **Storage** — `GhastStaminaData` registers two NeoForge data attachments on the ghast entity (`stamina`, `last_fed_time`), serialized with codecs so they persist with the entity. All writes go through `setStamina`, which clamps to `maxStamina`.
- **Gain** — `FeedingHandler` (right-click with kibble) adds stamina, records the feed time, and syncs.
- **Loss** — `StaminaDecayHandler` runs once per second on server tick, decays stamina for every ghast with a riding player, skipping a 3-second grace period after feeding.
- **Effect** — `GhastSpeedHandler` sets the `FLYING_SPEED` attribute base value every tick: the stamina curve when a player rides, 0.05 otherwise.
- **Sync** — the server is authoritative. `StaminaSyncPayload` (entity id + stamina) is sent to clients; the client writes it into the same attachment, which `StaminaBarOverlay` reads to draw a 10-icon HUD bar.
- **Attraction** — `GhastAttractionHandler` adds `FlyingTemptGoal` to Happy Ghasts on `EntityJoinLevelEvent`.

Client/server separation: anything touching `net.minecraft.client` must stay under `client/`. The payload is registered without a handler in common code (`playToClient(TYPE, CODEC)`); its client handler is registered in `ClientSetup` via `RegisterClientPayloadHandlersEvent`. Keep it that way so dedicated servers never load client classes.

`com.happyghastboost.common` holds logic with **no NeoForge imports** (`StaminaMath`, `FlyingTemptGoal`). This is deliberate: the code is meant to be shared with the Fabric side and across MC versions. Keep loader APIs (config values, `DeferredItem`, attachments) out of it and pass values in instead.

Config is a COMMON `ModConfigSpec` in `StaminaConfig` (`config/happyghastboost-common.toml` in the run dir).

## Repository and roadmap

- The GitHub default branch is `1.21.8`. There is also a **tag** named `1.21.8`, so use `refs/heads/1.21.8` when the branch is meant. Do not rename or delete the remote branch or the tag.
- `fabric-port` is an orphan branch holding a snapshot of an earlier standalone Fabric (Loom + Cloth Config) port. It is kept as reference, not built from this tree.
- Planned next: one branch building every MC version from 1.21.8 to current for both NeoForge and Fabric using Stonecutter. That rewrite replaces the MDK template build files; `TEMPLATE_LICENSE.txt` (the NeoForge MDK MIT notice) stays until then.
