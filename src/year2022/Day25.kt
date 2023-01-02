package year2022

import readInput

fun main() {

    val inputClassifier = "Day25"

    val digits = "=-012".toCharArray()

    fun fromSnafu(snafu: String): Long {
        var result = 0L
        for (c in snafu) {
            result = result * 5 + (digits.indexOf(c) - 2)
        }
        return result
    }

    fun Long.toSnafu(): String {
        if (this == 0L) return "0"
        if (this < 0L) return "-" + (-this).toSnafu()
        var result = ""
        var n = this
        while (n > 0) {
            val digit = (n + 2) % 5 - 2
            result = digits[digit.toInt() + 2] + result
            n = (n - digit) / 5
        }
        return result
    }

    fun part1(input: List<String>): String {
        return input.sumOf { fromSnafu(it) }.toSnafu()
    }


    fun part2(input: List<String>): String {
        return "BLEND IT!"
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == "2=-1=0")

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == "BLEND IT!")
    println(part2(input))

}
