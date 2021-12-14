fun main() {

    fun step(energyLevels: IntArray): IntArray {
        check(energyLevels.size == 100) { "Unexpected input size" }
        val flashed = BooleanArray(100)
        //increase energy by one
        val result = energyLevels.map { it + 1 }.toIntArray()
        //cascade flashes
        do {
            val flashIndex = result.indices.firstOrNull {
                result[it] > 9 && !flashed[it]
            }
            flashIndex?.let { idx ->
                flashed[idx] = true
                val row = idx / 10
                val col = idx % 10
                for (r in row - 1..row + 1) {
                    for (c in col - 1..col + 1) {
                        if (r in 0..9 && c in 0..9) {
                            result[r * 10 + c]++
                        }
                    }
                }
            }
        } while (flashIndex != null)
        //set flashed energy values to 0
        flashed.forEachIndexed { index, b ->
            if (b) result[index] = 0
        }
        return result
    }


    fun readInput(input: List<String>): IntArray {
        return input.flatMap {
            it.map { char -> char.digitToInt() }
        }.toIntArray()
    }

    fun part1(input: List<String>): Int {
        return generateSequence(readInput(input)) { step(it) }
            .drop(1) //drop the initial state
            .take(100) // take 100 steps
            .map { it.count { level -> level == 0 } } //the zeroes are the ones that flashed
            .sum()
    }

    fun part2(input: List<String>): Int {
        return generateSequence(readInput(input)) { step(it) }
            .takeWhile { it.any { level -> level > 0 } } //the last value is the first state before the synchronized flash
            .count() //with the initial state, the count is the expected value
    }

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)

    val input = readInput("Day11")
    println(part1(input))

    check(part2(testInput) == 195)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
