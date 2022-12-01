package year2021

import benchmark
import readInput

private typealias HeightMap = Array<Array<Int>>


private fun List<String>.asHeightMap(): HeightMap = this.map {
    Regex("""\d""").findAll(it).map { digit -> digit.value.toInt() }.toList().toTypedArray()
}.toTypedArray()


private fun HeightMap.hasPosition(row: Int, col: Int) = row in this.indices && col in this[row].indices

private val neigboursDirections = listOf(
    0 to 1,
    0 to -1,
    1 to 0,
    -1 to 0
)

fun main() {

    fun part1(input: List<String>): Int {
        val heightMap = input.asHeightMap()
        val lowPointPositions = buildList {
            heightMap
                .indices.forEach { row ->
                    heightMap[row].indices.forEach { col ->
                        if (
                            neigboursDirections.map { (row + it.first) to (col + it.second) }
                                .filter { heightMap.hasPosition(it.first, it.second) }
                                .all { heightMap[it.first][it.second] > heightMap[row][col] })
                            this.add(row to col)
                    }
                }
        }
        return lowPointPositions.sumOf { heightMap[it.first][it.second] + 1 }
    }

    fun flood(from: Pair<Int, Int>, basinIndex: Int, heightMap: HeightMap, basinMap: HeightMap) {
        if (heightMap[from.first][from.second] == 9 || basinMap[from.first][from.second] >= 0) return
        basinMap[from.first][from.second] = basinIndex
        neigboursDirections.map { it.first + from.first to it.second + from.second } //neighbour candidates
            .filter { heightMap.hasPosition(it.first, it.second) } //bound into map
            .forEach {
                flood(it, basinIndex, heightMap, basinMap) //flood from neigbours
            }
    }

    fun part2(input: List<String>): Int {
        val heightMap = input.asHeightMap()
        val basinMap: HeightMap = Array(heightMap.size) { row ->
            Array(heightMap[row].size) { -1 }
        }
        var basinIndex = 0
        do {
            val seed = heightMap.indices.firstNotNullOfOrNull { row ->
                heightMap[row].indices.firstNotNullOfOrNull { col ->
                    if (heightMap[row][col] < 9 && basinMap[row][col] < 0) row to col
                    else null
                }
            }
            seed?.let { flood(it, basinIndex++, heightMap, basinMap) }
        } while (seed != null)
        return basinMap.flatMap { it.toList() }
            .filter { it > 0 } //remove walls
            .groupingBy { it }//group by basin index
            .eachCount().values//get the counts
            .sortedDescending()
            .take(3).reduce { a, b -> a * b }
    }

    val testInput = readInput(2021, "Day09_test")
    check(part1(testInput) == 15)

    val input = readInput(2021, "Day09")
    println(part1(input))

    check(part2(testInput) == 1134)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
