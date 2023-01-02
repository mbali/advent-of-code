package year2022

import readInput
import splitAt

object Day11 {
    sealed class Operand {
        object Old : Operand()
        data class Literal(val value: Int) : Operand()

        fun value(input: Long) =
            when (this) {
                is Old -> input
                is Literal -> value.toLong()
            }


        companion object {
            fun parse(input: String) = when (input) {
                "old" -> Old
                else -> Literal(input.toInt())
            }
        }
    }

    sealed class Operator {
        object Add : Operator()
        object Multiply : Operator()

        fun apply(value: Long, left: Operand, right: Operand) =
            when (this) {
                is Add -> left.value(value) + right.value(value)
                is Multiply -> left.value(value) * right.value(value)
            }

        companion object {
            fun parse(input: String) = when (input) {
                "+" -> Add
                "*" -> Multiply
                else -> throw IllegalArgumentException("Unknown operator $input")
            }
        }
    }

    data class Calculation(val operator: Operator, val left: Operand, val right: Operand) {
        fun apply(value: Long) = operator.apply(value, left, right)

        companion object {
            fun parse(input: String): Calculation {
                val (left, operator, right) = input.split(" ")
                return Calculation(Operator.parse(operator), Operand.parse(left), Operand.parse(right))
            }
        }
    }

    data class Monkey(
        val id: Int,
        val operation: Calculation,
        val testDivisor: Int,
        val onTrue: Int,
        val onFalse: Int,
        val lowerWorry: Boolean = true,
        val checks: Long = 0
    ) {
        fun apply(value: Long, globalDivisor: Long?): Pair<Int, Long> {
            var newValue = operation.apply(value)
            globalDivisor?.let { newValue %= it }
            if (lowerWorry) {
                newValue /= 3
            }
            return if (newValue % testDivisor == 0L) {
                onTrue to newValue
            } else {
                onFalse to newValue
            }
        }

        companion object {
            fun parse(input: List<String>, lowerWorry: Boolean): Pair<Monkey, List<Long>> {
                val id = input[0].split(" ").filter { it.endsWith(":") }.first().dropLast(1).toInt()
                val values =
                    input[1].split(Regex("[, ]+")).filter { Regex("\\d+").matches(it) }.map { it.toLong() }
                val operation = Regex(" new = .*").find(input[2])!!.value.drop(7).let { Calculation.parse(it) }
                val testDivisor = parseTest(input[3])
                val onTrue = input[4].split(" ").last().toInt()
                val onFalse = input[5].split(" ").last().toInt()
                return Monkey(id, operation, testDivisor, onTrue, onFalse, lowerWorry) to values
            }

            private fun parseTest(description: String): Int {
                val desc = description.trimIndent().drop(6)
                return when {
                    desc.startsWith("divisible by") -> {
                        desc.split(" ").last().toInt()
                    }

                    else -> throw IllegalArgumentException("Unknown test $desc")
                }
            }
        }
    }
}

fun main() {


    val inputClassifier = "Day11"

    fun execute(input: List<String>, lowerWorry: Boolean) =
        sequence {
            var state = input.splitAt { it.isBlank() }.map { Day11.Monkey.parse(it, lowerWorry) }
            val globalDivisor =
                state.map { it.first.testDivisor * (if (lowerWorry) 3 else 1) }.fold(1L) { acc, i -> acc * i }
            while (true) {
                val newInventories = state.map { it.second.toMutableList() }
                state = state.indices.map { i ->
                    val monkey = state[i].first
                    val inventory = newInventories[i]
                    val checks = inventory.size
                    inventory.forEach { value ->
                        val (next, newValue) = monkey.apply(value, globalDivisor)
                        newInventories[next].add(newValue)
                    }
                    inventory.clear()
                    monkey to checks
                }.zip(newInventories).map { (monkey, inventory) ->
                    monkey.first.copy(checks = monkey.first.checks + monkey.second) to inventory.toList()
                }
                yield(state)
            }
        }

    fun part1(input: List<String>): Long {
        return execute(input, true).take(20)
            .last().map { it.first.checks }.sortedDescending().take(2).reduce(Long::times)
    }


    fun part2(input: List<String>): Long {
        return execute(input, false).take(10000)
            .last().map { it.first.checks }.sortedDescending().take(2).reduce(Long::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 10605L)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 2713310158L)
    println(part2(input))

}
