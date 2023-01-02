package year2022

import readInput

private sealed class MonkeyValue {
    data class Number(val value: Long) : MonkeyValue()
    object Unknown : MonkeyValue()
}

private sealed class MonkeyJob {
    data class Simple(val value: MonkeyValue) : MonkeyJob()
    data class Operation(val left: String, val right: String, val op: String) : MonkeyJob()
}

private data class Monkey(val id: String, val job: MonkeyJob)

fun main() {

    val linePattern = Regex("""(?<id>[a-z]+): (?:(?<left>[a-z]+) (?<op>[-+/*]) (?<right>[a-z]+)|(?<num>-?\d+))""")

    fun String.operate(v1: MonkeyValue, v2: MonkeyValue): MonkeyValue {
        if (v1 is MonkeyValue.Unknown || v2 is MonkeyValue.Unknown) return MonkeyValue.Unknown
        val left = (v1 as MonkeyValue.Number).value
        val right = (v2 as MonkeyValue.Number).value
        return when (this) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> left / right
            else -> throw IllegalArgumentException("Unknown op $this")
        }.let { MonkeyValue.Number(it) }
    }

    fun String.calculateRight(left: Long, expected: Long): Long {
        return when (this) {
            "+" -> expected - left
            "-" -> left - expected
            "*" -> expected / left
            "/" -> left / expected
            else -> throw IllegalArgumentException("Unknown op $this")
        }
    }

    fun String.calculateLeft(right: Long, expected: Long): Long {
        return when (this) {
            "+" -> expected - right
            "-" -> expected + right
            "*" -> expected / right
            "/" -> expected * right
            else -> throw IllegalArgumentException("Unknown op $this")
        }
    }

    fun String.monkey(): Monkey {
        val match = linePattern.matchEntire(this) ?: throw IllegalStateException("Invalid line: $this")
        val id = match.groups["id"]!!.value
        return when (val num = match.groups["num"]?.value) {
            null -> {
                val left = match.groups["left"]!!.value
                val right = match.groups["right"]!!.value
                val op = match.groups["op"]!!.value
                Monkey(id, MonkeyJob.Operation(left, right, op))
            }

            else -> Monkey(id, MonkeyJob.Simple(MonkeyValue.Number(num.toLong())))
        }
    }

    fun List<Monkey>.calculate(): Map<String, MonkeyValue> {
        val monkeyMap = associateBy { it.id }
        val results = mutableMapOf<String, MonkeyValue>()

        fun Monkey.evaluate(): MonkeyValue {
            if (id in results) return results.getValue(id)
            val result = when (val job = job) {
                is MonkeyJob.Simple -> job.value
                is MonkeyJob.Operation -> job.op.operate(
                    monkeyMap.getValue(job.left).evaluate(),
                    monkeyMap.getValue(job.right).evaluate()
                )
            }
            results[id] = result
            return result
        }

        forEach { it.evaluate() }
        return results.toMap()
    }


    val inputClassifier = "Day21"

    fun part1(input: List<String>): Long {
        return (input.map { it.monkey() }.calculate().getValue("root") as MonkeyValue.Number).value
    }


    fun part2(input: List<String>): Long {
        val monkeys = input.map { it.monkey() }.map {
            when (it.id) {
                "root" -> it.copy(job = (it.job as MonkeyJob.Operation).copy(op = "-"))// a == b -> a - b == 0
                "humn" -> it.copy(job = MonkeyJob.Simple(MonkeyValue.Unknown))
                else -> it
            }
        }
        val known = monkeys.calculate().toMutableMap()
        val monkeysById = monkeys.associateBy { it.id }
        var current = "root"
        var expected = 0L
        while (known.getValue(current) == MonkeyValue.Unknown) {
            known[current] = MonkeyValue.Number(expected)
            val job = monkeysById.getValue(current).job
            if (job !is MonkeyJob.Operation) break // we are done!
            val left = known.getValue(job.left)
            val right = known.getValue(job.right)
            val op = job.op
            if (left is MonkeyValue.Unknown) {
                expected = op.calculateLeft((right as MonkeyValue.Number).value, expected)
                current = job.left
            } else {
                expected = op.calculateRight((left as MonkeyValue.Number).value, expected)
                current = job.right
            }
        }
        return (known.getValue("humn") as MonkeyValue.Number).value
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 152L)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 301L)
    println(part2(input))

}
