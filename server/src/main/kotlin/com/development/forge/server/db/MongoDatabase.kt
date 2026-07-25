package com.development.forge.server.db

import com.development.forge.server.config.ServerConfig
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.slf4j.LoggerFactory
import java.io.Closeable

/**
 * Owns the single [MongoClient] for the process and hands out typed collections.
 *
 * The driver pools connections internally, so exactly one client is created at
 * startup and closed on shutdown (wired in `Application.kt`). Nothing else in the
 * server constructs a client.
 */
class MongoDatabase(
    uri: String = ServerConfig.mongoUri,
    databaseName: String = ServerConfig.mongoDatabase
) : Closeable {

    private val log = LoggerFactory.getLogger(MongoDatabase::class.java)

    private val client: MongoClient = MongoClient.create(
        MongoClientSettings.builder()
            .applyConnectionString(com.mongodb.ConnectionString(uri))
            .build()
    )

    private val database = client.getDatabase(databaseName)

    /** Typed handles. The data-class codec maps these to [UserDocument] / [TaskDocument]. */
    val users: MongoCollection<UserDocument> = database.getCollection(USERS)
    val tasks: MongoCollection<TaskDocument> = database.getCollection(TASKS)

    /**
     * Creates the indexes the API depends on. Idempotent — Mongo ignores a
     * `createIndex` for an index that already exists, so this is safe every boot.
     *
     *  - users.email  UNIQUE → makes duplicate registration a database-level
     *    guarantee, not just an application check. The 409 path in
     *    `AuthRepository.createUser` relies on this to close the race where two
     *    simultaneous registrations both pass the "does this email exist?" read.
     *  - tasks.userId → every task query filters by owner, so this is *the* index
     *    that keeps user-scoped reads fast.
     */
    suspend fun ensureIndexes() {
        users.createIndex(Indexes.ascending(UserDocument::email.name), IndexOptions().unique(true))
        tasks.createIndex(Indexes.ascending(TaskDocument::userId.name))
        log.info("Mongo indexes ensured on '{}' ({}, {})", database.name, USERS, TASKS)
    }

    override fun close() = client.close()

    private companion object {
        const val USERS = "users"
        const val TASKS = "tasks"
    }
}
