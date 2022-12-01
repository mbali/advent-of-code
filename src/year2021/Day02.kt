package year2021

import benchmark
import readInput

fun main() {

    fun part1(input: List<String>): Int {
        var position = 0 to 0
        input.forEach { line ->
            val (direction, distance) = line.split(" ")
            when (direction) {
                "up" -> position = position.first to (position.second + distance.toInt())
                "down" -> position = position.first to (position.second - distance.toInt())
                "forward" -> position = (position.first - distance.toInt()) to position.second
            }
        }
        return position.first * position.second
    }

    fun part2(input: List<String>): Int {
        var horizontal = 0
        var vertical = 0
        var aim = 0
        input.forEach { line ->
            val (direction, value) = line.split(" ")
            when (direction) {
                "up" -> aim -= value.toInt()
                "down" -> aim += value.toInt()
                "forward" -> {
                    horizontal += value.toInt()
                    vertical += aim * value.toInt()
                }
            }
        }
        return horizontal * vertical
    }

    val testInput = readInput(2021, "Day02_test")
    check(part1(testInput) == 150)
    check(part2(testInput) == 900)

    val input = readInput(2021, "Day02")
    println(part1(input))
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}