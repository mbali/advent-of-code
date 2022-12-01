package year2021

import benchmark
import readInput

fun main() {

    fun toMeasurements(input: List<String>) = input
        .map { it.toCharArray().map { digit -> digit.digitToInt(2) } }

    fun List<Int>.sortedCountsByValues(): List<Pair<Int, Int>> = groupingBy { it }
        .eachCount()
        .toList()
        .sortedBy { it.second }

    fun List<Int>.binaryDigitsToInt(): Int = joinToString("").toInt(2)

    fun mostCommonBit(measurements: List<List<Int>>, position: Int): Int {
        val (bit, count) = measurements
            .map { it[position] }
            .sortedCountsByValues()
            .last()
        return if (count * 2 == measurements.size)
            1 //if the counts are exactly the same then return 1
        else
            bit
    }

    fun leastCommonBit(measurements: List<List<Int>>, position: Int): Int {
        //cannot be 1-mostCommonBit (in case all bits are the same)
        val (bit, count) = measurements
            .map { it[position] }
            .sortedCountsByValues()
            .first()
        return if (count * 2 == measurements.size)
            0 //if the counts are exactly the same then return 0
        else
            bit
    }

    fun part1(input: List<String>): Int {
        val measurements = toMeasurements(input)
        val bitLength = measurements.first().size
        val gamma = List(bitLength) { idx -> mostCommonBit(measurements, idx) }.binaryDigitsToInt()
        val epsilon = List(bitLength) { idx -> leastCommonBit(measurements, idx) }.binaryDigitsToInt()

        return gamma * epsilon
    }

    fun reduceCandidates(measurements: List<List<Int>>, bitProvider: (List<List<Int>>, Int) -> Int): Int {
        val candidates = measurements.toMutableList()
        var position = 0
        while (position < measurements.first().size && candidates.size > 0) {
            val filterBit = bitProvider(candidates, position)
            candidates.removeAll { it[position] != filterBit }
            position++
        }
        return candidates.first().binaryDigitsToInt()
    }

    fun part2(input: List<String>): Int {
        val measurements = toMeasurements(input)
        val o2GeneratorRating = reduceCandidates(measurements, ::mostCommonBit)
        val co2ScrubberRating = reduceCandidates(measurements, ::leastCommonBit)
        return o2GeneratorRating * co2ScrubberRating
    }

    val testInput = readInput(2021, "Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput(2021, "Day03")
    println(part1(input))
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}