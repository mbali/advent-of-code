package year2022

import readInput

fun main() {

    val inputClassifier = "Day03"


    fun priorityOf(char: Char) = when (char) {
        in 'A'..'Z' -> 27 + (char - 'A')
        in 'a'..'z' -> 1 + (char - 'a')
        else -> 0
    }

    fun part1(input: List<String>): Int = input.map { rucksack ->
        val cnt = rucksack.length / 2
        val comp1 = rucksack.take(cnt)
        val comp2 = rucksack.takeLast(cnt)
        comp1 to comp2
    }.flatMap { (comp1, comp2) ->
        comp1.toSet() intersect comp2.toSet()
    }.sumOf { priorityOf(it) }


    fun part2(input: List<String>): Int = input.windowed(3, 3, partialWindows = false)
        .flatMap { group ->
            group.map { it.toSet() }.reduce(Set<Char>::intersect)
        }.sumOf { priorityOf(it) }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 157)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 70)
    println(part2(input))

}
