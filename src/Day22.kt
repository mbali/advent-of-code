import Day22.Region
import Day22.solution
import kotlin.math.max
import kotlin.math.min

private object Day22 {
    data class Region(val x: IntRange, val y: IntRange, val z: IntRange)

    val MAX_REGION = Region(
        IntRange(Int.MIN_VALUE, Int.MAX_VALUE),
        IntRange(Int.MIN_VALUE, Int.MAX_VALUE),
        IntRange(Int.MIN_VALUE, Int.MAX_VALUE)
    )

    private data class Instruction(val state: Boolean, val region: Region)

    private infix fun IntRange.intersect(other: IntRange): IntRange = max(first, other.first)..min(last, other.last)
    private infix fun Region.intersect(other: Region) =
        Region(x intersect other.x, y intersect other.y, z intersect other.z)

    private infix fun IntRange.splitWith(r: IntRange): List<IntRange> =
        listOf(first until r.first, r, r.last + 1..last).map { it intersect this }.filterNot { it.isEmpty() }

    private operator fun Region.contains(v: Vec3) = v.x in x && v.y in y && v.z in z

    private infix fun IntRange.mergeableWith(other: IntRange): Boolean =
        !intersect(other).isEmpty() ||
                (last != Int.MAX_VALUE && other.last != Int.MAX_VALUE && (first == other.last + 1 || other.first == last + 1))

    private infix fun IntRange.merge(other: IntRange): IntRange =
        if (!this.mergeableWith(other)) {
            throw IllegalArgumentException("Ranges are not mergeable")
        } else {
            val merged = min(first, other.first)..max(last, other.last)
            assert(merged.size() == this.size() + other.size() - this.intersect(other).size()) {
                "Unexpected merged int range size!"
            }
            merged
        }

    private infix fun Region.mergeableWith(other: Region): Boolean =
        x.mergeableWith(other.x) &&
                y.mergeableWith(other.y) &&
                z.mergeableWith(other.z) &&
                listOf(x, y, z).zip(listOf(other.x, other.y, other.z)).count { (a, b) -> a == b } >= 2

    private infix fun Region.merge(other: Region): Region =
        if (!this.mergeableWith(other)) {
            throw IllegalArgumentException("Ranges are not mergeable")
        } else {
            val merged = Region(x merge other.x, y merge other.y, z merge other.z)
            assert(
                merged.size() == this.size() + other.size() - this.intersect(other).size()
            ) { "Unexpected merged region size for $this and $other -> $merged" }
            merged
        }


    private fun MutableCollection<Region>.addRegion(region: Region, mergeWithExistingRegions: Boolean = true) {
        if (!mergeWithExistingRegions)
            add(region)
        else {
            var newRegion = region
            do {
                val mergeable = this.firstOrNull { it mergeableWith newRegion }?.also { other ->
                    remove(other)
                    newRegion = newRegion.merge(other)
                }
            } while (mergeable != null)
            add(newRegion)
        }
    }

    private operator fun Region.minus(other: Region): Set<Region> {
        val intersection = this intersect other
        return if (intersection.isEmpty())
            setOf(this)
        else if (intersection == this)
            emptySet()
        else
            buildSet {
                for ((xRange, yRange, zRange) in crossJoin(
                    this@minus.x splitWith intersection.x,
                    this@minus.y splitWith intersection.y,
                    this@minus.z splitWith intersection.z
                ))
                    if (xRange.first !in intersection.x ||
                        yRange.first !in intersection.y ||
                        zRange.first !in intersection.z
                    ) {
                        addRegion(Region(xRange, yRange, zRange))
                    }
            }
    }
    private fun Region.isEmpty(): Boolean = x.isEmpty() || y.isEmpty() || z.isEmpty()
    private fun IntRange.size(): Long = if (isEmpty()) 0L else 1L + last - first //last-first can be over Int.MAX_VALUE

    private fun Region.size(): Long = x.size() * y.size() * z.size()

    private val INSTRUCTION_REGEX = Regex("""^(?<state>off|on)(?:[ ,][xyz]=(-?\d+)\.\.(-?\d+)){3}$""")
    private fun String.toInstruction(): Instruction {
        val matches = INSTRUCTION_REGEX.matchEntire(this)
            ?: throw IllegalArgumentException("String \"$this\" does not match instruction format")

        val state = "on" == matches.groups["state"]!!.value
        val (x, y, z) = this.ints().chunked(2).map { (min, max) -> min..max }
        return Instruction(state, Region(x, y, z))
    }

    private fun List<String>.toInstructions(): List<Instruction> = map { it.toInstruction() }

    private data class ReactorState(
        val reactorRegion: Region = MAX_REGION,
        val onRegions: List<Region> = emptyList()
    )

    private infix fun ReactorState.apply(instruction: Instruction): ReactorState {
        val affectedRegion = reactorRegion intersect instruction.region
        return if (affectedRegion.isEmpty())
            this
        else {
            val newRegions =
                onRegions.flatMapTo(mutableListOf()) { it - affectedRegion } //remove on state for the affected region
            if (instruction.state) newRegions.addRegion(affectedRegion, false) //add it if needed
            copy(
                onRegions = newRegions
            )
        }
    }

    fun solution(input: List<String>, reactorRegion: Region = MAX_REGION, quiet: Boolean = true): Long {
        return input
            .toInstructions()
            .fold(ReactorState(reactorRegion)) { state, instruction -> state.apply(instruction) }
            .onRegions
            .apply { if (!quiet) println("Region count: ${this.count()}") }
            .sumOf { it.size() }
    }
}

fun main() {
    val quiet = true //for some debugging

    fun part1(input: List<String>, quiet: Boolean = true): Long {
        val reactorRegion = Region(
            -50..50,
            -50..50,
            -50..50
        )
        return solution(input, reactorRegion, quiet = quiet)
    }

    fun part2(input: List<String>, quiet: Boolean = true): Long {
        return solution(input, quiet = quiet)
    }

    val testInput = readInput("Day22_test")
    val testInput2 = readInput("Day22_test2")
    check(part1(testInput) == 590784L)
    check(part1(testInput2) == 474140L)

    val input = readInput("Day22")
    println(part1(input, quiet))

    check(part2(testInput2) == 2758514936282235L)
    println(part2(input, quiet))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 100) { part1(input) }
        benchmark("part2", 100) { part2(input) }
    }
}
