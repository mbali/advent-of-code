import Day24.solution
import Day24.toInstructions

object Day24 {
    data class Instruction(val name: String, val target: Char, val opRegister: Char?, val opValue: Long?)

    fun String.toInstruction(): Instruction {
        val parts = this.split(' ')
        val inst = parts[0]
        val target = parts[1].first()
        val opValue = parts.getOrNull(2)?.toLongOrNull()
        val opRegister = if (opValue != null) null else parts.getOrNull(2)?.first()
        return Instruction(inst, target, opRegister, opValue)
    }

    fun List<String>.toInstructions() = map { it.toInstruction() }

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
        if (inp.name != "inp" || inp.target != 'w') throw IllegalArgumentException("inp does not read into w")
        val addX = this[5]
        if (addX.name != "add" || addX.target != 'x' || addX.opValue == null) throw IllegalArgumentException("add x, :value not found")
        val addY = this[15]
        if (addY.name != "add" || addY.target != 'y' || addY.opValue == null) throw IllegalArgumentException("add y, :value not found")
        val divZ = this[4]
        if (divZ.name != "div" || divZ.target != 'z' || divZ.opValue == null) throw IllegalArgumentException("div z, :value not found")
        return CycleParam(addX.opValue, addY.opValue, divZ.opValue)
    }

    fun findModelNumber(program: List<Instruction>, inputProgression: IntProgression): List<Int>? {
        val cycleParams = buildList<CycleParam> {
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


    fun solution(program: List<Instruction>, inputProgression: IntProgression): String? {
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
}
