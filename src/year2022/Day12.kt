package year2022

import readInput

object Day12 {
    enum class Direction(val dx: Int, val dy: Int) {
        UP(0, 1),
        RIGHT(1, 0),
        DOWN(0, -1),
        LEFT(-1, 0);
    }

    data class HeightMap(val map: List<List<Char>>) {
        fun get(x: Int, y: Int) = map[y][x]
        fun contains(x: Int, y: Int) = y in map.indices && x in map[y].indices
        fun heightAt(x: Int, y: Int): Int {
            return when (val field = get(x, y)) {
                'S' -> 0
                'E' -> 'z' - 'a'
                in 'a'..'z' -> field - 'a'
                else -> throw IllegalArgumentException("Unknown tile $field")
            }
        }

        fun neighbours(x: Int, y: Int): List<Pair<Int, Int>> {
            return listOf(x to y + 1, x + 1 to y, x to y - 1, x - 1 to y)
                .filter { (x, y) -> contains(x, y) }
        }

        fun reachableNeighbours(x: Int, y: Int): List<Pair<Int, Int>> {
            return neighbours(x, y).filter { (nx, ny) -> heightAt(nx, ny) - heightAt(x, y) <= 1 }
        }

        fun locationsOf(c: Char) = map.withIndex().flatMap { (y, row) ->
            row.withIndex().filter { (_, field) -> field == c }.map { (x, _) -> x to y }
        }

        fun start() = locationsOf('S').first()

        fun end() = locationsOf('E').first()
    }
}

fun main() {
    val inputClassifier = "Day12"

    fun parseMap(input: List<String>): Day12.HeightMap {
        val map = input.map { it.toList() }
        return Day12.HeightMap(map)
    }

    fun routeCost(map: Day12.HeightMap, vararg additionalCharacters: Char): Int {
        val starts = buildSet {
            add(map.start())
            additionalCharacters.forEach { addAll(map.locationsOf(it)) }
        }
        val costs = Array(map.map.size) { IntArray(map.map[it].size) { Int.MAX_VALUE } }

        fun fill(x: Int, y: Int, cost: Int) {
            if (costs[y][x] <= cost) return
            costs[y][x] = cost
            map.reachableNeighbours(x, y).forEach { (x, y) ->
                fill(x, y, cost + 1)
            }
        }

        starts.forEach { start ->
            fill(start.first, start.second, 0)
        }

        val end = map.end()

        return costs[end.second][end.first]
    }

    fun part1(input: List<String>): Int {
        val map = parseMap(input)
        return routeCost(map)
    }


    fun part2(input: List<String>): Int {
        val map = parseMap(input)
        return routeCost(map, 'a')
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 31)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 29)
    println(part2(input))

}
