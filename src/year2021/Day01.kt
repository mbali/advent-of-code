package year2021

import benchmark
import readInput

fun main() {

    fun toInts(input: List<String>) = input.map { it.toInt() }

    fun increaseCount(measurements: Iterable<Int>): Int {
        return measurements.zipWithNext().count { (a, b) -> a < b }
    }

    fun part1(input: List<String>): Int {
        return increaseCount(toInts(input))
    }

    fun part2(input: List<String>): Int {
        val measurements = toInts(input)
        return increaseCount(measurements.windowed(3){ it.sum() })
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2021, "Day01_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 5)

    val input = readInput(2021, "Day01")
    println(part1(input))
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
