package year2022

import readInput
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.sign
import kotlin.streams.toList

fun main() {

    val inputClassifier = "Day09"

    val initialPosition = 0 to 0

    val directionMap = mapOf(
        "R" to (1 to 0),
        "L" to (-1 to 0),
        "U" to (0 to -1),
        "D" to (0 to 1)
    )

    fun headSegment(from: Pair<Int, Int>, dir: Pair<Int, Int>, cnt: Int): List<Pair<Int, Int>> {
        return buildList {
            repeat(cnt) {
                add(from.first + dir.first * (it + 1) to from.second + dir.second * (it + 1))
            }
        }
    }

    fun generateHeadRoute(input: List<String>): List<Pair<Int, Int>> {
        return input.map {
            val (dir, cnt) = it.split(" ")
            directionMap.getValue(dir) to cnt.toInt()
        }.fold(listOf(initialPosition)) { route, (dir, cnt) ->
            route + headSegment(route.last(), dir, cnt)
        }
    }

    fun followerSegment(from: Pair<Int, Int>, to: Pair<Int, Int>): List<Pair<Int, Int>> {
        var (x, y) = from
        val (toX, toY) = to
        return buildList {
            while (abs(x - toX) > 1 || abs(y - toY) > 1) {
                x += (toX - x).sign
                y += (toY - y).sign
                add(x to y)
            }
        }
    }

    fun followerRoute(headRoute: List<Pair<Int, Int>>) =
        headRoute.fold(listOf(initialPosition)) { route, pos ->
            route + followerSegment(route.last(), pos)
        }

    fun part1(input: List<String>): Int {
        return followerRoute(generateHeadRoute(input)).toSet().size
    }


    fun part2(input: List<String>): Int {
        return IntStream.range(1, 10).toList().fold(generateHeadRoute(input)) { route, _ ->
            followerRoute(route)
        }.toSet().size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 13)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    val testInput2 = readInput(2022, "${inputClassifier}_test2")

    //part2
    check(part2(testInput) == 1)
    check(part2(testInput2) == 36)
    println(part2(input))

}
