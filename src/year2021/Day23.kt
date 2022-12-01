package year2021

import SHOULD_NOT_REACH
import benchmark
import readInput
import year2021.Day23.solution
import kotlin.math.absoluteValue

object Day23 {
    enum class AmphipodType(val movementCost: Int, val doorIndex: Int) {
        AMBER(1, 2),
        BRONZE(10, 4),
        COPPER(100, 6),
        DESERT(1000, 8);

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

    data class Room(val expected: AmphipodType, val size: Int, val occupants: List<AmphipodType>)

    fun AmphipodType.canMoveInto(room: Room) =
        room.expected == this && room.occupants.size < room.size && room.occupants.none { it != this }

    fun Room.stepsToEnterFromDoor() = size - occupants.size
    fun Room.stepsToExitToDoor() = size - occupants.size + 1
    fun Room.hallwayIndex() = expected.doorIndex
    fun Room.top() = occupants.last()
    fun Room.removeTop() = copy(
        occupants = occupants.dropLast(1)
    )

    fun Room.attTop(type: AmphipodType) = copy(
        occupants = occupants + type
    )

    data class Hallway(val positions: List<AmphipodType?> = List(11) { null })

    fun Hallway.freePositionsAround(index: Int): Set<Int> {
        var left = index - 1
        while (left >= 0 && positions[left] == null) left--
        var right = index + 1
        while (right < positions.size && positions[right] == null) right++
        return (left + 1 until right).toSet() - HALLWAY_DOOR_INDICES - index
    }

    data class Movement(val type: AmphipodType, val steps: Int)

    fun Movement.cost() = type.movementCost * steps
    fun Hallway.update(index: Int, type: AmphipodType?): Hallway {
        return Hallway(buildList(positions.size) {
            addAll(positions)
            set(index, type)
        })
    }

    fun Room.possibleToHallwayMovements(hallway: Hallway): List<Triple<Room, Hallway, Movement>> {
        return if (occupants.none { it != expected }) emptyList()
        else buildList {
            val type = top()
            val hallwayIdx = hallwayIndex()
            val stepsToExit = stepsToExitToDoor()
            hallway.freePositionsAround(hallwayIdx).forEach { idx ->
                add(
                    Triple(
                        removeTop(),
                        hallway.update(idx, type),
                        Movement(type, (idx - hallwayIdx).absoluteValue + stepsToExit)
                    )
                )
            }
        }
    }

    fun Room.possibleFromHallwayMovements(hallway: Hallway): List<Triple<Room, Hallway, Movement>> {
        val type = expected
        return if (!type.canMoveInto(this)) emptyList()
        else buildList {
            val hallwayIdx = hallwayIndex()
            val stepsToEnter = stepsToEnterFromDoor()
            for (i in hallwayIdx downTo 0) {
                if (hallway.positions[i] == type) {
                    add(
                        Triple(
                            attTop(type),
                            hallway.update(i, null),
                            Movement(type, (i - hallwayIdx).absoluteValue + stepsToEnter)
                        )
                    )
                }
                if (hallway.positions[i] != null) {
                    break
                }
            }
            for (i in hallwayIdx until hallway.positions.size) {
                if (hallway.positions[i] == type) {
                    add(
                        Triple(
                            attTop(type),
                            hallway.update(i, null),
                            Movement(type, (i - hallwayIdx).absoluteValue + stepsToEnter)
                        )
                    )
                }
                if (hallway.positions[i] != null) {
                    break
                }
            }
        }
    }

    val HALLWAY_DOOR_INDICES = AmphipodType.values().map { it.doorIndex }.toSet()

    data class MapState(
        val rooms: Set<Room>,
        val hallway: Hallway
    )

    fun MapState.neighbours(): Map<MapState, Int> =
        buildMap {
            rooms.forEach { room ->
                (room.possibleFromHallwayMovements(hallway) + room.possibleToHallwayMovements(hallway)).forEach { (newRoom, newHallway, movement) ->
                    put(
                        copy(
                            rooms = rooms - room + newRoom,
                            hallway = newHallway
                        ),
                        movement.cost()
                    )
                }
            }
        }

    fun List<String>.parseMapState(roomSize: Int): MapState {
        val hallway = this[1].substring(1..11).map { AmphipodType.of(it) }.let { Hallway(it) }
        val rooms = AmphipodType.values().map { type ->
            (1..roomSize)
                .map { it + 1 }
                .mapNotNull { AmphipodType.of(this[it][type.doorIndex + 1]) }
                .reversed()
                .let { Room(type, roomSize, it) }
        }.toSet()
        return MapState(rooms, hallway)
    }

    fun expectedMapState(roomSize: Int): MapState {
        val hallway = Hallway()
        val rooms = AmphipodType.values().map { type ->
            Room(type, roomSize, List(roomSize) { type })
        }.toSet()
        return MapState(rooms, hallway)
    }


    fun MapState.calculateDistanceTo(target: MapState): Int {
        val seen = mutableSetOf<MapState>()
        val minDistancesByState = mutableMapOf<MapState, Int>().withDefault { Int.MAX_VALUE }
        val statesByDistances = mutableMapOf<Int, MutableSet<MapState>>()

        fun registerCandidate(state: MapState, distance: Int) {
            if (state in seen) return
            val currentMinDistance = minDistancesByState.getValue(state)
            if (distance < currentMinDistance) {
                statesByDistances[currentMinDistance]?.remove(state)
                minDistancesByState[state] = distance
                statesByDistances.computeIfAbsent(distance) {
                    mutableSetOf()
                }.add(state)
            }
        }

        statesByDistances[0] = mutableSetOf(this)
        do {
            val distance = statesByDistances.keys.minOfOrNull { it } ?: SHOULD_NOT_REACH()
            val statesForDistance = statesByDistances.remove(distance)!!
            if (target in statesForDistance) {
                return distance
            }
            statesForDistance
                .mapNotNull { nextState ->
                    if (nextState in seen) null
                    else nextState to nextState.neighbours()
                }
                .forEach { (state, neighbours) ->
                    seen.add(state)
                    neighbours
                        .forEach { (state, cost) ->
                            registerCandidate(state, cost + distance)
                        }
                }
        } while (statesByDistances.isNotEmpty())
        SHOULD_NOT_REACH()
    }


    fun solution(input: List<String>): Int {
        val roomSize = input.size - 3
        val initialState = input.parseMapState(roomSize)
        val expectedState = expectedMapState(roomSize)
        return initialState.calculateDistanceTo(expectedState)
    }

}

fun main() {

    fun part1(input: List<String>): Int {
        val foldedInput = input.toMutableList()
        foldedInput.removeAt(4)
        foldedInput.removeAt(3)
        return solution(foldedInput)
    }

    fun part2(input: List<String>): Int {
        return solution(input)
    }

    val testInput = readInput(2021, "Day23_test")

    check(part1(testInput) == 12521)
    val input = readInput(2021, "Day23")
    println(part1(input))

    check(part2(testInput) == 44169)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 10) { part1(input) }
        benchmark("part2", 10) { part2(input) }
    }
}

