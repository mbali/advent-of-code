package year2022

import readInput
import splitAt

fun main() {

    fun caloriesByElves(input: List<String>) =
        input.splitAt { it.isEmpty() }
            .map { bag -> bag.sumOf { it.toInt() } }


    fun part1(input: List<String>): Int =
        caloriesByElves(input).max()


    fun part2(input: List<String>): Int {
        return caloriesByElves(input).sortedDescending().take(3).sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput(2022, "Day01")
    println(part1(input))
    println(part2(input))

}
