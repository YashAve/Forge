package com.example.lib

fun main() {
    println("main started")
    routine(1, 500)
    routine(2, 300)
    println("main ended")
}

private fun routine(task: Int, delay: Long) {
    println("routine $task started")
    Thread.sleep(delay)
    println("routine $task finished")
}