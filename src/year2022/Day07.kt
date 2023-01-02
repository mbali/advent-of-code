package year2022

import readInput

sealed class FSEntry(open val name: String, open val parent: Directory?) {
    data class File(override val name: String, val size: Int, override val parent: Directory?) : FSEntry(name, parent)
    data class Directory(
        override val name: String,
        override val parent: Directory?,
        val children: MutableList<FSEntry> = mutableListOf()
    ) : FSEntry(name, parent)

    fun size(): Int {
        return when (this) {
            is File -> size
            is Directory -> children.sumOf { it.size() }
        }
    }

    fun entries(): List<FSEntry> {
        return buildList {
            add(this@FSEntry)
            if (this@FSEntry is Directory) {
                children.forEach { addAll(it.entries()) }
            }
        }
    }

}

fun main() {
    val inputClassifier = "Day07"

    fun processInput(input: List<String>): FSEntry.Directory {
        val root = FSEntry.Directory("", null)
        var currentDir = root
        input.drop(1) //first comman is always cd /
            .forEach { line ->
                when {
                    line.startsWith("$ cd ..") -> currentDir = currentDir.parent!!
                    line.startsWith("$ cd ") -> currentDir =
                        currentDir.children.find { it is FSEntry.Directory && it.name == line.substring(5) } as FSEntry.Directory

                    line.startsWith("$ ls") -> null //NOOP
                    line.startsWith("dir ") -> {
                        val name = line.substring(4)
                        currentDir.children.add(FSEntry.Directory(name, currentDir))
                    }

                    line.matches(Regex("""\d+ .+""")) -> {
                        val (size, name) = line.split(" ")
                        currentDir.children.add(FSEntry.File(name, size.toInt(), currentDir))
                    }

                    else -> throw IllegalArgumentException("Unprocessable line: $line")
                }
            }
        return root
    }


    fun part1(input: List<String>): Int {
        val root = processInput(input)
        return root.entries()
            .filterIsInstance<FSEntry.Directory>()
            .map { it.size() }
            .filter { it < 100000 }
            .sum()
    }


    fun part2(input: List<String>): Int {
        val root = processInput(input)
        val totalSize = root.size()
        val needToDelete = totalSize - (70000000 - 30000000)
        return root.entries()
            .filterIsInstance<FSEntry.Directory>()
            .map { it.size() }
            .sorted()
            .find { it >= needToDelete }
            ?: throw IllegalArgumentException("No directory found with size >= $needToDelete")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 95437)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 24933642)
    println(part2(input))

}
