package year2021

import benchmark
import readInput

private data class PuzzleEntry(val patterns: List<String>, val output: List<String>)

private fun String.toBits() =
    ('a'..'g').joinToString(separator = "") { if (this.contains(it)) "1" else "0" }

private fun Char.and(other: Char): Char = if (this == '1' && other == '1') '1' else '0'
private fun Char.or(other: Char): Char = if (this == '1' || other == '1') '1' else '0'

private fun String.and(other: String) = this.zip(other).map { it.first.and(it.second) }.joinToString("")
private fun String.or(other: String) = this.zip(other).map { it.first.or(it.second) }.joinToString("")


private fun String.toPuzzleEntry(): PuzzleEntry {
    val patterns = Regex("""[a-g]+""").findAll(this).map { it.value.toBits() }.toList()
    check(patterns.size == 14) { "Single entry should contain 14 patterns" }
    return PuzzleEntry(patterns.take(10), patterns.takeLast(4))
}

private fun String.popCnt() = this.count { it == '1' }

private class Decoder constructor(digitPatterns: List<String>) {
    val patternsToValues: Map<String, Int>

    init {
        val patternsByDigits: Array<String?> = arrayOfNulls(10)
        //1 has 2 segments
        patternsByDigits[1] = digitPatterns.single { it.popCnt() == 2 }
        //4 has 4 segments
        patternsByDigits[4] = digitPatterns.single { it.popCnt() == 4 }
        //7 has 3 segments
        patternsByDigits[7] = digitPatterns.single { it.popCnt() == 3 }
        //8 has 7 segments
        patternsByDigits[8] = digitPatterns.single { it.popCnt() == 7 }
        //5 segments: 2, 3, 5
        //3 has 5 segments and has 2 segments common with 1
        patternsByDigits[3] = digitPatterns.single {
            it.popCnt() == 5
                    && patternsByDigits[1]!!.and(it).popCnt() == 2
        }
        //2 has 5 segments and has 2 segments common with 4 (and it is not 3)
        patternsByDigits[2] = digitPatterns.single {
            it.popCnt() == 5
                    && patternsByDigits[4]!!.and(it).popCnt() == 2
                    && it != patternsByDigits[3]
        }
        //5 is the remaining 5 segment number
        patternsByDigits[5] = digitPatterns.single {
            it.popCnt() == 5
                    && it != patternsByDigits[2]
                    && it != patternsByDigits[3]
        }
        //6 segments: 0, 6, 9
        //6: the only 6 segment digit that has only one common segment with one
        patternsByDigits[6] = digitPatterns.single {
            it.popCnt() == 6
                    && patternsByDigits[1]!!.and(it).popCnt() == 1
        }
        //9 can be constructed from 4 and a 5
        patternsByDigits[9] = patternsByDigits[4]!!.or(patternsByDigits[5]!!)
        //0: 6 segments, is not 6 or 9
        patternsByDigits[0] = digitPatterns.single {
            it.popCnt() == 6
                    && it != patternsByDigits[6]
                    && it != patternsByDigits[9]
        }
        patternsToValues = patternsByDigits.requireNoNulls().mapIndexed { idx, value -> value to idx }.toMap()
    }

    fun decode(digits: List<String>): Int {
        check(digits.all { patternsToValues.containsKey(it) }) { "Not all digits have corresponding patterns" }
        return digits.joinToString(separator = "") { patternsToValues.getValue(it).toString() }.toInt()
    }
}


fun main() {

    fun part1(input: List<String>) =
        input.map { it.toPuzzleEntry() }
            .flatMap { entry -> entry.output.map { it.popCnt() } }
            .count { setOf(2, 3, 4, 7).contains(it) }


    fun part2(input: List<String>): Int =
        input.map { it.toPuzzleEntry() }
            .map { it to Decoder(it.patterns) }
            .sumOf { (entry, decoder) -> decoder.decode(entry.output) }

    val testInput = readInput(2021, "Day08_test")
    check(part1(testInput) == 26)

    val input = readInput(2021, "Day08")
    println(part1(input))

    check(part2(testInput) == 61229)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
