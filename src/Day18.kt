import java.util.concurrent.atomic.AtomicInteger

sealed class SnailFishNumber() {
    abstract fun magnitude(): Long
    data class RegularNumber(val value: Int, private val id: Int = generateId()) : SnailFishNumber() {
        override fun magnitude() = value.toLong()
        override fun toString() = value.toString()
    }

    data class NumberPair(val left: SnailFishNumber, val right: SnailFishNumber, private val id: Int = generateId()) :
        SnailFishNumber() {
        override fun magnitude() = 3 * left.magnitude() + 2 * right.magnitude()
        override fun toString() = "[${left},${right}]"
    }

    /**
     * Detect route to the first node, which satisfies the predicate
     * The route is returned in reverse order (eg.: its first element is the node that satisfies the predicate)
     * If no node found satisfying the predicate then null is returned
     */
    private fun detectRouteToFirst(
        currentDepth: Int = 0, predicate: (node: SnailFishNumber, depth: Int) -> Boolean
    ): List<SnailFishNumber>? {
        return when {
            predicate(this, currentDepth) -> listOf(this)
            this is NumberPair -> {
                (left.detectRouteToFirst(currentDepth + 1, predicate) ?: right.detectRouteToFirst(
                    currentDepth + 1, predicate
                ))?.let {
                    it + this
                }
            }
            else -> null
        }
    }

    /**
     * Explode a given NumberPair node, and return the new SnailFishNumber after the explosion
     *
     * @param left The regular number node to the left (if any)
     * @param toExplode The pair node to explode
     * @param right The regular number node to the right (if any)
     * @param affectedNodes The pair nodes that are affected by the explosion (ancestors of left, right, and toExplode)
     *
     * @return the number value after the explosion
     */
    private fun explodeAt(
        left: RegularNumber?,
        toExplode: NumberPair,
        right: RegularNumber?,
        affectedNodes: Set<SnailFishNumber>
    ): SnailFishNumber {
        if (!affectedNodes.contains(this)) {
            return this
        }
        if (toExplode.left !is RegularNumber || toExplode.right !is RegularNumber) throw IllegalArgumentException("Exploded pair must contain regular numbers")
        return when (this) {
            is RegularNumber ->
                if (this === left) RegularNumber(value + toExplode.left.value)
                else if (this === right) RegularNumber(value + toExplode.right.value)
                else this  //should not happen
            is NumberPair ->
                if (this === toExplode) RegularNumber(0)
                else NumberPair(
                    this.left.explodeAt(left, toExplode, right, affectedNodes),
                    this.right.explodeAt(left, toExplode, right, affectedNodes)
                )
        }
    }

    private fun splitAt(number: RegularNumber, affectedNodes: Set<SnailFishNumber>): SnailFishNumber {
        if (!affectedNodes.contains(this)) {
            return this
        }
        return when (this) {
            is NumberPair -> NumberPair(
                left.splitAt(number, affectedNodes),
                right.splitAt(number, affectedNodes)
            )
            is RegularNumber -> NumberPair(
                RegularNumber(this.value / 2),
                RegularNumber((this.value + 1) / 2)
            )
        }
    }

    /**
     * Explode the number if needed
     *
     * If any pair is nested inside four pairs, the leftmost such pair explodes.
     *
     * To explode a pair, the pair's left value is added to the first regular number to the left of the exploding pair (if any),
     * and the pair's right value is added to the first regular number to the right of the exploding pair (if any).
     * Exploding pairs will always consist of two regular numbers. Then, the entire exploding pair is replaced with the regular number 0.
     *
     * @return The new number after the explosion or null if no explosion took place
     */
    private fun maybeExplode(): SnailFishNumber? {
        return detectRouteToFirst { node, depth -> depth >= 4 && node is NumberPair && node.left is RegularNumber && node.right is RegularNumber }
            ?.let { routeToExplosion ->
                var regularNumberToTheLeft: RegularNumber? = null
                var regularNumberToTheRight: RegularNumber? = null
                val affectedNodes = routeToExplosion.toMutableSet()
                routeToExplosion.windowed(2).forEach { (node, parent) ->
                    val parentPair = parent as NumberPair
                    if (regularNumberToTheRight == null && parentPair.left === node) {
                        generateSequence(parentPair.right) {
                            when (it) {
                                is RegularNumber -> null
                                is NumberPair -> it.left
                            }
                        }.forEach {
                            affectedNodes += it
                            if (it is RegularNumber) regularNumberToTheRight = it
                        }
                    }
                    if (regularNumberToTheLeft == null && parentPair.right === node) {
                        generateSequence(parentPair.left) {
                            when (it) {
                                is RegularNumber -> null
                                is NumberPair -> it.right
                            }
                        }.forEach {
                            affectedNodes += it
                            if (it is RegularNumber) regularNumberToTheLeft = it
                        }
                    }
                }
                explodeAt(
                    regularNumberToTheLeft,
                    routeToExplosion.first() as NumberPair,
                    regularNumberToTheRight,
                    affectedNodes
                )
            }
    }

    /**
     * Split the number if needed
     *
     * If any regular number is 10 or greater, the leftmost such regular number splits.
     * To split a regular number, replace it with a pair; the left element of the pair should be the regular number
     * divided by two and rounded down, while the right element of the pair should be the regular number divided by two and rounded up.
     *
     * @return the new number after splitting, or null if no splitting took place
     */
    private fun maybeSplit(): SnailFishNumber? {
        return detectRouteToFirst { node, _ -> node is RegularNumber && node.value >= 10 }?.let {
            splitAt(it.first() as RegularNumber, it.toSet())
        }
    }

    protected fun reduce(): SnailFishNumber {
        var reduced = this
        do {
            val nextStep = reduced.run { maybeExplode() ?: maybeSplit() }?.also { reduced = it }
        } while (nextStep != null)
        return reduced
    }

    operator fun plus(other: SnailFishNumber): SnailFishNumber {
        return NumberPair(this, other).reduce()
    }

    companion object {
        private val idGenerator: AtomicInteger = AtomicInteger(0)

        protected fun generateId(): Int {
            return idGenerator.getAndIncrement()
        }

        private fun parse(input: String, position: Int = 0): Pair<SnailFishNumber, Int> {
            val firstChar = input[position]
            if (firstChar.isDigit()) {
                //regular case
                val digits = input.substring(position).takeWhile { it.isDigit() }
                return RegularNumber(digits.toInt()) to digits.length
            } else if (firstChar == '[') {
                val (left, leftLength) = parse(input, position + 1)
                val commaPosition = position + 1 + leftLength
                if (input[commaPosition] != ',')
                    throw IllegalArgumentException("Expected ',' at position ${commaPosition}, but got '${input[commaPosition]}'")
                val (right, rightLength) = parse(input, commaPosition + 1)
                val closingPosition = commaPosition + 1 + rightLength
                if (input[closingPosition] != ']')
                    throw IllegalArgumentException("Expected ']' at position ${closingPosition}, but got '${input[closingPosition]}'")
                return NumberPair(left, right) to leftLength + rightLength + 3
            } else {
                throw IllegalArgumentException("Expected digit or '[' at position $position, but got ${input[position]}")
            }
        }

        fun parse(input: String): SnailFishNumber {
            return parse(input, 0).first
        }
    }
}

fun main() {

    fun part1(input: List<String>): Long {
        return input.map { SnailFishNumber.parse(it) }.reduce { acc, number -> acc + number }.magnitude()
    }

    fun part2(input: List<String>): Long {
        val numbers = input.map { SnailFishNumber.parse(it) }
        return numbers.mapIndexed { i, n ->
            numbers.filterIndexed { index, _ -> index != i }.maxOf { (n + it).magnitude() }
        }.maxOf { it }
    }

    listOf(
        "[[1,2],[[3,4],5]]" to 143,
        "[[[[0,7],4],[[7,8],[6,0]]],[8,1]]" to 1384,
        "[[[[1,1],[2,2]],[3,3]],[4,4]]" to 445,
        "[[[[3,0],[5,3]],[4,4]],[5,5]]" to 791,
        "[[[[5,0],[7,4]],[5,5]],[6,6]]" to 1137,
        "[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]" to 3488
    ).forEach { (num, magnitude) ->
        check(
            SnailFishNumber.parse(num).magnitude() == magnitude.toLong()
        ) { "The magnitude of \"$num\" should be $magnitude" }
    }

    val testInput = readInput("Day18_test")
    check(part1(testInput) == 4140L)

    val input = readInput("Day18")
    println(part1(input))

    check(part2(testInput) == 3993L)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 100) { part1(input) }
        benchmark("part2", 100) { part2(input) }
    }

}
