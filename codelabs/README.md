# Compose codelabs — original vs. my code

A study area for learning Jetpack Compose with the official
[android/codelab-android-compose](https://github.com/android/codelab-android-compose)
codelabs. **This is not built** — there's no Gradle module here and nothing is
wired into the Forge build. It's just source files to read and compare against.

```
codelabs/
  original/   reference solutions (the upstream codelab source, read-only)
  mine/       your own code — empty for now, you'll add it as you go
```

Each codelab has a matching folder name on both sides, e.g.:

- `original/BasicsCodelab/`  ← the finished reference
- `mine/BasicsCodelab/`      ← write your version here, then diff the two

## Codelabs included

| Codelab | Official walkthrough |
| --- | --- |
| BasicsCodelab | https://developer.android.com/codelabs/jetpack-compose-basics |
| BasicStateCodelab | https://developer.android.com/codelabs/jetpack-compose-state |
| BasicLayoutsCodelab | https://developer.android.com/codelabs/jetpack-compose-layouts |
| ThemingCodelab | https://developer.android.com/codelabs/jetpack-compose-theming |
| AnimationCodelab | https://developer.android.com/codelabs/jetpack-compose-animation |
| NavigationCodelab | https://developer.android.com/codelabs/jetpack-compose-navigation |
| AdvancedStateAndSideEffectsCodelab | https://developer.android.com/codelabs/jetpack-compose-advanced-state-side-effects |
| AccessibilityCodelab | https://developer.android.com/codelabs/jetpack-compose-accessibility |
| PerformanceCodelab | https://developer.android.com/codelabs/jetpack-compose-performance |
| TestingCodelab | https://developer.android.com/codelabs/jetpack-compose-testing |
| MigrationCodelab | https://developer.android.com/codelabs/jetpack-compose-migration |
| AdaptiveUiCodelab | https://developer.android.com/codelabs/jetpack-compose-adaptive-ui |
| ThemingCodelabM2 | (Material 2 theming, kept for comparison) |

## Suggested workflow

1. Open the official walkthrough for a codelab and write your attempt under
   `mine/<CodelabName>/`.
2. Compare against the reference, e.g.:
   ```bash
   diff -ru codelabs/original/BasicsCodelab/app/src codelabs/mine/BasicsCodelab/app/src
   ```

Source: https://github.com/android/codelab-android-compose (Apache 2.0). The
`original/` tree keeps each codelab's own `build.gradle`/`README.md` for
reference; gradle wrappers, build outputs and doc screenshots were stripped.
