package year2022

import readInput
import year2022.Day16.solve
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.max

object Day16 {
    private val VALVE_REGEX = Regex("""^Valve ([A-Z]+) .*flow rate=(\d+); .* to valves? (.*)""")

    data class Valve(val id: String, val flowRate: Int, val paths: Map<String, Int>)

    private fun String.parseValve(): Valve {
        val (id, flowRate, neighbours) = VALVE_REGEX.matchEntire(this)!!.destructured
        return Valve(id, flowRate.toInt(), neighbours.split(", ").associateWith { 1 })
    }

    private fun List<String>.parseValves(): List<Valve> = map { it.parseValve() }

    private fun List<Valve>.simplify(): List<Valve> {
        this.flatMap { it.paths.values }.find { it != 1 }
            ?.let { throw IllegalStateException("Map is already simplified?") }

        fun calculateDistancesFrom(valve: Valve): Map<String, Int> = buildMap<String, Int> {
            val queue = ArrayDeque<Pair<String, Int>>()
            queue.add(valve.id to 0)
            while (queue.isNotEmpty()) {
                val (v, d) = queue.removeFirst()
                if (v in this) continue
                this[v] = d
                this@simplify.first { it.id == v }.paths.forEach { (n, _) -> queue.add(n to d + 1) }
            }
            remove(valve.id)
            keys.filter { k -> this@simplify.first { it.id == k }.flowRate == 0 }.forEach { remove(it) }
        }

        return this.map {
            it.copy(paths = calculateDistancesFrom(it))
        }
    }

    fun solve(input: List<String>, timeLimit: Int, part2: Boolean = false): Int {
        val valves = input.parseValves().simplify().associateBy { it.id }
        val keys = valves.keys.sorted().toTypedArray();

        val paths = valves.values.associate { v -> v.id to v.paths }
        val memo = mutableMapOf<Set<String>, Int>()

        val startId = "AA"

        fun fillMemo(
            currentId: String = startId,
            currentFlow: Int = 0,
            remainingTime: Int = timeLimit,
            visited: Set<String> = emptySet()
        ): Int {
            val newVisited = visited + currentId
            val memoKey = newVisited - startId
            memo[memoKey] = max(memo[memoKey] ?: 0, currentFlow)
            var bestFlow = 0
            for ((nextId, distance) in paths.getValue(currentId)) {
                if (nextId in newVisited) continue
                val remainingTimeAfter = remainingTime - distance - 1
                if (remainingTimeAfter <= 0) continue
                val additionalFlow = valves.getValue(nextId).flowRate * remainingTimeAfter
                val newFlow =
                    fillMemo(nextId, currentFlow + additionalFlow, remainingTimeAfter, newVisited) + additionalFlow
                if (newFlow > bestFlow) bestFlow = newFlow
            }
            return bestFlow
        }


        val part1Score = fillMemo()
        if (!part2) {
            return part1Score
        }

        println(memo.size)

        var bestFlow = 0
        for (human in memo.keys) {
            memo.keys.filter { it != human && it.intersect(human).isEmpty() }.forEach { elephant ->
                val newFlow = memo.getValue(human) + memo.getValue(elephant)
                if (newFlow > bestFlow) bestFlow = newFlow
            }
        }
        return bestFlow
    }

}

fun main() {

    val inputClassifier = "Day16"

    fun part1(input: List<String>): Int {
        return solve(input, 30)
    }


    fun part2(input: List<String>): Int {
        return solve(input, 26, true)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 1651)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 1707)
    println(part2(input))
}
