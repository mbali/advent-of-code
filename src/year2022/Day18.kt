package year2022

import Vec3
import manhattanDistance
import neighbours
import readInput

fun main() {
    fun String.toVec3() = split(",").let { (x, y, z) -> Vec3(x.toInt(), y.toInt(), z.toInt()) }

    fun parse(input: List<String>): List<Vec3> {
        return input.map { it.toVec3() }
    }


    fun exterior(init: List<Vec3>): List<Vec3> {
        if (init.isEmpty()) return emptyList()

        val xRange = init.map { it.x }.let { (it.min() - 1)..(it.max() + 1) }
        val yRange = init.map { it.y }.let { (it.min() - 1)..(it.max() + 1) }
        val zRange = init.map { it.z }.let { (it.min() - 1)..(it.max() + 1) }

        val visited = mutableSetOf<Vec3>()
        visited.addAll(init)
        return buildList {
            val queue = ArrayDeque<Vec3>()
            queue.add(Vec3(xRange.first, yRange.first, zRange.first))
            while (!queue.isEmpty()) {
                val v = queue.removeFirst()
                if (v in visited) continue
                if (v.x !in xRange || v.y !in yRange || v.z !in zRange) continue
                visited.add(v)
                add(v)
                queue.addAll(v.neighbours(orthogonal = true))
            }
        }
    }

    val inputClassifier = "Day18"

    fun part1(input: List<String>): Int {
        val obsidian = parse(input)
        println("""
            Cube count: ${obsidian.size}
            X range: ${obsidian.minOfOrNull { it.x }}..${obsidian.maxOfOrNull { it.x }}
            Y range: ${obsidian.minOfOrNull { it.y }}..${obsidian.maxOfOrNull { it.y }}
            Z range: ${obsidian.minOfOrNull { it.z }}..${obsidian.maxOfOrNull { it.z }}
        """.trimIndent()
        )
        return obsidian.sumOf { c ->
            6 - obsidian.count { c2 -> c2.manhattanDistance(c) == 1 }
        }
    }


    fun part2(input: List<String>): Int {
        val obsidian = parse(input)
        val exterior = exterior(obsidian)
        return obsidian.sumOf { c ->
            exterior.count { e -> e.manhattanDistance(c) == 1 }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 64)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 58)
    println(part2(input))

}
