package year2022

import readInput
import year2022.Day17.Jet.LEFT
import year2022.Day17.Jet.RIGHT

object Day17 {
    enum class Shape(val asInitialLines: List<Int>) {
        //these are flipped upside down
        HORIZONTAL(
            listOf(
                0b00011110
            )
        ),
        VERTICAL(
            listOf(
                0b00010000,
                0b00010000,
                0b00010000,
                0b00010000,
            )
        ),
        SQUARE(
            listOf(
                0b00011000,
                0b00011000,
            )
        ),
        CROSS(
            listOf(
                0b00001000,
                0b00011100,
                0b00001000,
            )
        ),
        J(
            listOf(
                0b00011100,
                0b00000100,
                0b00000100,
            )
        )
        ;


        companion object {
            fun dispenser() = sequence {
                while (true) {
                    yield(HORIZONTAL)
                    yield(CROSS)
                    yield(J)
                    yield(VERTICAL)
                    yield(SQUARE)
                }
            }
        }
    }

    enum class Jet {
        LEFT, RIGHT;

        companion object {
            private fun valueOf(char: Char) = when (char) {
                '<' -> LEFT
                '>' -> RIGHT
                else -> throw IllegalArgumentException("Invalid jet: $char")
            }

            fun patternOf(string: String) = sequence {
                while (true) {
                    yieldAll(string.mapIndexed { index, c -> index to valueOf(c) })
                }
            }
        }
    }

    fun Jet.shift(lines: List<Int>) =
        when (this) {
            LEFT -> lines.rotate(-1)
            RIGHT -> lines.rotate(1)
        }


    fun List<Int>.rotate(n: Int) = map {
        when {
            n == 0 -> it
            n > 0 -> it.rotateRight(n)
            else -> it.rotateLeft(-n)
        }
    }

    data class Chamber(val jetPattern: String, var bottom: Long = 0L, var lines: List<Int> = emptyList()) {
        private val jets = Jet.patternOf(jetPattern).iterator()
        private val shapeDispenser = Shape.dispenser().iterator()

        private var jetIndex = jetPattern.indices.last
        private lateinit var lastShape: Shape

        private fun getLine(y: Long): Int = lines.getOrNull((y - bottom - 1).toInt()) ?: 0
        private fun setLine(y: Long, line: Int) {
            val index = (y - bottom - 1).toInt()
            if (index >= lines.size) {
                lines = lines + List(index - lines.size + 1) { 0 }
            }
            lines = lines.mapIndexed { i, l -> if (i == index) line else l }
        }

        private fun nextJet(): Jet {
            val next = jets.next()
            jetIndex = next.first
            return next.second
        }

        private fun nextShape(): Shape {
            lastShape = shapeDispenser.next()
            return lastShape
        }


        private fun raiseFloorTo(newFloor: Long) {
            if (newFloor > bottom) {
                lines = lines.drop((newFloor - bottom).toInt())
                bottom = newFloor
            }
        }

        private fun valid(lines: List<Int>, y: Long): Boolean {
            return lines.mapIndexed { index, line ->
                index + y to line
            }.none { (y, line) ->
                y <= bottom || line >= 128 || line < 0 || getLine(y) and line != 0
            }
        }

        private fun addRocks(lines: List<Int>, yOffset: Long) {
            lines.mapIndexed { index, line ->
                index + yOffset to line
            }.forEach { (y, line) ->
                setLine(y, getLine(y) or line)
            }
            lines.indices.reversed().forEach { index ->
                val candidateBottom = index + yOffset
                if (Shape.values().flatMap { shape -> (-2..4).map { rot -> shape.asInitialLines.rotate(rot) } }
                        .none { valid(it, candidateBottom) }) {
                    raiseFloorTo(candidateBottom)
                    return
                }
            }
        }

        fun blockHeight() = bottom + lines.count { it != 0 }

        fun drop(shape: Shape = nextShape()) {
            val currentTop = blockHeight()

            //using 1 based indexing
            var offset = currentTop + 4
            var fallingLines = shape.asInitialLines
            while (true) {
                val jet = nextJet()
                jet.shift(fallingLines).takeIf { valid(it, offset) }?.let { fallingLines = it }
                if (valid(fallingLines, offset - 1)) {
                    offset--
                } else {
                    addRocks(fallingLines, offset)
                    break
                }
            }
        }

        fun dropWithMemo(repeats: Long) {
            data class MemoKey(val shape: Shape, val jetIndex: Int, val lines: List<Int>)
            data class MemoValue(val bottom: Long, val rounds: Long)

            val memo = mutableMapOf<MemoKey, MemoValue>()

            var currentStep = 0L
            while (currentStep < repeats) {
                currentStep++
                val shape = nextShape()
                val key = MemoKey(shape, jetIndex, lines)
                drop(shape)
                val newMemo = MemoValue(bottom, currentStep)
                if (key in memo) {
                    //found a cycle!!!!!
                    val oldMemo = memo.getValue(key)
                    val cycleLength = currentStep - oldMemo.rounds
                    val increaseByCycle = newMemo.bottom - oldMemo.bottom
                    val remainingCycles = (repeats - currentStep) / cycleLength
                    bottom += increaseByCycle * remainingCycles
                    val remainingSteps = (repeats - currentStep).rem(cycleLength)
                    //keep the iterators in sync
                    repeat(remainingSteps.toInt()) {
                        drop()
                    }
                    return
                }
                memo[key] = newMemo
            }
        }
    }

}

fun main() {

    val inputClassifier = "Day17"

    fun solution(input: List<String>, repeats: Long): Long {
        val chamber = Day17.Chamber(input.first())
        chamber.dropWithMemo(repeats)
        return chamber.blockHeight()
    }

    fun part1(input: List<String>) = solution(input, 2022)


    fun part2(input: List<String>) = solution(input, 1_000_000_000_000L)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 3068L)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 1514285714288L)
    println(part2(input))

}
