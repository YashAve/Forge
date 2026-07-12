package com.example.lib

import kotlin.concurrent.thread

fun main() {
    println("main started")
    threadRoutine(1, 500)
    threadRoutine(2, 300)
    Thread.sleep(1000)
    println("main ended")
}

private fun threadRoutine(task: Int, delay: Long) {
    thread {
        println("routine $task started")
        Thread.sleep(delay)
        println("routine $task finished")
    }
}