@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.day2

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import org.example.utils.resourceLines
import kotlin.time.measureTime

class InvalidIdDetector(
    private val ranges: Flow<LongRange>,
    private val strategy: IdValidator
) {
    suspend fun sumInvalidIds(): Long =
        detectInvalidIds()
            .fold(0L) { acc, id -> acc + id }

    fun detectInvalidIds(): Flow<Long> =
        ranges.flatMapMerge { range ->
            flow {
                for (id in range) {
                    if (!strategy.isValid(id)) {
                        emit(id)
                    }
                }
            }
        }
}

fun part1IsValid(id: Long) = !detectedRepetition(id, 2, true)

fun part2IsValid(id: Long) = !detectedRepetition(id, 2, false)

fun detectedRepetition(id: Long, minimumOccurrences: Int, limitToMinimum: Boolean): Boolean {
    val stringId = id.toString()
    val end = if (limitToMinimum) minimumOccurrences else stringId.length
    for (occurrences in minimumOccurrences..end) {
        if (stringId.length % occurrences == 0) {
            val segmentLength = stringId.length / occurrences
            val segment = stringId.take(segmentLength)
            val repeatedSegment = segment.repeat(occurrences)
            if (repeatedSegment == stringId) {
                return true
            }
        } else {
            continue
        }
    }
    return false
}

enum class IdValidator(
    val isValid: (id: Long) -> Boolean
) {
    PART1(::part1IsValid),
    PART2(::part2IsValid),
}

suspend fun runPart(fileName: String, strategy: IdValidator) {
    val detector = InvalidIdDetector(
        resourceLines(fileName)
            .flatMapConcat { line ->
                flow {
                    line.split(",").forEach { range ->
                        val parts = range.split("-").map { it.toLong() }
                        val range = parts[0]..parts[1]
                        emit(range)
                    }
                }
            },
        strategy
    )
    val result = detector.sumInvalidIds()
    println("Sum of invalid IDs: $result")
}

suspend fun main() {
    measureTime {
        runPart("day2/example.txt", IdValidator.PART1)
        runPart("day2/part1.txt", IdValidator.PART1)
        runPart("day2/part2.txt", IdValidator.PART2)
    }.also {
        println("Execution time: $it")
    }
}
