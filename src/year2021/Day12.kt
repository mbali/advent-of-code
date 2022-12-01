package year2021

import benchmark
import readInput

fun main() {

    fun isSmall(id: String) = id.all { it.isLowerCase() }

    fun toNeigbourhoods(input: List<String>, start: String = "start", end: String = "end"): Map<String, List<String>> {
        val vertices =
            input.flatMap { line ->
                val (from, to) = line.split('-')
                listOf(
                    from to to,
                    to to from
                ).filter { it.second != start && it.first != end } //start is a source, end is a sink
            }.toSet()
        return vertices.groupBy { it.first }.mapValues { v -> v.value.map { it.second }.toList() }
    }

    fun routes(
        neigbourhoods: Map<String, List<String>>,
        prefix: List<String> = emptyList(),
        currentNode: String = "start",
        targetNode: String = "end",
        routeValidator: (List<String>) -> Boolean
    ): List<List<String>> {
        val route = prefix + currentNode
        if (!routeValidator(route)) {
            return emptyList()
        }
        if (currentNode == targetNode) {
            return listOf(route)
        }
        return neigbourhoods.getValue(currentNode).flatMap { next ->
            routes(neigbourhoods, route, next, targetNode, routeValidator)
        }
    }


    fun part1(input: List<String>): Int {
        return routes(toNeigbourhoods(input)) { route ->
            route.filter { isSmall(it) }.groupingBy { it }.eachCount().all { it.value <= 1 }
        }.size
    }

    fun part2(input: List<String>): Int {
        return routes(toNeigbourhoods(input)) { route ->
            val counts = route.filter { isSmall(it) }.groupingBy { it }.eachCount().values
            counts.all { it <= 2 } && counts.count { it == 2 } <= 1
        }.size
    }

    val testInput1 = readInput(2021, "Day12_test1")
    val testInput2 = readInput(2021, "Day12_test2")
    val testInput3 = readInput(2021, "Day12_test3")
    check(part1(testInput1) == 10)
    check(part1(testInput2) == 19)
    check(part1(testInput3) == 226)

    val input = readInput(2021, "Day12")
    println(part1(input))

    check(part2(testInput1) == 36)
    check(part2(testInput2) == 103)
    check(part2(testInput3) == 3509)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 30) { part1(input) }
        benchmark("part2", 30) { part2(input) }
    }
}
