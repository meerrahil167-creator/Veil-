# Veil — Anonymous Privacy Browser for Android

An anonymous privacy browser inspired by Tor Browser, reverse-engineered for Android with Jetpack Compose.
Every site is isolated with its own 3-relay Tor circuit, all requests are routed securely, nothing is kept to disk, and users can inspect circuits, audit privacy leaks, and publish one-click ephemeral onion services.

## Core Features

- **Tor Network Routing & Circuit Isolation**:
  - Per-site 3-hop Tor circuit (Guard ➡️ Middle ➡️ Exit) isolation ensuring websites cannot cross-link user sessions by IP.
  - Live relay inspector displaying relay nicknames, IP fingerprints, bandwidth, and country flags.
  - New Circuit generation per domain and global New Identity resets.
  - Bridge support (Snowflake, obfs4, meek-azure) to bypass network censorship.

- **Onion Routing Visualizer**:
  - Interactive multi-layer cryptographic peeling simulator matching the desktop visualizer.
  - Real-time packet progression through Guard, Middle, and Exit relays.
  - "Who Sees What" security guarantees breakdown matrix.
  - Live cell traffic event streaming (`CREATE2`, `EXTEND2`, `RELAY_DATA`).

- **Built-in Leak Test**:
  - Automated network isolation audit verifying Tor exit status, per-site exit divergence, and local DNS suppression.
  - 13 comprehensive fingerprint resistance tests: forced UTC timezone, uniform desktop User-Agent spoofing, screen letterboxing, WebRTC stripping, timer quantisation to 16.67 ms, and canvas noise injection.

- **Signed Community Directory & Invites**:
  - Cryptographically verified directory (Ed25519 signature verified, fingerprint `7a1b94e82c5d6f10`).
  - Pre-verified onion sites including The Tor Project, DuckDuckGo Onion, BBC News, and ProPublica.
  - Tor v3 Client Authorization support for joining private `veil-invite:` onion services.

- **One-Click Ephemeral Onion Hosting**:
  - Publish local HTML or Markdown notes directly as v3 Tor hidden services (`.onion`).
  - Optional invite-only protection generating x25519 client authorization keys.

- **Amnesic Privacy & Panic Button**:
  - Amnesic architecture: nothing is written to disk; bookmarks and history remain strictly in-memory.
  - Instant nuclear **PANIC** button: immediately closes all open tabs, wipes session memory, purges cookies/caches, and destroys hosted services.
  - Security levels: **Standard**, **Safer**, and **Safest** (complete JavaScript and WebGL shutdown).
  - Built-in Ad & Tracker blocker using EasyList and EasyPrivacy filtering rules.

## Tech Stack

- **Target Runtime**: Android (API 36, minSdk 24)
- **Language**: Kotlin 2.1
- **UI Framework**: Jetpack Compose with Material Design 3
- **Design & Typography**: Dark cyberpunk aesthetic with bundled Creepster display font and Monospace accents
- **Build System**: Gradle 9.3.1 (Kotlin DSL) with Android Gradle Plugin
