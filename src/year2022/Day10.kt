package year2022

import readInput

fun main() {

    val inputClassifier = "Day10"

    fun registerValues(program: List<String>): List<Int> =
        buildList {
            var x = 1
            add(x)
            program.forEach { instuction ->
                //after the first cycle both noop and add keeps the register at the same value
                add(x)
                if (instuction.startsWith("addx ")) {
                    x += instuction.split(" ")[1].toInt()
                    add(x)
                }
            }
        }

    fun part1(input: List<String>): Int {
        val registerHistory = registerValues(input)
        return IntProgression.fromClosedRange(20, 220, 40)
            .sumOf { it * registerHistory[it - 1] }
    }


    fun part2(input: List<String>, type: String) {
        println(type)
        registerValues(input).take(240).mapIndexed { index, x ->
            index % 40 in (x - 1)..(x + 1)
        }.map { if (it) "█" else " " }
            .joinToString("")
            .chunked(40)
            .forEach { println(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 13140)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    part2(testInput, "TEST PATTERN")
    part2(input, "CHARACTER PATTERN")

}
