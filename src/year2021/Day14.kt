package year2021

import benchmark
import readInput

fun main() {

    class Polymer(val start: Char, val end: Char, val pairCounts: Map<String, Long>) {
        fun expand(rules: Map<String, String>): Polymer {
            val newCounts = pairCounts
                .flatMap { (pair, count) ->
                    rules.getOrDefault(pair, pair).windowed(2).map { it to count } //maybe expand pair to 2 new pairs
                }
                .groupingBy { it.first }
                .fold(0L) { acc, element -> acc + element.second }
            return Polymer(start, end, newCounts)
        }

        fun score(): Long {
            val counts = buildMap<Char, Long> {
                //first and last chars are in only one pair
                put(start, 1L)
                put(end, 1L)
                pairCounts.forEach { (pair, count) ->
                    pair.forEach {
                        merge(it, count) { acc, v -> acc + v }
                    }
                }
            }.values.map { it / 2 } //each value is counted twice
            return counts.maxOf { it } - counts.minOf { it }
        }
    }

    fun String.toPolymer(): Polymer {
        check(length >= 2) { "Template should be at least 2 characters long" }
        val counts = this.windowed(2).groupingBy { it }.eachCount().mapValues { (_, v) -> v.toLong() }
        return Polymer(first(), last(), counts)
    }

    fun List<String>.toExpansions(): Map<String, String> {
        val expansionRegex = Regex("""(?<from>..) -> (?<insert>.)""")
        return this.mapNotNull { line ->
            expansionRegex.matchEntire(line)?.let { it ->
                val from = it.groups["from"]!!.value
                val insert = it.groups["insert"]!!.value.first()
                from to listOf(from[0], insert, from[1]).joinToString("")
            }
        }.toMap()
    }

    fun solution(input: List<String>, steps: Int): Long {
        val template = input.first().toPolymer()
        val rules = input.drop(2).toExpansions()
        return generateSequence(template) {
            it.expand(rules)
        }.elementAt(steps).score()
    }

    fun part1(input: List<String>): Long {
        return solution(input, 10)
    }

    fun part2(input: List<String>): Long {
        return solution(input, 40)
    }

    val testInput = readInput(2021, "Day14_test")
    check(part1(testInput) == 1588L)

    val input = readInput(2021, "Day14")
    println(part1(input))
    check(part2(testInput) == 2188189693529L)
    println(part2(input))
    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
