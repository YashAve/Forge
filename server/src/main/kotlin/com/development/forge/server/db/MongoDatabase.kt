package com.development.forge.server.db

import com.development.forge.server.config.ServerConfig
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
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
     *  - users.resetToken UNIQUE, PARTIAL → the reset code is only 40 bits (see
     *    `auth/ResetTokens.kt`), so two live codes coinciding is improbable rather
     *    than impossible. `findByResetToken` matches on the code alone, so a
     *    collision would let one account's code reset another account's password.
     *    This index makes that a duplicate-key error that `createResetToken`
     *    retries. **Partial** rather than plain unique because `updatePassword`
     *    *unsets* the field: without the filter, every account with no reset in
     *    flight would collide on a missing value.
     *  - tasks.userId → every task query filters by owner, so this is *the* index
     *    that keeps user-scoped reads fast.
     */
    suspend fun ensureIndexes() {
        users.createIndex(Indexes.ascending(UserDocument::email.name), IndexOptions().unique(true))
        users.createIndex(
            Indexes.ascending(UserDocument::resetToken.name),
            IndexOptions()
                .unique(true)
                .partialFilterExpression(Filters.exists(UserDocument::resetToken.name, true))
        )
        tasks.createIndex(Indexes.ascending(TaskDocument::userId.name))
        log.info("Mongo indexes ensured on '{}' ({}, {})", database.name, USERS, TASKS)
    }

    override fun close() = client.close()

    private companion object {
        const val USERS = "users"
        const val TASKS = "tasks"
    }
}
