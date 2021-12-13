fun main() {
    class FoldInstruction(val x: Int? = null, val y: Int? = null) {
        fun transform(positions: Set<Pair<Int, Int>>): Set<Pair<Int, Int>> =
            positions.mapNotNull { transform(it) }.normalize().toSet()

        private fun transform(position: Pair<Int, Int>): Pair<Int, Int>? =
            if (position.first == x || position.second == y) null
            else foldAt(position.first, x) to foldAt(position.second, y)

        private fun foldAt(what: Int, where: Int?) =
            if (where == null || what < where) what
            else 2 * where - what

        private fun Collection<Pair<Int, Int>>.normalize(): List<Pair<Int, Int>> {
            val minX = this.minOf { it.first }
            val minY = this.minOf { it.second }
            return this.map { it.first - minX to it.second - minY }
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

    fun part1(input: List<String>): Int {
        val puzzle = input.parseInput()
        return puzzle.instructions.take(1).fold(puzzle.dots) { dots, instruction -> instruction.transform(dots) }
            .count()
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)

    val input = readInput("Day13")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
