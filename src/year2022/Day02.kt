package year2022

import readInput

internal enum class Play {
    ROCK,
    PAPER,
    SCISSORS;

    fun beats(other: Play) = when (this) {
        ROCK -> other == SCISSORS
        PAPER -> other == ROCK
        SCISSORS -> other == PAPER
    }

    fun forOpponentResult(result: Result) = when (result) {
        Result.DRAW -> this
        Result.WIN -> values().first { it.beats(this) }
        Result.LOSE -> values().first { this.beats(it) }
    }
}

internal enum class Result {
    WIN,
    LOSE,
    DRAW;

    companion object {
        fun of(me: Play, opponent: Play) = when {
            me == opponent -> DRAW
            me.beats(opponent) -> WIN
            else -> LOSE
        }
    }
}

fun main() {

    val inputClassifier = "Day02"

    fun score(elf: Play, me: Play) = when (Result.of(me, elf)) {
        Result.WIN -> 6
        Result.DRAW -> 3
        Result.LOSE -> 0
    } + when (me) {
        Play.ROCK -> 1
        Play.PAPER -> 2
        Play.SCISSORS -> 3
    }

    fun part1(input: List<String>): Int {
        val elfLookup = mapOf(
            "A" to Play.ROCK,
            "B" to Play.PAPER,
            "C" to Play.SCISSORS
        )

        val myLookup = mapOf(
            "X" to Play.ROCK,
            "Y" to Play.PAPER,
            "Z" to Play.SCISSORS
        )

        return input.sumOf { line ->
            val (elf, my) = line.split(" ")
            score(elfLookup.getValue(elf), myLookup.getValue(my))
        }
    }


    fun part2(input: List<String>): Int {
        val elfLookup = mapOf(
            "A" to Play.ROCK,
            "B" to Play.PAPER,
            "C" to Play.SCISSORS
        )

        val myLookup = mapOf(
            "X" to Result.LOSE,
            "Y" to Result.DRAW,
            "Z" to Result.WIN
        )

        return input.sumOf { line ->
            val (elf, my) = line.split(" ")
            val elfPlays = elfLookup.getValue(elf)
            val myPlay = elfPlays.forOpponentResult(myLookup.getValue(my))
            score(elfPlays, myPlay)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 15)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 12)
    println(part2(input))

}
