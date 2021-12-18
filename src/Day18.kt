sealed class SnailFishNumber(var parent: NumberPair? = null) {
    abstract fun magnitude(): Long
    class RegularNumber(val value: Int, parent: NumberPair? = null) : SnailFishNumber(parent) {
        override fun magnitude() = value.toLong()
        override fun toString() = value.toString()
    }

    class NumberPair(val left: SnailFishNumber, val right: SnailFishNumber, parent: NumberPair? = null) :
        SnailFishNumber(parent) {
        init {
            left.parent = this
            right.parent = this
        }

        override fun magnitude() = 3 * left.magnitude() + 2 * right.magnitude()
        override fun toString() = "[${left},${right}]"
    }

    private fun explodeAt(left: RegularNumber?, exploded: NumberPair, right: RegularNumber?): SnailFishNumber {
        if (exploded.left !is RegularNumber || exploded.right !is RegularNumber) throw IllegalArgumentException("Exploded pair must contain regular numbers")
        return when (this) {
            is RegularNumber -> RegularNumber(
                if (this === left) value + exploded.left.value
                else if (this === right) value + exploded.right.value
                else value
            )
            is NumberPair -> if (this === exploded) RegularNumber(0) else {
                NumberPair(this.left.explodeAt(left, exploded, right), this.right.explodeAt(left, exploded, right))
            }
        }
    }

    private fun splitAt(number: RegularNumber): SnailFishNumber {
        return when (this) {
            is NumberPair -> NumberPair(left.splitAt(number), right.splitAt(number))
            is RegularNumber ->
                if (this === number) NumberPair(RegularNumber(this.value / 2), RegularNumber((this.value + 1) / 2))
                else RegularNumber(this.value)
        }
    }

    private fun copy(): SnailFishNumber =
        when (this) {
            is RegularNumber -> RegularNumber(value)
            is NumberPair -> NumberPair(left.copy(), right.copy())
        }


    private fun nodesAndDepthsInOrder(depth: Int = 0): List<Pair<SnailFishNumber, Int>> {
        return when (this) {
            is RegularNumber -> listOf(this to depth)
            is NumberPair -> buildList {
                addAll(left.nodesAndDepthsInOrder(depth + 1))
                add(this@SnailFishNumber to depth)
                addAll(right.nodesAndDepthsInOrder(depth + 1))
            }
        }
    }

    protected fun reduce(): SnailFishNumber {
        var reduced = this
        do {
            val nodesAndDepths = reduced.nodesAndDepthsInOrder()
            val explodingPair = nodesAndDepths.filter {
                val node = it.first
                it.second >= 4 && node is NumberPair && node.left is RegularNumber && node.right is RegularNumber
            }.map { it.first }.filterIsInstance<NumberPair>().firstOrNull()
            if (explodingPair != null) {
                var pivotSeen = false
                var left: RegularNumber? = null
                var right: RegularNumber? = null
                nodesAndDepths.map { it.first }.forEach { number ->
                    if (number === explodingPair) pivotSeen = true
                    else if (number is RegularNumber && number.parent !== explodingPair) {
                        if (!pivotSeen) left = number
                        else if (right == null) right = number
                    }
                }
                reduced = reduced.explodeAt(left, explodingPair, right)
            }
            val splittingNode: RegularNumber? =
                if (explodingPair != null) null
                else nodesAndDepths
                    .map { it.first }
                    .filterIsInstance<RegularNumber>()
                    .filter { it.value >= 10 }
                    .firstOrNull()
            if (splittingNode != null) {
                reduced = reduced.splitAt(splittingNode)
            }
        } while (explodingPair != null || splittingNode != null)
        return reduced
    }

    operator fun plus(other: SnailFishNumber): SnailFishNumber {
        return NumberPair(this.copy(), other.copy()).reduce()
    }

    companion object {
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
}
