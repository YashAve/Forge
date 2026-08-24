import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.development.forge"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.development.forge"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.development.forge.HiltTestRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.46.89.25:9292\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

// `compileJava` is a JVM-plugin convention that AGP does not follow: an Android module
// gets one Java compile task per variant (compileDebugJavaWithJavac, compileRelease...)
// and no plain `compileJava`. Asking for :app:compileJava therefore fails, and confusingly
// so — Gradle falls back to camel-case abbreviation, matches all five variant tasks, and
// reports "task 'compileJava' is ambiguous" rather than "no such task".
//
// Registering it for real fixes two things:
//   1. `./gradlew :app:compileJava` resolves exactly (an exact name beats abbreviation
//      matching) instead of erroring.
//   2. `./gradlew compileJava` from the root now covers :app too. Previously it ran only
//      in :lib and :server — the modules that get `compileJava` from java-library — and
//      silently skipped :app, reporting BUILD SUCCESSFUL without ever compiling the app.
//
// Debug is the variant chosen, mirroring what `compileJava` means in :lib/:server (the
// main sources). Depending on the javac task pulls in compileDebugKotlin and kspDebugKotlin
// transitively, so this compiles the module's Kotlin as well despite the Java-flavoured name.
tasks.register("compileJava") {
    group = "build"
    description = "Compiles the debug variant. Alias for the JVM-conventional task name; " +
        "AGP only creates per-variant compile tasks."
    dependsOn("compileDebugJavaWithJavac")
}

hilt {
    // Do Hilt component aggregation inside the KSP processor instead of the separate
    // `hiltJavaCompileDebug` javac task. That javac task reads Kotlin metadata through
    // Dagger's bundled kotlin-metadata-jvm, which maxes out at metadata 2.2.0 and cannot
    // parse Kotlin 2.3.x (metadata 2.3.0). KSP reads metadata via the Kotlin compiler and
    // handles 2.3.0 fine, so this keeps Hilt working on the modern Kotlin toolchain.
    enableAggregatingTask = false
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.ui.unit)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.navigation.compose)

    // Navigation 3 — app-owned back stack rendered by NavDisplay + entryProvider.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    // NOTE: the stable lifecycle-viewmodel-navigation3 (per-NavEntry ViewModel scoping)
    // lives in lifecycle 2.11.0, which requires AGP 9.1.0 + compileSdk 37. Deferred until
    // this project moves to AGP 9 — see docs/CROSS-MACHINE-SETUP.md.
    // @Serializable Nav3 keys (kotlin-serialization plugin is applied above).
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // OkHttp Profiler — logcat-based network inspector for the Android Studio plugin of the
    // same name. Debug-only: the interceptor is registered from src/debug (see
    // core/network/DebugNetworkInterceptors.kt), and src/release returns an empty list, so
    // neither the library nor any reference to it exists in a release build.
    debugImplementation(libs.okhttp.profiler)

    implementation(project(":shared"))

    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.navigation.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}