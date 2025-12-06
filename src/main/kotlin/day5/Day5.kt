@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.day5

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import org.example.utils.resourceLines
import kotlin.time.measureTime

fun MutableSet<LongRange>.mergeOverlapping(): List<LongRange> {
    val sorted = ArrayDeque(this.sortedWith(compareBy({ it.first }, { it.last })))
    val merged = mutableListOf<LongRange>()
    // 1-4
    // 2-5
    while (sorted.isNotEmpty()) {
        val current = sorted.removeFirst()
        if (sorted.isEmpty()) {
            merged += current
            break
        }
        val next = sorted.first()
        if (current.last >= next.first) {
            // overlapping
            val newRange = current.first..maxOf(current.last, next.last)
            sorted.removeFirst()
            sorted.addFirst(newRange)
        } else {
            merged += current
        }
    }

    return merged
}
suspend fun runPart(fileName: String) {
    val freshIngredients = mutableSetOf<LongRange>()
    val availableFreshIngredients = mutableSetOf<Long>()
    resourceLines(fileName).filter { !it.isBlank() }.collect { line ->
        if (line.contains('-')) {
            val parts = line.split("-")
            val left = parts[0].toLong()
            val right = parts[1].toLong()
            val range = left..right
            freshIngredients += range
        } else {
            val value = line.toLong()
            if (freshIngredients.any { value in it }) {
                availableFreshIngredients += value
            }
        }
    }

    println("Available fresh ingredients count: ${availableFreshIngredients.size}")

    val allFresh = freshIngredients.mergeOverlapping().sumOf {
        it.last - it.first + 1
    }
    println("All fresh ingredients count summed up: $allFresh")
}

suspend fun main() {
    measureTime {
//        runPart("day5/example.txt")
        runPart("day5/input.txt")
    }.also {
        println("Execution time: $it")
    }
}
