fun main() {
    fun List<String>.toExpansions(): Map<String, String> {
        val expansionRegex = Regex("""(?<from>..) -> (?<insert>.)""")
        return this@toExpansions.mapNotNull { line ->
            expansionRegex.matchEntire(line)?.let { it ->
                val from = it.groups["from"]!!.value
                val insert = it.groups["insert"]!!.value.first()
                from to listOf(from[0], insert, from[1]).joinToString("")
            }
        }.toMap()
    }

    fun String.expand(rules: Map<String, String>): String =
        this.windowed(2)
            .joinToString("") { pair ->
                rules.getOrDefault(pair, pair).dropLast(1)
            } + this.last()

    fun score(polymer: String): Int {
        val counts = polymer.groupingBy { it }.eachCount()
        return counts.maxOf { it.value } - counts.minOf { it.value }
    }

    fun part1(input: List<String>): Int {
        val template = input.first()
        val rules = input.drop(2).toExpansions()
        return score(generateSequence(template) {
            it.expand(rules)
        }.elementAt(10))
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588)

    val input = readInput("Day14")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
