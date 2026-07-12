package com.example.lib

import kotlin.concurrent.thread

fun main() {
    repeat(1_00_000) {
        thread {
            Thread.sleep(500)
            print(".")
        }
    }
}