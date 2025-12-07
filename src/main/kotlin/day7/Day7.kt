package org.example

import kotlinx.coroutines.flow.collectIndexed
import org.example.utils.resourceLines
import kotlin.time.measureTime

inline fun String.indexesOf(predicate: (Char) -> Boolean) = mapIndexedNotNull{ index, elem -> index.takeIf{ predicate(elem) } }
fun MutableMap<Int, Long>.increment(key: Int, value: Long = 1L) = this.merge(key, value, Long::plus)

suspend fun runPart(fileName: String) {
    val beams: MutableSet<Int> = mutableSetOf()
    val beamsPerIndex: MutableMap<Int, Long> = mutableMapOf()
    var splits = 0
    resourceLines(fileName).collectIndexed { lineIndex, line ->
        if (lineIndex == 0) {
            val start = line.indexOfFirst { it == 'S' }
            beams += start
            beamsPerIndex.increment(start)
        } else {
            line.indexesOf { it == '^' }.forEach { colIndex ->
                if (beams.contains(colIndex)) {
                    beams.remove(colIndex)
                    val beamsTraveling = beamsPerIndex[colIndex] ?: 0L
                    beamsPerIndex[colIndex] = 0

                    splits += 1

                    beams += colIndex - 1
                    beamsPerIndex.increment(colIndex - 1, beamsTraveling)

                    beams += colIndex + 1
                    beamsPerIndex.increment(colIndex + 1, beamsTraveling)
                }
            }
        }

    }

    println("Number of splits: $splits")
    println("Timelines : ${beamsPerIndex.values.sum()}")
}

suspend fun main() {
    measureTime {
        runPart("day7/input.txt")
//        runPart("day7/example.txt")
    }.also {
        println("Execution time: $it")
    }
}
