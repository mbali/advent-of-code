package year2022

import readInput
import java.util.*

object Day24 {
    enum class Direction(val dx: Int, val dy: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0)
    }

    data class Blizzard(val direction: Direction, val position: Position)
    data class Position(val x: Int, val y: Int) {
        operator fun plus(dir: Direction): Position {
            return Position(x + dir.dx, y + dir.dy)
        }
    }

    data class Basin(val blizzards: Set<Blizzard>, val walls: Set<Position>, val width: Int, val height: Int) {
        companion object {
            fun parse(input: List<String>): Basin {
                val blizzards = mutableSetOf<Blizzard>()
                val walls = mutableSetOf<Position>()
                input.forEachIndexed { y, row ->
                    row.forEachIndexed { x, c ->
                        val position = Position(x, y)
                        when (c) {
                            '^' -> blizzards.add(Blizzard(Direction.UP, position))
                            'v' -> blizzards.add(Blizzard(Direction.DOWN, position))
                            '<' -> blizzards.add(Blizzard(Direction.LEFT, position))
                            '>' -> blizzards.add(Blizzard(Direction.RIGHT, position))
                            '#' -> walls.add(position)
                        }
                    }
                }
                return Basin(blizzards, walls, input[0].length, input.size)
            }
        }
    }

    fun Basin.next(): Basin {
        val newBlizzards = blizzards.map { blizzard ->
            var newPosition = blizzard.position + blizzard.direction
            if (newPosition in walls) {
                newPosition = when (blizzard.direction) {
                    Direction.UP -> newPosition.copy(y = height - 2)
                    Direction.DOWN -> newPosition.copy(y = 1)
                    Direction.LEFT -> newPosition.copy(x = width - 2)
                    Direction.RIGHT -> newPosition.copy(x = 1)
                }
            }
            Blizzard(blizzard.direction, newPosition)
        }.toSet()
        return copy(blizzards = newBlizzards)
    }

    fun Basin.mayStepTo(position: Position): Boolean =
        position !in walls && position.x in 0 until width && position.y in 0 until height

    fun routeLength(
        initialBasin: Basin,
        start: Position = entry(initialBasin),
        target: Position = exit(initialBasin)
    ): Pair<Int, Basin> {
        val windCycle = buildList {
            var state = initialBasin
            while (state !in this) {
                add(state)
                state = state.next()
            }
        }

        data class State(val position: Position, val cycleIndex: Int)

        val visited = mutableSetOf<State>()
        val queue = PriorityQueue<Pair<State, Int>>(compareBy { it.second })
        queue.add(State(start, 0) to 0)
        while (queue.isNotEmpty()) {
            val (state, length) = queue.poll()
            if (state in visited) continue
            visited.add(state)
            val basinState = windCycle[state.cycleIndex]
            if (state.position == target) return length to basinState
            if (basinState.blizzards.any { it.position == state.position }) continue
            val nextCycleIndex = (state.cycleIndex + 1) % windCycle.size
            buildList {
                Direction.values().forEach { add(state.position + it) }
                add(state.position)
            }.filter { basinState.mayStepTo(it) }
                .map { nextPosition ->
                    State(nextPosition, nextCycleIndex)
                }.filter { it !in visited }
                .forEach { next ->
                    queue.add(next to length + 1)
                }
        }
        throw IllegalArgumentException("No route found")
    }

    internal fun exit(initialBasin: Basin) =
        (0 until initialBasin.width).map { Position(it, initialBasin.height - 1) }
            .filterNot { it in initialBasin.walls }.first()

    internal fun entry(initialBasin: Basin) = (0 until initialBasin.width).map { Position(it, 0) }
        .filterNot { it in initialBasin.walls }.first()
}

fun main() {

    val inputClassifier = "Day24"

    fun part1(input: List<String>): Int {
        val basin = Day24.Basin.parse(input)
        return Day24.routeLength(basin).first
    }


    fun part2(input: List<String>): Int {
        val basin = Day24.Basin.parse(input)
        val entry = Day24.entry(basin)
        val exit = Day24.exit(basin)
        val (leg1, state1) = Day24.routeLength(basin, start = entry, target = exit)
        val (leg2, state2) = Day24.routeLength(state1, start = exit, target = entry)
        val (leg3, state3) = Day24.routeLength(state2, start = entry, target = exit)
        return leg1 + leg2 + leg3
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 18)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 54)
    println(part2(input))

}
