package com.development.forge.core.network

import okhttp3.Interceptor

/**
 * Release counterpart of the debug-variant file of the same name.
 *
 * No debugging interceptors ship in a release build, so this returns nothing.
 * Keeping the function here — rather than guarding on `BuildConfig.DEBUG` in
 * `NetworkModule` — is what allows the `okhttpprofiler` dependency to stay a
 * `debugImplementation`: release code never names the library, so it is not on
 * the release compile classpath at all.
 */
fun debugNetworkInterceptors(): List<Interceptor> = emptyList()
