@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.day6

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import org.example.utils.resourceLines
import kotlin.time.measureTime

suspend fun runPart1(fileName: String) {
    val sums = mutableMapOf<Int, Long>()
    val products = mutableMapOf<Int, Long>()
    var result: Long = 0L
    resourceLines(fileName).collect { line ->
        line.trim().split("\\s+".toRegex()).forEachIndexed { index, s ->
            if (s == "+") {
                result += sums.getOrElse(index) { 0 }
            } else if (s == "*") {
                result += products.getOrElse(index) { 0 }
            } else {
                val value = s.toLong()
                sums[index] = sums.getOrElse(index) { 0 } + value
                products[index] = products.getOrElse(index) { 1 } * value
            }
        }
    }
    println("Result: $result")
}
suspend fun runPart2(fileName: String) {
    var result: Long = 0L
    val input: MutableList<String> = mutableListOf()
    var maxLength = 0
    resourceLines(fileName).collect { line ->
        input += line.reversed()
        maxLength = maxOf(maxLength, line.length)
    }

    var tempSum = 0L
    var tempProduct = 1L

    for (columnIndex in 0 until maxLength) {
        var columnBuilder = ""
        for (rowIndex in 0 until input.size) {
            columnBuilder += input[rowIndex][columnIndex]
        }
        columnBuilder = columnBuilder.trim()
        if (columnBuilder.isEmpty()) continue

        val number = columnBuilder.takeWhile { it.isDigit() }.toLong()
        val operator = columnBuilder.dropWhile { it.isDigit() }.trim()

        tempSum += number
        tempProduct *= number

        if (operator.isBlank()) continue


        if (operator == "+") {
            result += tempSum
        }
        if (operator == "*") {
            result += tempProduct
        }
        tempSum = 0L
        tempProduct = 1L
    }



    println("Result: $result")
}

suspend fun main() {
    measureTime {
//        runPart1("day6/example.txt")
//        runPart2("day6/example.txt")
        runPart2("day6/input.txt")
//        runPart1("day6/input.txt")
    }.also {
        println("Execution time: $it")
    }
}
