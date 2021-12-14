fun main() {

    class Parser constructor(val input: String) {
        private val closingPairs = mapOf(
            '(' to ')',
            '[' to ']',
            '{' to '}',
            '<' to '>'
        )

        val expectedClosers = ArrayDeque<Char>()
        val firstErrorPosition: Int?

        init {
            var errorPosition: Int? = null
            input.toCharArray().forEachIndexed { idx, char ->
                if (char in closingPairs.keys) {
                    expectedClosers.addFirst(closingPairs.getValue(char))
                } else {
                    if (expectedClosers.removeFirstOrNull() != char) {
                        errorPosition = idx
                        return@forEachIndexed
                    }
                }
            }
            firstErrorPosition = errorPosition
        }

        fun isIncomplete() = expectedClosers.isNotEmpty() && firstErrorPosition == null
    }

    fun part1(input: List<String>): Int {
        val scores = mapOf(
            ')' to 3,
            ']' to 57,
            '}' to 1197,
            '>' to 25137
        )

        return input
            .map { Parser(it) }
            .filter { it.firstErrorPosition != null }
            .map { it.input[it.firstErrorPosition!!] }
            .sumOf { scores.getValue(it) }
    }

    fun part2(input: List<String>): Long {
        val valuesOfClosers = mapOf(
            ')' to 1,
            ']' to 2,
            '}' to 3,
            '>' to 4
        )
        val scores = input
            .map { Parser(it) }
            .filter { it.isIncomplete() }
            .map {
                it.expectedClosers
                    .map { char -> valuesOfClosers.getValue(char) }
                    .fold(0L) { acc, score -> acc * 5 + score }
            }
            .sorted()
        check(scores.size.mod(2) == 1) { "Should have an odd result set" }
        return scores[scores.size / 2]
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397)

    val input = readInput("Day10")
    println(part1(input))

    check(part2(testInput) == 288957L)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
