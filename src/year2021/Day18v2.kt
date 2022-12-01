package year2021

import benchmark
import year2021.SnailfishToken.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pmap
import readInput

private sealed class SnailfishToken() {
    data class RegularNumber(val value: Int) : SnailfishToken()
    object StartPair : SnailfishToken()
    object EndPair : SnailfishToken()
    object Next : SnailfishToken()
}

private fun Array<SnailfishToken>.explode(): Array<SnailfishToken>? {
    val depths = Array(size) { 0 }
    var depth = 0
    for (i in indices) {
        val token = this[i]
        depth =
            if (token === StartPair) depth + 1
            else if (token === EndPair) depth - 1
            else depth
        depths[i] = depth
    }
    var explosionIndex: Int? = null
    for (i in indices) {
        if (depths[i] > 4 &&
            this[i + 2] == Next &&
            this[i + 1] is RegularNumber &&
            this[i + 3] is RegularNumber
        ) {
            explosionIndex = i
            break
        }
    }
    explosionIndex ?: return null
    val result = Array(size - 4) { newIdx ->
        if (newIdx < explosionIndex) this[newIdx]
        else if (newIdx == explosionIndex) RegularNumber(0)
        else this[newIdx + 4]
    }
    for (i in explosionIndex - 1 downTo 0) {
        if (result[i] is RegularNumber) {
            result[i] =
                RegularNumber((result[i] as RegularNumber).value + (this[explosionIndex + 1] as RegularNumber).value)
            break
        }
    }
    for (i in explosionIndex + 1 until result.size) {
        if (result[i] is RegularNumber) {
            result[i] =
                RegularNumber((result[i] as RegularNumber).value + (this[explosionIndex + 3] as RegularNumber).value)
            break
        }
    }
    return result
}

private fun Array<SnailfishToken>.split(): Array<SnailfishToken>? {
    var splitIndex: Int? = null
    for (i in indices) {
        val token = this[i]
        if (token is RegularNumber && token.value >= 10) {
            splitIndex = i
            break
        }
    }
    splitIndex ?: return null
    val value = (this[splitIndex] as RegularNumber).value
    return Array(size + 4) { idx ->
        if (idx < splitIndex) this[idx]
        else if (idx > splitIndex + 4) this[idx - 4]
        else if (idx == splitIndex) StartPair
        else if (idx == splitIndex + 1) RegularNumber(value / 2)
        else if (idx == splitIndex + 2) Next
        else if (idx == splitIndex + 3) RegularNumber((value + 1) / 2)
        else EndPair
    }
}

private fun Array<SnailfishToken>.simplify(): Array<SnailfishToken> =
    generateSequence(this) {
        it.explode() ?: it.split()
    }.last()

private fun Array<SnailfishToken>.magnitude(): Int {
    val stack = ArrayDeque<Int>()
    this.forEach { token ->
        if (token is RegularNumber) {
            stack.addLast(token.value)
        } else if (token == EndPair) {
            stack.addLast(stack.removeLast() * 2 + stack.removeLast() * 3)
        }
    }
    return stack.removeLast()
}

private fun Array<SnailfishToken>.add(other: Array<SnailfishToken>): Array<SnailfishToken> =
    buildList {
        add(StartPair)
        addAll(this@add)
        add(Next)
        addAll(other)
        add(EndPair)
    }.toTypedArray().simplify()

private fun String.tokenize(): Array<SnailfishToken> {
    return buildList {
        var number: Int? = null
        val str = this@tokenize
        for (c in str.toCharArray()) {
            if (c.isDigit()) {
                number = (number ?: 0) * 10 + c.digitToInt()
            } else {
                if (number != null) {
                    add(RegularNumber(number))
                    number = null
                }
                add(
                    when (c) {
                        '[' -> StartPair
                        ',' -> Next
                        ']' -> EndPair
                        else -> throw IllegalStateException("Unexpected character: $c")
                    }
                )
            }
        }
    }.toTypedArray()
}


fun main() {
    fun part1(input: List<String>): Int {
        return input.map { it.tokenize() }.reduce { acc, tokens -> acc.add(tokens) }.magnitude()
    }

    fun part2(input: List<String>): Int {
        return runBlocking(Dispatchers.Default) {
            val numbers = input.pmap { it.tokenize() }
            numbers.indices.flatMap { i -> numbers.indices.filter { j -> j != i }.map { i to it } }
                .pmap { numbers[it.first].add(numbers[it.second]).magnitude() }.maxOf { it }
        }
    }

    val testInput = readInput(2021, "Day18_test")
    check(part1(testInput) == 4140)

    val input = readInput(2021, "Day18")
    println(part1(input))

    check(part2(testInput) == 3993)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 100) { part1(input) }
        benchmark("part2", 100) { part2(input) }
    }
}
