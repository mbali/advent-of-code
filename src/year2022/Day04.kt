package year2022

import readInput

fun main() {

    val inputClassifier = "Day04"

    fun IntProgression.fullyContains(other: IntProgression) = this.first <= other.first && this.last >= other.last

    fun IntProgression.overlaps(other: IntProgression) = this.first <= other.last && this.last >= other.first

    fun parseInput(input: List<String>): List<Pair<IntRange, IntRange>> =
        input.map { line ->
            val (range1, range2) = line.split(",")
            val (min1, max1) = range1.split("-").map { it.toInt() }
            val (min2, max2) = range2.split("-").map { it.toInt() }
            IntRange(min1, max1) to IntRange(min2, max2)
        }

    fun part1(input: List<String>): Int =
        parseInput(input).count { (range1, range2) ->
            range1.fullyContains(range2) || range2.fullyContains(range1)
        }


    fun part2(input: List<String>): Int =
        parseInput(input).count { (range1, range2) ->
            range1.overlaps(range2)
        }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 2)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 4)
    println(part2(input))

}
