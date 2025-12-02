package org.example.utils

import kotlinx.coroutines.flow.flow
import kotlin.sequences.forEach

fun resourceLines(resourcePath: String) = flow {
    object {}.javaClass.getResourceAsStream("/$resourcePath")
        ?.bufferedReader()
        ?.useLines { lines ->
            lines.forEach { emit(it) }
        } ?: throw IllegalArgumentException("Resource not found: /$resourcePath")
}
