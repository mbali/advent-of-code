private data class PuzzleEntry(val patterns: List<String>, val output: List<String>)

private fun String.toBits() =
    ('a'..'g').joinToString(separator = "") { if (this.contains(it)) "1" else "0" }


private fun String.toPuzzleEntry(): PuzzleEntry {
    val patterns = Regex("""[a-g]+""").findAll(this).map { it.value.toBits() }.toList()
    check(patterns.size == 14) { "Single entry should contain 14 patterns" }
    return PuzzleEntry(patterns.take(10), patterns.takeLast(4))
}

private fun String.popCnt() = this.count { it == '1' }

fun main() {

    fun part1(input: List<String>): Int =
        input.map { it.toPuzzleEntry() }
            .flatMap { entry -> entry.output.map { it.popCnt() } }
            .count { setOf(2, 3, 4, 7).contains(it) }


    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)

    val input = readInput("Day08")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
