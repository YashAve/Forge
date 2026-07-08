# Cross-machine setup notes (desktop ↔ personal laptop)

Forge is built on two machines that run **different Android Studio versions** and have
different SDK/JBR locations. This note records what each machine needs so a `git pull`
from one never breaks the build on the other.

Nothing machine-specific (absolute paths, JDK homes) should be committed into
`gradle.properties` — keep those in each machine's own `~/.gradle/gradle.properties`.

## Toolchain the repo currently targets

Pinned in `gradle/libs.versions.toml` (committed, same on every machine):

| Tool | Version | Notes |
|------|---------|-------|
| Android Gradle Plugin | 8.13.2 | **Not** AGP 9 — see the Nav3 note below |
| Gradle wrapper | 8.13 | `gradle/wrapper/gradle-wrapper.properties` |
| compileSdk / targetSdk | 36 | SDK Platform **36** must be installed |
| minSdk | 24 | |
| Kotlin | 2.3.21 | uses the `compilerOptions` DSL (not the removed `kotlinOptions`) |
| KSP | 2.3.9 | Hilt + Room annotation processing |
| Compose BOM | 2026.06.00 | |
| lifecycle | 2.9.0 | highest that works on AGP 8.x / compileSdk 36 |
| Navigation 3 | 1.1.4 | `navigation3-runtime` + `navigation3-ui` |
| Hilt | 2.57.2 | needs `hilt { enableAggregatingTask = false }` — see gotcha below |

## Per-machine setup checklist (do this once on the laptop)

1. **JDK / JBR** — Gradle must run on Android Studio's bundled JBR (the distro OpenJDK
   SIGSEGVs in R8/D8 during dexing). Put the laptop's own path in
   `~/.gradle/gradle.properties` (untracked):
   ```properties
   org.gradle.java.home=/path/to/laptop/android-studio/jbr
   ```
   Find it with: `find ~ /opt -maxdepth 5 -type d -name jbr 2>/dev/null`
   (On this desktop it is `/home/yash/softwares/android_panda/jbr`.)
2. **SDK Platform 36** — install via SDK Manager (`platforms;android-36`). compileSdk is 36.
3. **Android Studio version** — any Studio that supports **AGP 8.13.2** is fine (2025-era
   builds). The laptop does **not** need the newest Studio, SDK Platform 37, or AGP 9 for
   the current setup.

## Navigation 3 — why we're on AGP 8.13, not AGP 9

Navigation 3 *core* (`navigation3-runtime` / `navigation3-ui` 1.1.4) works fine on AGP
8.13.2 / compileSdk 36. The **stable ViewModel add-on** `lifecycle-viewmodel-navigation3`
only exists in **lifecycle 2.11.0**, which hard-requires **AGP 9.1.0+ and compileSdk 37**
(and lifecycle 2.10.0+ requires AGP 9.2.0). Forcing that here would make the laptop's
older Android Studio unable to build the project, so it is **deferred**.

Effect today: `ForgeNavHost` uses `NavDisplay` + `rememberSaveableStateHolderNavEntryDecorator()`
(saveable UI state per entry). ViewModels still work; they just aren't yet scoped per
`NavEntry` via `rememberViewModelStoreNavEntryDecorator()`.

### When both machines are ready to move to AGP 9

1. Install **SDK Platform 37** on both machines (`platforms;android-37`).
2. Update Android Studio on both to a version that bundles/ supports **AGP 9.1.0+**.
3. In `gradle/libs.versions.toml`: `agp → 9.1.0+`, `lifecycle → 2.11.0`, `compileSdk → 37`;
   bump the Gradle wrapper to the version AGP 9 requires.
4. Re-add `androidx-lifecycle-viewmodel-navigation3` (version.ref = lifecycle) and wire
   `implementation(libs.androidx.lifecycle.viewmodel.navigation3)` in `app/build.gradle.kts`.
5. Add `rememberViewModelStoreNavEntryDecorator()` back into the `entryDecorators` list in
   `ForgeNavHost` (remove the TODO there).

## Gotcha: Hilt + Kotlin 2.3

Hilt's aggregating build step (`hiltJavaCompileDebug`) reads Kotlin class metadata through
Dagger's bundled `kotlin-metadata-jvm`, which (as of Hilt 2.57.2) maxes out at metadata
**2.2.0** and throws on Kotlin 2.3.x's metadata **2.3.0**:

```
[Hilt] Provided Metadata instance has version 2.3.0, while maximum supported version is 2.2.0.
```

Fix (already applied in `app/build.gradle.kts`):

```kotlin
hilt {
    enableAggregatingTask = false
}
```

This moves component aggregation into the KSP processor (which reads metadata via the
Kotlin compiler and understands 2.3.0), so the failing javac task never runs. Note this
error only appears in a **full build** (`assembleDebug`), not `compileDebugKotlin` — always
verify with `assembleDebug`.
