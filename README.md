# Open Archive

Open Archive is a native Android rewrite of the Archive personal-life-record prototype. The app is written in Kotlin and Jetpack Compose; it does **not** use WebView. The supplied single-file HTML prototype is the UI/interaction source of truth; the parity contract is captured in `docs/UI_PARITY.md`.

## Current scope

- Timeline home + side drawer
- Compose flow with auto-growing text, up to 9 Photo Picker images, persistent draft
- Small location/weather/mood metadata widgets
  - Location: bottom sheet with search/custom input + nearby POI list
  - Weather: anchored compact floating card
  - Mood: anchored five-stop slider (低落 / 平静 / 悠闲 / 开心 / 活力)
- Entry detail with two-line metadata and a borderless Share/Delete overflow menu
- Comments and replies
- Search across text/location/weather/mood/date
- Album and 7-day statistics
- DiceBear Glyphs avatars
- Room persistence + DataStore preferences/draft
- Android Keystore AES-GCM encryption for provider API keys
- Multiple AI providers, models and one global default model
- OpenAI Chat Completions / OpenAI Responses-compatible / Anthropic-compatible streaming adapters
- Achi uses the selected default model and recent Archive context

## UI contract

The Android UI intentionally does not adopt default Material visual styling. The prototype's white background, near-black text, generous spacing, no visible outlines, very light floating shadows, small metadata controls and restrained motion are treated as product requirements. See `docs/UI_PARITY.md`.

## Build

Requirements:

- JDK 17+
- Android SDK platform 37
- Gradle 9.5

Open the repository in a current Android Studio, or run:

```bash
./gradlew assembleDebug
```

The included `gradlew` bootstrap downloads Gradle 9.5 when a local Gradle installation is not available.

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

`android-ci.yml` runs on pushes/PRs to `main` and manual dispatch. It installs Android platform 37 + Gradle 9.5, then runs lint, JVM tests, Android-test compilation, and builds the Debug APK. `open-archive-debug` is uploaded as an Actions artifact.

`release.yml` runs for `v*` tags (and manually), builds Debug + Release APKs and creates a GitHub Release for version tags. Without signing secrets it publishes an installable Debug APK plus the unsigned Release APK. With the following repository secrets it additionally signs and verifies the Release APK:

```text
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

Never commit API keys, keystores, `local.properties`, or signing passwords.

## Data and privacy

Archive entries, comments, image URI references, provider configuration and preferences are stored locally. Provider API keys are encrypted with an AES-GCM key generated in Android Keystore before ciphertext is written to Room. Achi sends the current request and a bounded recent-record context only when the user explicitly sends a message.

## Provider formats

A provider has a name, format, Base URL, encrypted API key and configured models. Supported request adapters:

- OpenAI-compatible Chat Completions (`/chat/completions`)
- OpenAI-compatible Responses (`/responses`)
- Anthropic-compatible Messages (`/messages`)

Model discovery uses `<baseUrl>/models`.

## Reference prototype

Do not casually redesign Compose, Detail or Provider screens. If product design changes, update the prototype and `docs/UI_PARITY.md` together.

## License

MIT
