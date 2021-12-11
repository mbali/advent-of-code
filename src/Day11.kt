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
        val result = (1..100).scan(readInput(input)) { acc, _ ->
            step(acc)
        }.sumOf { it.count { level -> level == 0 } }
        return result

    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)

    val input = readInput("Day11")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
