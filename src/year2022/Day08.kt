package year2022

import readInput

fun main() {

    data class Forest(val heightMap: Array<IntArray>) {
        private val directions = listOf(
            1 to 0,
            -1 to 0,
            0 to 1,
            0 to -1
        )

        fun heightAt(row: Int, col: Int): Int? {
            if (heightMap.indices.contains(row) && heightMap[row].indices.contains(col)) {
                return heightMap[row][col]
            }
            return null
        }

        fun positions(): List<Pair<Int, Int>> {
            return heightMap.indices.flatMap { row ->
                heightMap[row].indices.map { col ->
                    row to col
                }
            }
        }

        fun heightsInDirection(row: Int, col: Int, dRow: Int, dCol: Int): List<Int> {
            return generateSequence(row to col) { (row, col) ->
                row + dRow to col + dCol
            }.drop(1).map { (row, col) ->
                heightAt(row, col)
            }.takeWhile { it != null }
                .filterNotNull()
                .toList()
        }

        fun isVisible(row: Int, col: Int): Boolean {
            val testHeight = heightAt(row, col) ?: return false
            return directions.any { (dRow, dCol) ->
                heightsInDirection(row, col, dRow, dCol).none { it >= testHeight }
            }
        }

        fun scenicScore(row: Int, col: Int): Int {
            val testHeight = heightAt(row, col) ?: return 0
            return directions.map { (dRow, dCol) ->
                val heights = heightsInDirection(row, col, dRow, dCol)
                val index = heights.indexOfFirst { it >= testHeight }
                if (index == -1) {
                    heights.size
                } else {
                    index + 1
                }
            }.reduce(Int::times)
        }
    }

    val inputClassifier = "Day08"

    fun parseForest(input: List<String>): Forest {
        return Forest(input.map { line ->
            line.map { it.digitToInt() }.toIntArray()
        }.toTypedArray())
    }

    fun part1(input: List<String>): Int {
        val forest = parseForest(input)
        val visibility = forest.positions().map { it to forest.isVisible(it.first, it.second) }.toMap()
        return forest.positions().count { forest.isVisible(it.first, it.second) }
    }


    fun part2(input: List<String>): Int {
        val forest = parseForest(input)
        return forest.positions().maxOf { forest.scenicScore(it.first, it.second) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 21)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 8)
    println(part2(input))

}
