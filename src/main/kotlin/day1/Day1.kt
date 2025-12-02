package org.example.day1

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import org.example.utils.resourceLines

@JvmInline
value class Dial(
    val position: Position
) {
    fun rotate(rotation: Rotation): Dial {
        val newPosition = when (rotation) {
            is Rotation.Left -> (position.value - rotation.clicks + 100) % 100
            is Rotation.Right -> (position.value + rotation.clicks) % 100
        }
        return Dial(Position(newPosition))
    }
    companion object {
        val START = Dial(Position(50))
    }
    @JvmInline
    value class Position(val value: Int)
}

sealed interface Rotation {
    @JvmInline
    value class Left(val clicks: Int) : Rotation
    @JvmInline
    value class Right(val clicks: Int) : Rotation
    companion object {
        fun fromLine(line: String): Rotation = when (line.first()) {
            'L' -> Left(line.substring(1).toInt())
            'R' -> Right(line.substring(1).toInt())
            else -> throw IllegalArgumentException("Invalid rotation line: $line")
        }
    }
    fun split(): List<Rotation> {
        return when (this) {
            is Left -> List(this.clicks) { Left(1) }
            is Right -> List(this.clicks) { Right(1) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DialRunner(
    private val rotations: Flow<Rotation>,
) {
    suspend fun runRotations(splitByClicks: Boolean = false, debug: Boolean = false): DialResult {
        var timesLeftPointingAtZero = 0
        var dial = Dial.START
        
        val rotationFlow = if (splitByClicks) {
            rotations.flatMapConcat { it.split().asFlow() }
        } else {
            rotations
        }
        
        rotationFlow.collect { rotation ->
            dial = dial.rotate(rotation)
            if (dial.position.value == 0) timesLeftPointingAtZero++
            if (debug) {
                println("Rotated $rotation to position ${dial.position.value}")
            }
        }
        
        return DialResult(
            finalDial = dial,
            timesLeftPointingAtZero = timesLeftPointingAtZero,
        )
    }

    data class DialResult(
        val finalDial: Dial,
        val timesLeftPointingAtZero: Int,
    )

}

suspend fun runPart(fileName: String, splitByClicks: Boolean = false, debug: Boolean = false) {
    val dialRunner = DialRunner(resourceLines(fileName).map { Rotation.fromLine(it) })
    val result = dialRunner.runRotations(splitByClicks, debug)
    println("Final dial position: $result")
}

suspend fun main() {


    runPart("day1/example.txt", debug = true)
    runPart("day1/part1.txt")
    runPart("day1/part2.txt", splitByClicks = true)
}
