@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.day4

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.utils.resourceLines
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTime

data class Position(
    val x: Int,
    val y: Int,
) {
    fun neighbors(): List<Position> {
        return listOf(
            Position(x - 1, y - 1),
            Position(x - 1, y),
            Position(x - 1, y + 1),

            Position(x + 1, y - 1),
            Position(x + 1, y),
            Position(x + 1, y + 1),


            Position(x, y - 1),
            Position(x, y + 1),
        )
    }
}

interface MapElement {
    val x: Int
    val y: Int

    data class Empty(
        override val x: Int,
        override val y: Int
    ) : MapElement
    data class PaperRoll(
        override val x: Int,
        override val y: Int,
    ) : MapElement
}

data class MapRow(
    val elements: Flow<MapElement>
)

data class Grid(
    val rows: Flow<MapRow>
) {
    suspend fun howManyRollsCanBeRemoved(singleIteration: Boolean = true): Int {
        var totalRemoved = 0
        while (true) {
            val adjacentRolls = calculateAdjacentRolls()
            val toRemove = adjacentRolls.keys
            if (toRemove.isEmpty()) {
                break
            }
            totalRemoved += toRemove.size
            if (singleIteration) {
                break
            }
            val toRemoveSet = toRemove.toSet()
            val newRows = rows.map { row ->
                MapRow(
                    row.elements.filter { element ->
                        val position = Position(element.x, element.y)
                        position !in toRemoveSet
                    }
                )
            }
            return Grid(newRows).howManyRollsCanBeRemoved(singleIteration).let {
                totalRemoved + it
            }
        }
        return totalRemoved
    }

    private val mutexes = ConcurrentHashMap<Position, Mutex>()
    suspend fun calculateAdjacentRolls(): Map<Position, Int> {
        val map = ConcurrentHashMap<Position, Int>()
        val paperRolls = ConcurrentHashMap<Position, Boolean>()
        coroutineScope {
            rows.collect { row -> // this is collected too many times :|
                row.elements.filterIsInstance<MapElement.PaperRoll>().collect { element ->
                    val position = Position(element.x, element.y)
                    launch {
                        val mutex = mutexes.getOrPut(position) { Mutex() }
                        mutex.withLock {
                            map.putIfAbsent(position, 0)
                        }
                    }
                    launch {
                        paperRolls[Position(element.x, element.y)] = true
                    }
                    launch {
                        position.neighbors().forEach { neighbor ->
                            val mutex = mutexes.getOrPut(neighbor) { Mutex() }
                            mutex.withLock {
                                map[neighbor] = (map[neighbor] ?: 0) + 1
                            }
                        }
                    }
                }
            }
        }
        return map.filterKeys { paperRolls[it] == true }.filter {
            it.value < 4
        }
    }
}

suspend fun runPart(fileName: String) {
    val map = Grid(
        resourceLines(fileName).withIndex()
        .flatMapConcat { line ->
            println("Processing line: $line")
            flow {
                emit(
                    MapRow(
                        line.value.asSequence().mapIndexed { index, ch ->
                            when (ch) {
                                '.' -> MapElement.Empty(x = index, y = line.index)
                                '@' -> MapElement.PaperRoll(x = index, y = line.index)
                                else -> throw IllegalArgumentException("Unknown map element: $ch")
                            }
                        }.asFlow()
                    )
                )
            }
        }
    )

    println("Total rolls that can be removed: ${map.howManyRollsCanBeRemoved(singleIteration = false)}")

}

suspend fun main() {
    measureTime {
//        runPart("day4/example.txt")
        runPart("day4/input.txt")
    }.also {
        println("Execution time: $it")
    }
}
