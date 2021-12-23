import Day23.AmphipodType.*
import Day23.EXPECTED_STATE
import Day23.LocationType.HALLWAY
import Day23.LocationType.ROOM
import Day23.calculateDistanceFrom
import Day23.parseState
import java.util.*
import kotlin.math.absoluteValue

object Day23 {
    enum class AmphipodType(val movementCost: Int, val marker: Char) {
        AMBER(1, 'A'),
        BRONZE(10, 'B'),
        COPPER(100, 'C'),
        DESERT(1000, 'D');

        companion object {
            fun of(char: Char): AmphipodType? =
                when (char) {
                    '.' -> null
                    'A' -> AMBER
                    'B' -> BRONZE
                    'C' -> COPPER
                    'D' -> DESERT
                    else -> throw IllegalArgumentException("Illegal amphipod type '$char'")
                }
        }
    }

    fun AmphipodType?.marker(): Char = this?.marker ?: '.'

    enum class LocationType {
        HALLWAY,
        ROOM
    }

    data class Location(val type: LocationType, val x: Int, val y: Int, val expectedAmphipodType: AmphipodType?)

    val LOCATIONS = buildList {
        addAll(listOf(1, 2, 4, 6, 8, 10, 11).map { Location(HALLWAY, it, 1, null) })
        listOf(AMBER to 3, BRONZE to 5, COPPER to 7, DESERT to 9).forEach { (type, x) ->
            (2..3).forEach { y -> add(Location(ROOM, x, y, type)) }
        }
    }

    private fun Location.isNeighbour(other: Location, allLocations: List<Location>): Boolean {
        return if (other == this) false
        else if (this.type == HALLWAY && other.type == HALLWAY && y == other.y) {
            allLocations.filter { it.type == HALLWAY }
                .none { it.x in x + 1 until other.x || it.x in other.x + 1 until x }
        } else {
            (x - other.x).absoluteValue <= 1 && (y - other.y).absoluteValue <= 1
        }
    }

    val NEIGHBOURS = LOCATIONS.associateWith { from ->
        LOCATIONS.filter { to -> from.isNeighbour(to, LOCATIONS) }
            .associateWith { (Vec3(it.x, it.y) manhattanDistance Vec3(from.x, from.y)) }
    }

    val EXPECTED_STATE = LOCATIONS.map { it.expectedAmphipodType.marker() }.joinToString("")

    fun String.neigbouringStates(): Map<String, Int> {
        assert(this.length == LOCATIONS.size)
        val initial = LOCATIONS.zip(this.map { AmphipodType.of(it) }).toMap()
        return buildMap {
            initial.filterValues { it != null }.forEach { (from, type) ->
                val movementCost = type!!.movementCost
                NEIGHBOURS.getValue(from).filterKeys { to ->
                    initial.getValue(to) == null
                }.filterKeys { to ->
                    to.type == HALLWAY || from.type == to.type || to.expectedAmphipodType == type && NEIGHBOURS.getValue(
                        to
                    ).keys.filter { it.type == ROOM && initial[it] != null }
                        .all { it.expectedAmphipodType == initial[it] }
                }.forEach { (toLocation, cost) ->
                    val next = initial.toMutableMap()
                    next[from] = null
                    next[toLocation] = type
                    put(LOCATIONS.map { next[it].marker() }.joinToString(""), cost * movementCost)
                }
            }
        }
    }

    fun List<String>.parseState(): String {
        return LOCATIONS.map { this[it.y][it.x] }.joinToString("")
    }

    fun calculateDistanceFrom(start: String, target: String): Int {
        val queue = PriorityQueue<Pair<String, Int>>(compareBy { it.second })
        val seen = mutableSetOf<String>()
        queue.offer(start to 0)
        do {
            val maybeNext = queue.poll()
            maybeNext?.let { next ->
                val nextState = next.first
                if (!seen.contains(nextState)) {
                    val distance = next.second
                    if (nextState.contentEquals(target)) return distance
                    seen.add(nextState)
                    nextState.neigbouringStates().filter { !seen.contains(it.key) }.forEach { (state, cost) ->
                        val candidateDistance = distance + cost
                        queue.offer(state to candidateDistance)
                    }
                }
            }
        } while (maybeNext != null)
        SHOULD_NOT_REACH()
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        val initialState = input.parseState()
        return calculateDistanceFrom(initialState, EXPECTED_STATE)
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day23_test")
    check(part1(testInput) == 12521)

    val input = readInput("Day23")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}

