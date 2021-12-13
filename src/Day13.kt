fun main() {
    class FoldInstruction(val x: Int? = null, val y: Int? = null) {
        fun transform(positions: Set<Pair<Int, Int>>): Set<Pair<Int, Int>> =
            positions.mapNotNull { it.transform() }.normalize()

        private fun Pair<Int, Int>.transform(): Pair<Int, Int>? =
            if (first == x || second == y) null
            else first.foldAt(x) to second.foldAt(y)

        private fun Int.foldAt(where: Int?) =
            if (where == null || this < where) this
            else 2 * where - this

        private fun Collection<Pair<Int, Int>>.normalize(): Set<Pair<Int, Int>> {
            val minX = this.minOf { it.first }
            val minY = this.minOf { it.second }
            return this.map { it.first - minX to it.second - minY }.toSet()
        }
    }

    data class PuzzleInput(val dots: Set<Pair<Int, Int>>, val instructions: List<FoldInstruction>)

    fun List<String>.parseInput(): PuzzleInput {
        val dots = mutableSetOf<Pair<Int, Int>>()
        val instructions = mutableListOf<FoldInstruction>()
        val positionRegex = Regex("""(?<x>\d+),(?<y>\d+)""")
        val instructionRegex = Regex("""fold along (?<axis>.)=(?<value>\d+)""")
        this.forEach { line ->
            positionRegex.matchEntire(line)?.let {
                dots.add(
                    it.groups["x"]!!.value.toInt() to
                            it.groups["y"]!!.value.toInt()
                )
            }
            instructionRegex.matchEntire(line)?.let {
                val axis = it.groups["axis"]!!.value
                val value = it.groups["value"]!!.value.toInt()
                instructions.add(
                    when (axis) {
                        "x" -> FoldInstruction(x = value)
                        "y" -> FoldInstruction(y = value)
                        else -> error("Invalid axis $axis")
                    }
                )
            }
        }
        return PuzzleInput(dots, instructions)
    }

    fun Set<Pair<Int, Int>>.printCode(header: String) {
        val maxX = this.maxOf { it.first }
        val maxY = this.maxOf { it.second }
        println(header)
        println("----------------")
        for (y in 0..maxY) {
            for (x in 0..maxX) {
                print(if (x to y in this) "\u2588" else " ")
            }
            println()
        }
        println("----------------")
    }

    fun part1(input: List<String>): Int {
        val puzzle = input.parseInput()
        return puzzle.instructions.take(1).fold(puzzle.dots) { dots, instruction -> instruction.transform(dots) }
            .count()
    }

    fun part2(header: String, input: List<String>): Int {
        val puzzle = input.parseInput()
        val result = puzzle.instructions.fold(puzzle.dots) { dots, instruction -> instruction.transform(dots) }
        result.printCode(header)
        return result.count()
    }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)

    val input = readInput("Day13")
    println(part1(input))

    check(part2("TEST", testInput) == 16)
    println(part2("REAL", input))
}
