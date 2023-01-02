package year2022

import readInput
import year2022.Day22.toInstructions
import year2022.Day22.value
import year2022.Day22.walk
import year2022.Day22.walkCube

object Day22 {
    enum class Direction(val dx: Int, val dy: Int, val value: Int) {
        NORTH(0, -1, 3),
        EAST(1, 0, 0),
        SOUTH(0, 1, 1),
        WEST(-1, 0, 2);

        fun left(): Direction {
            return when (this) {
                NORTH -> WEST
                EAST -> NORTH
                SOUTH -> EAST
                WEST -> SOUTH
            }
        }

        fun right(): Direction {
            return when (this) {
                NORTH -> EAST
                EAST -> SOUTH
                SOUTH -> WEST
                WEST -> NORTH
            }
        }
    }

    enum class Tile {
        FLOOR, WALL
    }

    data class Position(val x: Int, val y: Int)

    data class Board(val tiles: Map<Position, Tile>, val rows: Int, val cols: Int) {
        companion object {
            fun parse(input: List<String>): Board {
                val tiles = mutableMapOf<Position, Tile>()
                val rows = input.size
                var cols = 0
                input.forEachIndexed { y, row ->
                    cols = maxOf(row.length, cols)
                    row.forEachIndexed { x, c ->
                        val tile = when (c) {
                            '.' -> Tile.FLOOR
                            '#' -> Tile.WALL
                            else -> null
                        }?.let { tiles[Position(x, y)] = it }
                    }
                }
                return Board(tiles, rows, cols)
            }
        }
    }

    fun String.toInstructions(): List<String> =
        Regex("""L|R|(\d+)""").findAll(this).map { it.value }.toList()

    data class State(val position: Position, val direction: Direction)

    fun Board.nextPosition(position: Position, direction: Direction): Position {
        var pos = position
        do {
            pos = Position((pos.x + direction.dx).mod(cols), (pos.y + direction.dy).mod(rows))
        } while (tiles[pos] == null)
        return pos
    }

    fun Board.walk(
        instructions: List<String>,
        initialState: State = State(
            tiles.keys.minWith(
                compareBy<Position> { it.y }.then(
                    compareBy { it.x })
            ), Direction.EAST
        )
    ): Sequence<State> = sequence {
        if (tiles[initialState.position] != Tile.FLOOR) throw IllegalArgumentException("Initial position is not a floor tile")
        var state = initialState
        yield(state)
        instructions.forEach { instruction ->
            when (instruction) {
                "L" -> {
                    state = state.copy(direction = state.direction.left())
                    yield(state)
                }

                "R" -> {
                    state = state.copy(direction = state.direction.right())
                    yield(state)
                }

                else -> {
                    val steps = instruction.toInt()
                    repeat(steps) {
                        val newPosition =
                            this@walk.nextPosition(state.position, state.direction)
                        if (tiles[newPosition] == Tile.WALL) return@repeat
                        state = state.copy(position = newPosition)
                        yield(state)
                    }
                }
            }
        }
    }

    //this is hackish, tailored for my input
    fun Board.stepOnCube(position: Position, direction: Direction): Pair<Position, Direction> {
        val newPosition = Position(position.x + direction.dx, position.y + direction.dy)
        if (newPosition in tiles) return Pair(newPosition, direction)
        //we stepped out somewhere
        if (direction == Direction.NORTH) {
            if (position.x in 0 until 50) return Position(50, 50 + position.x) to Direction.EAST
            if (position.x in 50 until 100) return Position(0, 100 + position.x) to Direction.EAST
            if (position.x in 100 until 150) return Position(position.x - 100, 199) to Direction.NORTH
        }
        if (direction == Direction.EAST) {
            if (position.y in 0 until 50) return Position(99, 149 - position.y) to Direction.WEST
            if (position.y in 50 until 100) return Position(50 + position.y, 49) to Direction.NORTH
            if (position.y in 100 until 150) return Position(149, 149 - position.y) to Direction.WEST
            if (position.y in 150 until 200) return Position(position.y - 100, 149) to Direction.NORTH
        }
        if (direction == Direction.SOUTH) {
            if (position.x in 0 until 50) return Position(100 + position.x, 0) to Direction.SOUTH
            if (position.x in 50 until 100) return Position(49, 100 + position.x) to Direction.WEST
            if (position.x in 100 until 150) return Position(99, position.x - 50) to Direction.WEST
        }
        if (direction == Direction.WEST) {
            if (position.y in 0 until 50) return Position(0, 149 - position.y) to Direction.EAST
            if (position.y in 50 until 100) return Position(position.y - 50, 100) to Direction.SOUTH
            if (position.y in 100 until 150) return Position(50, 149 - position.y) to Direction.EAST
            if (position.y in 150 until 200) return Position(position.y - 100, 0) to Direction.SOUTH
        }
        TODO("Unexpected $position -> $direction")
    }


    fun Board.walkCube(
        instructions: List<String>,
        initialState: State = State(
            tiles.keys.minWith(
                compareBy<Position> { it.y }.then(
                    compareBy { it.x })
            ), Direction.EAST
        )
    ): Sequence<State> = sequence {
        if (tiles[initialState.position] != Tile.FLOOR) throw IllegalArgumentException("Initial position is not a floor tile")
        var state = initialState
        yield(state)
        instructions.forEach { instruction ->
            when (instruction) {
                "L" -> {
                    state = state.copy(direction = state.direction.left())
                    yield(state)
                }

                "R" -> {
                    state = state.copy(direction = state.direction.right())
                    yield(state)
                }

                else -> {
                    val steps = instruction.toInt()
                    repeat(steps) {
                        val newPosition =
                            this@walkCube.stepOnCube(state.position, state.direction)
                        if (tiles.getValue(newPosition.first) == Tile.WALL) return@repeat
                        state = State(newPosition.first, newPosition.second)
                        yield(state)
                    }
                }
            }
        }
    }

    fun State.value(): Int = (position.y + 1) * 1000 + (position.x + 1) * 4 + direction.value
}

fun main() {


    val inputClassifier = "Day22"

    fun part1(input: List<String>): Int {
        val board = Day22.Board.parse(input.dropLast(2))
        val instructions = input.last().toInstructions()
        val states = board.walk(instructions)
        states.forEach { println(it) }
        return states.last().also { println(it) }.value()
    }


    fun part2(input: List<String>): Int {
        val board = Day22.Board.parse(input.dropLast(2))
        val instructions = input.last().toInstructions()
        val states = board.walkCube(instructions)
        states.forEach { println(it) }
        return states.last().also { println(it) }.value()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 6032)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    //check(part2(testInput) == TODO())
    println(part2(input))

}
