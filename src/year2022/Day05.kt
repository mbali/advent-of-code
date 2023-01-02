package year2022

import readInput
import splitAt


fun main() {

    val inputClassifier = "Day05"

    data class Step(val count: Int, val from: Int, val to: Int) {
        fun updateState(state: List<ArrayDeque<Char>>, singleStep: Boolean = false) {
            val (from, to) = from to to
            val (fromDeque, toDeque) = state[from - 1] to state[to - 1]
            if (singleStep) {
                //hacky way to do it, maybe I should not have used ArrayDeque as the "stack" representation...
                val shunt = ArrayDeque<Char>()
                repeat(count) {
                    shunt.addLast(fromDeque.removeLast())
                }
                repeat(count) {
                    toDeque.addLast(shunt.removeLast())
                }
            } else {
                repeat(count) {
                    fromDeque.removeLast().let { toDeque.addLast(it) }
                }
            }
        }
    }

    fun parseInitialState(input: List<String>): List<ArrayDeque<Char>> {
        val cnt = """\d+""".toRegex().findAll(input.last()).count()
        val result = List(cnt) { ArrayDeque<Char>() }
        val stacks = input.reversed().drop(1)
        stacks.forEach { row ->
            repeat(cnt) { s ->
                row.toCharArray().getOrNull(s * 4 + 1)?.let {
                    if (it != ' ') {
                        result[s].addLast(it)
                    }
                }
            }
        }
        return result
    }

    fun process(input: List<String>, singleStep: Boolean): String {
        val (initialStateInput, stepsInput) = input.splitAt { it.isEmpty() }
        val state = parseInitialState(initialStateInput)
        val steps = stepsInput.map { line ->
            val (count, from, to) = line.split(" ").slice(listOf(1, 3, 5)).map { it.toInt() }
            Step(count, from, to)
        }
        steps.forEach { step ->
            step.updateState(state, singleStep)
        }
        return state.joinToString(separator = "") { it.last().toString() }
    }

    fun part1(input: List<String>): String {
        return process(input, false)
    }


    fun part2(input: List<String>): String {
        return process(input, true)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == "CMZ")

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == "MCD")
    println(part2(input))

}
