package com.example.lib

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    joinAll(
        async { coroutine(1, 500) },
        async { coroutine(2, 300) }
    )
    /*
    *  println("task $task started") for 1
    * suspend from 1
    *  println("task $task started") from 2
    * delay(delay) from 1
    * suspend
    * delay(delay) from 2*/
}

private suspend fun coroutine(task: Int, delay: Long) {
    println("task $task started on ${Thread.currentThread().name}")
    delay(delay)
    println("task $task finished on ${Thread.currentThread().name}")
}