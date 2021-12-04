fun main() {
    class BingoBoard(val numbers: Array<Int>) {
        init {
            check(numbers.size == 25)
        }

        private val marks = BooleanArray(25)

        fun mark(number: Int): Int? {
            val idx = numbers.indexOf(number)
            if (idx < 0) return null
            marks[idx] = true
            val bingo = (0..4).map { marks[idx / 5 * 5 + it] }.reduce { acc, mark -> acc && mark } ||
                    (0..4).map { marks[idx % 5 + it * 5] }.reduce { acc, mark -> acc && mark }
            if (!bingo) return null
            return numbers.zip(marks.toTypedArray()).filter { !it.second }.sumOf { it.first } * number
        }
    }

    fun parseInput(input: List<String>): Pair<List<Int>, List<BingoBoard>> {
        val draws = input.first().split(",").map { it.toInt() }
        val boards = input.drop(2).filter { it.isNotEmpty() }.chunked(5) { board ->
            board.flatMap { line ->
                line.split("""\s+""".toRegex()).filter { it.isNotEmpty() }.map { it.toInt() }
            }.toTypedArray().let { BingoBoard(it) }
        }
        return draws to boards
    }

    fun part1(input: List<String>): Int {
        val (draws, boards) = parseInput(input)
        draws.forEach { draw ->
            boards.forEach { board ->
                val score = board.mark(draw)
                if (score != null) {
                    return score
                }
            }
        }
        SHOULD_NOT_REACH()
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)

    val input = readInput("Day04")
    println(part1(input))

    check(part2(testInput) == -1)
    println(part2(input))

}