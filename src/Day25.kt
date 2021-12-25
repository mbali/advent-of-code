import Day25.step
import Day25.toSeafloor

object Day25 {
    data class SeaFloor(
        val sizeX: Int,
        val sizeY: Int,
        val eastMovingHerdLocations: Set<Pair<Int, Int>>,
        val southMovingHerdLocations: Set<Pair<Int, Int>>
    )

    fun SeaFloor.step(): SeaFloor? {
        val eastMovers = eastMovingHerdLocations.filterNot { (x, y) ->
            val next = (x + 1) % sizeX to y
            next in eastMovingHerdLocations || next in southMovingHerdLocations
        }
        val nextEastMovingHerd =
            eastMovingHerdLocations - eastMovers.toSet() + eastMovers.map { (x, y) -> (x + 1) % sizeX to y }
        val southMovers = southMovingHerdLocations.filterNot { (x, y) ->
            val next = x to (y + 1) % sizeY
            next in nextEastMovingHerd || next in southMovingHerdLocations
        }
        if (eastMovers.isEmpty() && southMovers.isEmpty()) return null
        val nextSouthMovingHerd =
            southMovingHerdLocations - southMovers + southMovers.map { (x, y) -> x to (y + 1) % sizeY }
        return SeaFloor(sizeX, sizeY, nextEastMovingHerd, nextSouthMovingHerd)
    }

    fun List<String>.toSeafloor(): SeaFloor {
        val sizeX = first().length
        val sizeY = size
        val eastMovingHerd = mutableSetOf<Pair<Int, Int>>()
        val southMovingHerd = mutableSetOf<Pair<Int, Int>>()
        this.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (c == '>') eastMovingHerd.add(x to y)
                if (c == 'v') southMovingHerd.add(x to y)
            }
        }
        return SeaFloor(sizeX, sizeY, eastMovingHerd, southMovingHerd)
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        return generateSequence(input.toSeafloor()) { it.step() }.count()
    }

    val testInput = readInput("Day25_test")
    check(part1(testInput) == 58)

    val input = readInput("Day25")
    println(part1(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("solution", 10) { part1(input) }
    }
}
