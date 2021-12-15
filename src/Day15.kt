import java.util.*

fun main() {
    data class Node(val x: Int, val y: Int, val cost: Int) {
        val neighbours: MutableSet<Node> = mutableSetOf()
    }

    fun calculateDistancesFrom(start: Node): Map<Node, Int> {
        return buildMap {
            val queue = PriorityQueue<Pair<Node, Int>>(compareBy { it.second })
            //seed queue with neighbours
            start.neighbours.forEach { queue.offer(it to it.cost) }
            do {
                val maybeNext = queue.poll()
                maybeNext?.let { next ->
                    val nextNode = next.first
                    val distance = next.second
                    put(nextNode, distance)
                    nextNode.neighbours.filter { !containsKey(it) }.forEach { candidate ->
                        val candidateDistance = distance + candidate.cost
                        val storedElement = queue.firstOrNull { it.first == candidate }
                        if (storedElement != null && storedElement.second > candidateDistance) {
                            queue.remove(storedElement)
                        }
                        if (storedElement == null || storedElement.second > candidateDistance) {
                            queue.offer(candidate to candidateDistance)
                        }
                    }
                }
            } while (maybeNext != null)
        }
    }

    fun List<String>.parseInput(multiplier: Int = 1): List<Node> {
        val costs = this.map { line -> line.map { it.digitToInt() }.toTypedArray() }.toTypedArray()
        val scannedX = costs.first().size
        val scannedY = costs.size
        val maxX = scannedX * multiplier - 1
        val maxY = scannedY * multiplier - 1
        fun costAt(x: Int, y: Int): Int =
            ((costs[y % scannedY][x % scannedX] - 1) + x / scannedX + y / scannedY) % 9 + 1

        return buildMap<Pair<Int, Int>, Node> {
            for (x in 0..maxX) {
                for (y in 0..maxY) {
                    put(x to y, Node(x, y, costAt(x, y)))
                }
            }
            for (x in 0..maxX) {
                for (y in 0..maxY) {
                    listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                        .map { x + it.first to y + it.second }
                        .filter { it.first in 0..maxX && it.second in 0..maxY }
                        .forEach { getValue(x to y).neighbours.add(getValue(it)) }
                }
            }
        }.values.toList()
    }

    fun solution(input: List<String>, repetitions: Int = 1): Int {
        val parsed = input.parseInput(repetitions)
        val start = parsed.first { it.x == 0 && it.y == 0 }
        val distances = calculateDistancesFrom(start)
        return distances
            .entries
            .sortedWith(compareBy<Map.Entry<Node, Int>> { it.key.x }
                .thenComparingInt { it.key.y })
            .last()
            .value
    }

    fun part1(input: List<String>): Int {
        return solution(input)
    }

    fun part2(input: List<String>): Int {
        return solution(input, 5)
    }

    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)

    val input = readInput("Day15")
    println(part1(input))

    check(part2(testInput) == 315)
    println(part2(input))
}
