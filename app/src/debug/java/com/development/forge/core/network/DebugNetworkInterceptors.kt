package com.development.forge.core.network

import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.Interceptor

/**
 * Debug-variant network interceptors, installed on the Ktor OkHttp engine by
 * `NetworkModule.provideHttpClient`.
 *
 * [OkHttpProfilerInterceptor] emits every request/response to logcat under the
 * `OKPRFL` tag, in the format the Android Studio **OkHttp Profiler** plugin
 * (LocalHost / `itkacher.okhttpprofiler`) parses. Install that plugin to get a
 * request list with headers, bodies and timings.
 *
 * There is a matching `src/release` copy of this file that returns an empty
 * list. That split is deliberate: `okhttpprofiler` is a `debugImplementation`
 * dependency, so it is absent from the release compile classpath entirely, and
 * `NetworkModule` (which lives in `src/main` and is compiled for every variant)
 * must not name it directly.
 */
fun debugNetworkInterceptors(): List<Interceptor> = listOf(OkHttpProfilerInterceptor())
