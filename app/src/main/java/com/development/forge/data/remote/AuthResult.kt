package com.development.forge.data.remote

import com.development.forge.feature.auth.contract.AuthError

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}