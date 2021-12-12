fun main() {

    fun isSmall(id: String) = id.all { it.isLowerCase() }

    fun toNeigbourhoods(input: List<String>): Map<String, List<String>> {
        val vertices =
            input.flatMap { line ->
                val (from, to) = line.split('-')
                listOf(from to to, to to from)
            }.toSet()
        return vertices.groupBy { it.first }.mapValues { v -> v.value.map { it.second }.toList() }
    }

    fun routes(
        neigbourhoods: Map<String, List<String>>,
        prefix: List<String> = emptyList(),
        currentNode: String = "start",
        targetNode: String = "end"
    ): List<List<String>> {
        val route = prefix + currentNode
        if (currentNode == targetNode) {
            return listOf(route)
        }
        return neigbourhoods.getValue(currentNode).flatMap { next ->
            if (isSmall(next) && next in prefix) emptyList()
            else routes(neigbourhoods, route, next, targetNode)
        }
    }


    fun part1(input: List<String>): Int {
        return routes(toNeigbourhoods(input)).size
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput1 = readInput("Day12_test1")
    val testInput2 = readInput("Day12_test2")
    val testInput3 = readInput("Day12_test3")
    check(part1(testInput1) == 10)
    check(part1(testInput2) == 19)
    check(part1(testInput3) == 226)

    val input = readInput("Day12")
    println(part1(input))

    check(part2(testInput1) == TODO())
    println(part2(input))
}
