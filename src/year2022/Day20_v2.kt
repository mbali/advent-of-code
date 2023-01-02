package year2022

import readInput
import kotlin.math.abs

fun main() {
    val inputClassifier = "Day20"

    data class ValueWithIndex(val value: Long, val index: Int)

    fun List<Long>.withIndexes() = ArrayDeque(mapIndexed { index, value -> ValueWithIndex(value, index) })

    fun <T> ArrayDeque<T>.rotate(times: Long) {
        if (abs(times).mod(size) == 0) return
        if (times > 0) {
            repeat(times.mod(size)) {
                addLast(removeFirst())
            }
        } else if (times < 0) {
            repeat(abs(times).mod(size)) {
                addFirst(removeLast())
            }
        }
    }

    fun ArrayDeque<ValueWithIndex>.mix() {
        for (idx in 0 until size) {
            while (first().index != idx) {
                rotate(1)
            }
            val current = removeFirst()
            rotate(current.value)
            addFirst(current)
        }
    }

    fun ArrayDeque<ValueWithIndex>.normalize() {
        while (this.first().value != 0L) {
            rotate(1)
        }
    }

    fun ArrayDeque<ValueWithIndex>.grooveCoordinates(): List<Long> =
        buildList {
            normalize()
            repeat(3) {
                rotate(1000)
                add(this@grooveCoordinates.first().value)
            }
        }

    fun List<String>.parseInput(): List<Long> {
        return this.map { it.toLong() }
    }


    fun List<String>.solution(decryptionKey: Long = 1L, rounds: Int = 1): Long {
        val buffer = parseInput().map { it * decryptionKey }.withIndexes()
        repeat(rounds) {
            buffer.mix()
        }
        return buffer.grooveCoordinates().sum()
    }

    fun part1(input: List<String>) = input.solution()


    fun part2(input: List<String>) = input.solution(811589153, 10)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 3L)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 1_623_178_306L)
    println(part2(input))

    println("Checking for cycles")

    fun checkForCycles(input: List<String>, checkpointFrequency: Int = 1000) {
        val buffer = input.parseInput().withIndexes().apply { normalize() }
        val seen = mutableMapOf<List<Long>, Int>()
        var i = 0
        while (true) {
            val key = buffer.map { it.value }
            if (key in seen) {
                println("Cycle found at $i, length ${i - seen[key]!!}")
                break
            }
            seen[key] = i
            i++
            buffer.mix()
            buffer.normalize()
            if (i % checkpointFrequency == 0) {
                println("Checkpoint after $i mixes")
            }
        }
    }

    checkForCycles(testInput)
    println("Checking cycles for input")
    checkForCycles(input)

}
