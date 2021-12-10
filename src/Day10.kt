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
            .sumOf { scores.getOrDefault(it, 0) }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397)

    val input = readInput("Day10")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
