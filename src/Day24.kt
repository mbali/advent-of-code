import Day24.countModelNumbers
import Day24.countValidModelNumbers
import Day24.findModelNumber
import Day24.solution
import Day24.toInstructions
import Day24.toProgram

object Day24 {
    sealed class Instruction(val target: String) {
        init {
            target in setOf("x", "y", "z", "w") || throw IllegalArgumentException("Unknown register $target")
        }

        class Inp(target: String) : Instruction(target)
        sealed class Op(target: String, val operand: String, val evaluator: (Long, Long) -> Long) :
            Instruction(target) {
            val immediate: Long? = operand.toLongOrNull()

            init {
                operand in setOf("x", "y", "z", "w") ||
                        operand.toIntOrNull() != null ||
                        throw IllegalArgumentException(
                            "Invalid operand: $operand"
                        )
            }
        }

        class Eql(target: String, operand: String) : Op(target, operand, { a, b -> if (a == b) 1 else 0 })
        class Add(target: String, operand: String) : Op(target, operand, { a, b -> a + b })
        class Mul(target: String, operand: String) : Op(target, operand, { a, b -> a * b })
        class Div(target: String, operand: String) : Op(
            target,
            operand,
            { a, b -> if (b == 0L) throw IllegalArgumentException("Cannot divide by 0") else a / b })

        class Mod(target: String, operand: String) : Op(
            target,
            operand,
            { a, b -> if (b <= 0L || a < 0L) throw IllegalArgumentException("Invalid mod instruction") else a % b })
    }

    fun String.toInstruction(): Instruction {
        val parts = this.split(' ')
        val inst = parts[0]
        val target = parts[1]
        if (inst == "inp") return Instruction.Inp(target)
        val operand = parts[2]
        return when (inst) {
            "eql" -> Instruction.Eql(target, operand)
            "mul" -> Instruction.Mul(target, operand)
            "add" -> Instruction.Add(target, operand)
            "div" -> Instruction.Div(target, operand)
            "mod" -> Instruction.Mod(target, operand)
            else -> throw IllegalArgumentException("Illegal instruction: $this")
        }
    }

    fun List<String>.toInstructions() = map { it.toInstruction() }

    class Subprogram(val instructions: List<Instruction.Op>) {
        init {
            //first operator affecting x is mul x 0 -> x is local
            instructions.firstOrNull { it.target == "x" || it.operand == "x" }?.let {
                if (it.operand == "x" || it !is Instruction.Mul || it.immediate != 0L)
                    throw IllegalArgumentException("Register x is not local")
            }
            instructions.firstOrNull { it.target == "y" || it.operand == "y" }?.let {
                if (it.operand == "y" || it !is Instruction.Mul || it.immediate != 0L)
                    throw IllegalArgumentException("Register y is not local")
            }
        }

        fun execute(input: Long, initialZ: Long): Long {
            val registers = mutableMapOf("z" to initialZ, "w" to input).withDefault { 0L }
            instructions.forEach { op ->
                registers[op.target] = op.evaluator(
                    registers.getValue(op.target),
                    op.immediate ?: registers.getValue(op.operand)
                )
            }
            return registers.getValue("z")
        }
    }

    data class Program(val subprograms: List<Subprogram>)

    fun List<Instruction>.toProgram(): Program {
        none { it is Instruction.Inp && it.target != "w" } || throw IllegalArgumentException("Not all inp instructions read into w register")
        return (this.indices.filter { this[it] is Instruction.Inp } + this.size).windowed(2).map { (start, end) ->
            this.slice(start + 1 until end).map { it as Instruction.Op }
        }.map { Subprogram(it) }.let { Program(it) }
    }

    //implement MONAD cycle - only z value is affected by previous cycle
    fun Long.cycle(input: Long, params: CycleParam): Long {
        /* initial registers: w0, x0, y0, z0
        inp w # w = input
        mul x 0 # x = 0
        add x z # x = z0
        mod x 26 # x = z0 mod 26
        div z :zDiv # z = z0 / dDiv
        add x :xAdd # x = z0 mod 26 + xAdd
        eql x w  # x = input == (z0 mod 26 + xAdd) ? 1 : 0
        eql x 0 # x = input == (z0 mod 26 + xAdd) ? 0 : 1
        mul y 0 # y = 0
        add y 25 # y = 25
        mul y x # y = input == (z0 mod 26 + xAdd) ? 0 : 25
        add y 1 # y = input == (z0 mod 26 + xAdd) ? 0 : 25 + 1
        mul z y # z = z0 / zdiv * (input == z0 mod 26 + xAdd? 0 : 25 + 1)
        mul y 0 # y = 0
        add y w # y = input
        add y :yAdd # y = input + yAdd
        mul y x # y = (input + yAdd) * (input == z0 mod 26 + xAdd? 0 : 1)
        add z y # z = z0 / zdiv * (input == z0 mod 26 + xAdd? 0 : 25 + 1 ) + (input + yAdd) * (input == z0 mod 26 + xAdd ? 0 : 1)
         */
        val z0 = this
        val t = if (input == z0.mod(26) + params.xAdd) 0 else 1
        return z0 / params.zDiv * (t * 25 + 1) + (input + params.yAdd) * t
    }

    data class CycleParam(val xAdd: Long, val yAdd: Long, val zDiv: Long)

    fun List<Instruction>.cycleParam(): CycleParam {
        if (this.size != 18) throw IllegalArgumentException("Invalid cycle size")
        val inp = this[0]
        if (inp !is Instruction.Inp || inp.target != "w") throw IllegalArgumentException("inp does not read into w")
        val addX = this[5]
        if (addX !is Instruction.Add || addX.target != "x" || addX.immediate == null) throw IllegalArgumentException(
            "add x, :value not found"
        )
        val addY = this[15]
        if (addY !is Instruction.Add || addY.target != "y" || addY.immediate == null) throw IllegalArgumentException(
            "add y, :value not found"
        )
        val divZ = this[4]
        if (divZ !is Instruction.Div || divZ.target != "z" || divZ.immediate == null) throw IllegalArgumentException(
            "div z, :value not found"
        )
        return CycleParam(addX.immediate, addY.immediate, divZ.immediate)
    }

    fun findModelNumber(program: List<Instruction>, inputProgression: IntProgression): List<Int>? {
        val cycleParams = buildList {
            program
                .chunked(18)
                .also { it.size == 14 || throw IllegalStateException("Not 14 cycles found") }
                .forEach { cycle ->
                    add(cycle.cycleParam())
                }
        }
        val exchaustedStates = mutableSetOf<Pair<Int, Long>>() //index to z at beginning

        fun helper(depth: Int = 0, z: Long = 0): List<Int>? {
            val key = depth to z
            if (key in exchaustedStates) return null
            for (input in inputProgression) {
                val newZ = z.cycle(input.toLong(), cycleParams[depth])
                if (newZ == 0L && depth == cycleParams.lastIndex)
                    return listOf(input)
                else if (depth < cycleParams.lastIndex) {
                    helper(depth + 1, newZ)?.let { return it + input }
                }
            }
            exchaustedStates.add(key)
            return null
        }
        return helper()?.reversed()
    }

    fun Program.findModelNumber(inputProgression: IntProgression): List<Int>? {
        val exchaustedStates = mutableSetOf<Pair<Int, Long>>() //index to z at beginning
        fun helper(depth: Int = 0, z: Long = 0): List<Int>? {
            val key = depth to z
            if (key in exchaustedStates) return null
            for (input in inputProgression) {
                val newZ = subprograms[depth].execute(input.toLong(), z)
                if (newZ == 0L && depth == subprograms.lastIndex)
                    return listOf(input)
                else if (depth < subprograms.lastIndex) {
                    helper(depth + 1, newZ)?.let { return it + input }
                }
            }
            exchaustedStates.add(key)
            return null
        }
        return helper()?.reversed()
    }

    fun countModelNumbers(program: List<Instruction>): Long {
        val cycleParams = buildList {
            program
                .chunked(18)
                .also { it.size == 14 || throw IllegalStateException("Not 14 cycles found") }
                .forEach { cycle ->
                    add(cycle.cycleParam())
                }
        }
        val postfixCount = mutableMapOf<Pair<Int, Long>, Long>()

        fun helper(depth: Int = 0, z: Long = 0): Long {
            return postfixCount.getOrPut(depth to z) {
                var count = 0L
                for (input in 1..9) {
                    val newZ = z.cycle(input.toLong(), cycleParams[depth])
                    if (newZ == 0L && depth == cycleParams.lastIndex) {
                        count++
                    } else if (depth < cycleParams.lastIndex) {
                        count += helper(depth + 1, newZ)
                    }
                }
                count
            }
        }
        return helper()
    }

    fun Program.countValidModelNumbers(): Long {
        val postfixCount = mutableMapOf<Pair<Int, Long>, Long>()
        fun helper(depth: Int = 0, z: Long = 0): Long {
            return postfixCount.getOrPut(depth to z) {
                var count = 0L
                for (input in 1..9) {
                    val newZ = subprograms[depth].execute(input.toLong(), z)
                    if (newZ == 0L && depth == subprograms.lastIndex) {
                        count++
                    } else if (depth < subprograms.lastIndex) {
                        count += helper(depth + 1, newZ)
                    }
                }
                count
            }
        }
        return helper()
    }

    fun solution(program: List<Instruction>, inputProgression: IntProgression): String? {
//        return program.toProgram().findModelNumber(inputProgression)?.joinToString("")
        return findModelNumber(program, inputProgression)?.joinToString("")
    }
}

fun main() {

    fun part1(input: List<String>): String {
        return solution(input.toInstructions(), 9 downTo 1)!!
    }

    fun part2(input: List<String>): String {
        return solution(input.toInstructions(), 1..9)!!
    }

    val input = readInput("Day24")
    println(part1(input))
    println(part2(input))
    println("Count of valid model numbers: ${countModelNumbers(input.toInstructions())}")
    println(
        "Count of valid model numbers with generic algorithm: ${
            input.toInstructions().toProgram().countValidModelNumbers()
        }"
    )

    val program = input.toInstructions().toProgram()
    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1 reverse engineered", 3) { part1(input) }
        benchmark("part1 'generic'", 3) { program.findModelNumber(9 downTo 1) }
        benchmark("part2", 3) { part2(input) }
        benchmark("part2 'generic'", 3) { program.findModelNumber(1..9) }
        benchmark("count model numbers", 1) { countModelNumbers(input.toInstructions()) }
        benchmark("count model numbers 'generic'", 1) { program.countValidModelNumbers() }
    }

}
