package year2022

import readInput
import year2022.Day23.disperse

object Day23 {
    data class Position(val x: Int, val y: Int) {
        operator fun plus(dir: Direction): Position {
            return Position(x + dir.dx, y + dir.dy)
        }
    }

    enum class Direction(val dx: Int, val dy: Int) {
        N(0, -1),
        NE(1, -1),
        E(1, 0),
        SE(1, 1),
        S(0, 1),
        SW(-1, 1),
        W(-1, 0),
        NW(-1, -1)
    }

    val checked = buildList {
        add(Direction.N to listOf(Direction.NW, Direction.N, Direction.NE))
        add(Direction.S to listOf(Direction.SE, Direction.S, Direction.SW))
        add(Direction.W to listOf(Direction.SW, Direction.W, Direction.NW))
        add(Direction.E to listOf(Direction.NE, Direction.E, Direction.SE))
    }

    fun Position.neighbours(): Set<Position> {
        return Direction.values().map { this + it }.toSet()
    }

    fun next(positions: Set<Position>, round: Int): Set<Position>? {
        if (positions.all { p ->
                p.neighbours().none { it in positions }
            }) return null
        val consideredPositions = buildMap {
            for (p in positions) {
                if (p.neighbours().none { it in positions }) continue
                for (i in checked.indices) {
                    val c = (i + round) % checked.size
                    val (dir, toCheck) = checked[c]
                    if (toCheck.none { p + it in positions }) {
                        this[p] = p + dir
                        break
                    }
                }
            }
        }
        val duplicates = consideredPositions.values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        val movements = consideredPositions.filterValues { it !in duplicates }
        val next = positions.map { p ->
            movements[p] ?: p
        }.toSet()
        if (next.size != positions.size) throw IllegalStateException("Size changed")
        return next
    }

    fun display(positions: Set<Position>) {
        val minX = positions.minOf { it.x }
        val maxX = positions.maxOf { it.x }
        val minY = positions.minOf { it.y }
        val maxY = positions.maxOf { it.y }
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                print(if (Position(x, y) in positions) '#' else '.')
            }
            println()
        }
    }

    fun disperse(initial: Set<Position>): Sequence<Set<Position>> {
        return generateSequence(initial to 0) { (positions, round) ->
            next(positions, round)?.let { it to round + 1 }
        }.map { it.first }

    }
}

fun main() {

    fun parseInput(input: List<String>): Set<Day23.Position> {
        return input.mapIndexed { y, row ->
            row.mapIndexedNotNull { x, c ->
                if (c == '#') Day23.Position(x, y) else null
            }
        }.flatten().toSet()
    }

    fun boundingBoxSize(positions: Set<Day23.Position>): Int {
        val minX = positions.minOf { it.x }
        val minY = positions.minOf { it.y }
        val maxX = positions.maxOf { it.x }
        val maxY = positions.maxOf { it.y }
        return (maxX - minX + 1) * (maxY - minY + 1)
    }


    val inputClassifier = "Day23"

    fun part1(input: List<String>): Int {
        val start = parseInput(input)
        return disperse(start).take(11).last().let(::boundingBoxSize) - start.size
    }

    /*
        disperse(parseInput(readInput(2022, "${inputClassifier}_test"))).forEachIndexed {i, p ->
            println("After $i rounds:")
            Day23.display(p)
            println("--------------------------------------------------")
        }
    */



    fun part2(input: List<String>): Int {
        val start = parseInput(input)
        return disperse(start).mapIndexed { i, _ -> i }.last() + 1
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 110)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 20)
    println(part2(input))

}
