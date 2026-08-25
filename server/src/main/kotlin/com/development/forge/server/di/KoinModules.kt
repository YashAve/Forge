package com.development.forge.server.di

import com.development.forge.server.auth.AuthRepository
import com.development.forge.server.auth.GoogleTokenVerifier
import com.development.forge.server.auth.JwtService
import com.development.forge.server.db.MongoDatabase
import com.development.forge.server.task.TaskRepository
import org.koin.dsl.module

/**
 * Koin wiring. Keeps the project's existing DI choice.
 *
 * Everything is a `single`: one Mongo client (the driver pools connections
 * internally, so creating more would be wasteful), one JWT service, and stateless
 * repositories.
 */
val appModule = module {
    single { MongoDatabase() }
    single { JwtService() }
    single { GoogleTokenVerifier() }
    single { AuthRepository(get()) }
    single { TaskRepository(get()) }
}
