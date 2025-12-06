package org.example

import org.example.utils.resourceLines
import kotlin.time.measureTime

suspend fun runPart(fileName: String) {
    resourceLines(fileName)
}

suspend fun main() {
    measureTime {
        runPart("dayX/example.txt")
    }.also {
        println("Execution time: $it")
    }
}
