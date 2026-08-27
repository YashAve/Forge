package com.development.forge.di

import com.development.forge.BuildConfig
import com.development.forge.core.network.debugNetworkInterceptors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json
    ): HttpClient =
        HttpClient(OkHttp) {
            engine {
                // Empty in release — see core/network/DebugNetworkInterceptors.kt in src/debug
                // and src/release. Debug builds get the OkHttp Profiler interceptor here.
                debugNetworkInterceptors().forEach(::addInterceptor)
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(DefaultRequest) {
                url(BuildConfig.BASE_URL)
            }
        }
}
