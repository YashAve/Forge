package com.development.forge.shared.api

import kotlinx.serialization.Serializable

/**
 * The single error shape every failing request returns — no HTML, no stack
 * traces. Produced centrally by the StatusPages plugin so the Android client can
 * deserialize one type for every 4xx/5xx.
 *
 * @param error   stable machine-readable code (e.g. `email_taken`), safe to switch on.
 * @param message human-readable detail, safe to surface in UI.
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)
