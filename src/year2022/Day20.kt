package year2022

import readInput

fun main() {
    val inputClassifier = "Day20"


    fun MutableList<Int?>.insertAt(index: Int, value: Int) {
        val originalSize = size
        val wrappedIndex = index.mod(size)
        if (this[wrappedIndex] == null) {
            this[wrappedIndex] = value
        } else {
            this.add(wrappedIndex, value)
            for (i in wrappedIndex + 1 until size + wrappedIndex) {
                if (this[i % size] == null) {
                    removeAt(i % size)
                    break
                }
            }
        }
        if (size != originalSize) {
            throw IllegalStateException("Size changed")
        }
    }

    data class ValueWithRelocation(val value: Long, val originalIndex: Int, var newIndex: Int = originalIndex)

    fun List<Long>.toRelocationList() = mapIndexed { index, value -> ValueWithRelocation(value, index) }
    fun List<ValueWithRelocation>.shuffle(): List<ValueWithRelocation> {
        val newRelocationList = map { v -> v.copy() }
        for (i in indices) {
            val currentRelocation = newRelocationList.first { it.originalIndex == i }
            if (currentRelocation.value % size == 0L) continue
            val oldIndex = currentRelocation.newIndex
            val newIndex = (oldIndex + currentRelocation.value).mod(size - 1)
            if (currentRelocation.value > 0) {
                if (newIndex < oldIndex) { //we moved right, but wrapped around
                    newRelocationList.filter { it.newIndex in newIndex until oldIndex }.forEach { it.newIndex++ }
                } else if (newIndex > oldIndex) {
                    newRelocationList.filter { it.newIndex in oldIndex + 1..newIndex }.forEach { it.newIndex-- }
                }
            } else if (currentRelocation.value < 0) {
                if (newIndex > oldIndex) { //we moved left, but wrapped around
                    newRelocationList.filter { it.newIndex in oldIndex + 1..newIndex }.forEach { it.newIndex-- }
                } else if (newIndex < oldIndex) {
                    newRelocationList.filter { it.newIndex in newIndex until oldIndex }.forEach { it.newIndex++ }
                }
            }
            currentRelocation.newIndex = newIndex
        }
        if (newRelocationList.map { it.newIndex }.toSet().size != newRelocationList.size) {
            throw IllegalStateException("Duplicate index")
        }
        return newRelocationList
    }

    fun List<ValueWithRelocation>.grooveCoordinates(): List<Long> {
        val i0 = first { it.value == 0L }.newIndex
        return listOf(1000, 2000, 3000).map { (i0 + it).mod(size) }.map { idx -> first { it.newIndex == idx }.value }
    }

    fun List<String>.parseInput(): List<Long> {
        return this.map { it.toLong() }
    }


    fun part1(input: List<String>): Long {
        return input.parseInput().toRelocationList().shuffle().grooveCoordinates().sum()
    }


    fun part2(input: List<String>): Long {
        var list = input.parseInput().map { it * 811589153 }.toRelocationList()
        repeat(10) {
            list = list.shuffle()
        }
        return list.grooveCoordinates().sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 3L)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 1_623_178_306L)
    println(part2(input))

}
