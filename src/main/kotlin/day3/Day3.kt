@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.day3

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import org.example.utils.resourceLines
import java.math.BigInteger
import kotlin.time.measureTime

data class Battery(
    val voltage: Int,
)

data class Bank(
    val batteries: List<Battery>,
) {
    fun calculateTotalOutputVoltage(batteries: Int): BigInteger {
        var multiplier = 1L
        var sum = BigInteger.ZERO
        val voltages = findTopVoltages(batteries)
        for (voltage in voltages.reversed()) {
            sum += BigInteger.valueOf(voltage * multiplier)
            multiplier *= 10
        }
        return sum
    }

    private fun findTopVoltages(n: Int): List<Int> {
        val result = mutableListOf<Int>()
        val rightStack = ArrayDeque(batteries.takeLast(n).map { it.voltage })
        var leftStack = ArrayDeque(batteries.take(batteries.size - n).map { it.voltage })

        while (rightStack.isNotEmpty()) {
            val right = rightStack.removeFirst()
            val max = findMax(leftStack, right)
            leftStack = ArrayDeque(leftStack.dropWhile { it < max })
            val left = leftStack.removeFirstOrNull()
            addValueToResult(left, result, leftStack, right)
        }

        return result
    }

    private fun addValueToResult(
        left: Int?,
        result: MutableList<Int>,
        leftStack: ArrayDeque<Int>,
        right: Int
    ) {
        if (left != null) {
            result.add(left)
            leftStack.addLast(right)
        } else {
            result.add(right)
        }
    }

    private fun findMax(leftStack: ArrayDeque<Int>, right: Int): Int {
        val leftMax = leftStack.maxOrNull()
        val max = if (leftMax != null && leftMax > right) {
            leftMax
        } else {
            right
        }
        return max
    }
}


suspend fun runPart(fileName: String, batteries: Int = 2) {
    val result = resourceLines(fileName)
        .flatMapConcat { line ->
            println("Processing line: $line")
            flow {
                emit(
                    Bank(line.map { Battery(it.digitToInt()) })
                )
            }
        }.flatMapMerge {
            flow {
                emit(it.calculateTotalOutputVoltage(batteries))
            }
        }.fold(BigInteger.ZERO) { acc, voltage -> acc + voltage }


    println("Sum of voltages: $result")
}

suspend fun main() {
    measureTime {
        runPart("day3/example.txt")
        runPart("day3/part1.txt")
        runPart("day3/part2.txt", batteries = 12)
    }.also {
        println("Execution time: $it")
    }
}
