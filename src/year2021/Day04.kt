package year2021

import SHOULD_NOT_REACH
import benchmark
import readInput

private class BingoBoard(val numbers: Array<Int>) {
    init {
        check(numbers.size == 25)
    }

    private val marks = BooleanArray(25)

    fun mark(number: Int): Int? {
        val idx = numbers.indexOf(number)
        if (idx < 0) return null
        marks[idx] = true
        val bingo = (0..4).map { marks[idx / 5 * 5 + it] }.reduce { acc, mark -> acc && mark } ||
                (0..4).map { marks[idx % 5 + it * 5] }.reduce { acc, mark -> acc && mark }
        if (!bingo) return null
        return numbers.zip(marks.toTypedArray()).filter { !it.second }.sumOf { it.first } * number
    }
}

fun main() {

    fun parseInput(input: List<String>): Pair<List<Int>, List<BingoBoard>> {
        val draws = input.first().split(",").map { it.toInt() }
        val boards = input.drop(2).filter { it.isNotEmpty() }.chunked(5) { board ->
            board.flatMap { line ->
                line.split("""\s+""".toRegex()).filter { it.isNotEmpty() }.map { it.toInt() }
            }.toTypedArray().let { BingoBoard(it) }
        }
        return draws to boards
    }

    fun part1(input: List<String>): Int {
        val (draws, boards) = parseInput(input)
        draws.forEach { draw ->
            boards.forEach { board ->
                val score = board.mark(draw)
                if (score != null) {
                    return score
                }
            }
        }
        SHOULD_NOT_REACH()
    }

    fun part2(input: List<String>): Int {
        val (draws, boards) = parseInput(input).let { it.first to it.second.toMutableList() }
        draws.forEach { draw ->
            ArrayList(boards) //copy boards to avoid concurrent modification
                .forEach { board ->
                    val score = board.mark(draw)
                    if (score != null) {
                        if (boards.size == 1) {
                            return score
                        } else {
                            boards.remove(board)
                        }
                    }
                }
        }
        SHOULD_NOT_REACH()
    }

    val testInput = readInput(2021, "Day04_test")
    check(part1(testInput) == 4512)

    val input = readInput(2021, "Day04")
    println(part1(input))

    check(part2(testInput) == 1924)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}