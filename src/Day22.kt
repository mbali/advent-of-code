import Day22.apply
import Day22.toInstructions
import kotlin.math.max
import kotlin.math.min

object Day22 {
    data class Region(val x: IntRange, val y: IntRange, val z: IntRange)
    data class Instruction(val state: Boolean, val region: Region)

    infix fun IntRange.intersect(other: IntRange): IntRange = max(first, other.first)..min(last, other.last)
    infix fun Region.intersect(other: Region) = Region(x intersect other.x, y intersect other.y, z intersect other.z)
    fun Region.isEmpty(): Boolean = x.isEmpty() || y.isEmpty() || z.isEmpty()
    fun Region.locations(): Set<Vec3> =
        if (isEmpty()) emptySet()
        else x.flatMap { px -> y.flatMap { py -> z.map { pz -> Vec3(px, py, pz) } } }.toSet()

    val INSTRUCTION_REGEX = Regex("""^(?<state>off|on)(?:[ ,][xyz]=(-?\d+)\.\.(-?\d+)){3}$""")
    fun String.toInstruction(): Instruction {
        val matches = INSTRUCTION_REGEX.matchEntire(this)
            ?: throw IllegalArgumentException("String \"$this\" does not match instruction format")

        val state = "on" == matches.groups["state"]!!.value
        val (x, y, z) = this.ints().chunked(2).map { (min, max) -> min..max }
        return Instruction(state, Region(x, y, z))
    }

    fun List<String>.toInstructions(): List<Instruction> = map { it.toInstruction() }

    data class ReactorState(
        val reactorRegion: Region = Region(
            IntRange(Int.MIN_VALUE, Int.MAX_VALUE),
            IntRange(Int.MIN_VALUE, Int.MAX_VALUE),
            IntRange(Int.MIN_VALUE, Int.MAX_VALUE)
        ),
        val onCubes: Set<Vec3> = emptySet()
    )

    infix fun ReactorState.apply(instruction: Instruction): ReactorState {
        val affectedRegion = reactorRegion intersect instruction.region
        return if (affectedRegion.isEmpty())
            this
        else {
            val changedCubes = affectedRegion.locations()
            copy(
                onCubes = if (instruction.state) {
                    changedCubes union onCubes
                } else {
                    onCubes - changedCubes
                }
            )
        }
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        val reactorRegion = Day22.Region(
            -50..50,
            -50..50,
            -50..50
        )
        return input.toInstructions()
            .fold(Day22.ReactorState(reactorRegion)) { state, instruction -> state.apply(instruction) }.onCubes.size
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day22_test")
    check(part1(testInput) == 590784)

    val input = readInput("Day22")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
