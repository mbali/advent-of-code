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

private fun Pair<Int, Int>.isNeighbour(other: Pair<Int, Int>): Boolean = neigboursDirections
    .map { first + it.first to second + it.second }
    .any { it == other }

fun main() {

    fun part1(input: List<String>): Int {
        val heightMap = input.asHeightMap()
        val lowPointPositions = buildList<Pair<Int, Int>> {
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

    fun part2(input: List<String>): Int {
        val heightMap = input.asHeightMap()
        val nonFloodedPositions = heightMap.indices.flatMap { row ->
            heightMap[row].indices.filter { col -> heightMap[row][col] < 9 }.map { row to it }
        }.toMutableList()
        val basins = mutableListOf<List<Pair<Int, Int>>>()
        while (nonFloodedPositions.isNotEmpty()) {
            val basin = mutableListOf(nonFloodedPositions.removeFirst())
            do {
                val connectedPositions =
                    nonFloodedPositions.filter { candidate -> basin.any { it.isNeighbour(candidate) } }
                basin.addAll(connectedPositions)
                nonFloodedPositions.removeAll(connectedPositions)
            } while (connectedPositions.isNotEmpty())
            basins.add(basin)
        }
        return basins.map { it.size }.sortedDescending().take(3).reduce { a, b -> a * b }
    }

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)

    val input = readInput("Day09")
    println(part1(input))

    check(part2(testInput) == 1134)
    println(part2(input))
}
