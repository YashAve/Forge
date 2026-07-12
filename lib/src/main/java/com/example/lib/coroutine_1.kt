package com.example.lib

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("main started")
    joinAll(
        async { coroutine(1, 500) },
        async { coroutine(2, 300) }
    )
    println("main ended")
}

private suspend fun coroutine(task: Int, delay: Long) {
    println("coroutine $task started")
    delay(delay)
    println("coroutine $task finished")
}