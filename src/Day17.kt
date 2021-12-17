import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun main() {

    data class Position(val x: Int, val y: Int)
    data class Speed(val vx: Int, val vy: Int)
    data class Area(val xRange: IntRange, val yRange: IntRange)
    data class State(val speed: Speed, val position: Position = Position(0, 0))

    fun State.trajectory(): Sequence<State> = generateSequence(this) {
        State(
            Speed(it.speed.vx - it.speed.vx.sign, it.speed.vy - 1),
            Position(it.position.x + it.speed.vx, it.position.y + it.speed.vy)
        )
    }

    fun String.toArea(): Area {
        (Regex("""target area: x=(?<minX>-?\d+)\.\.(?<maxX>-?\d+), y=(?<minY>-?\d+)\.\.(?<maxY>-?\d+)""")
            .matchEntire(this) ?: throw IllegalArgumentException("Could not parse area >$this<"))
            .groups.let {
                return Area(
                    it["minX"]!!.value.toInt()..it["maxX"]!!.value.toInt(),
                    it["minY"]!!.value.toInt()..it["maxY"]!!.value.toInt()
                )
            }
    }

    fun Area.contains(position: Position): Boolean = position.x in xRange && position.y in yRange

    fun Area.hittingTrajectories(position: Position = Position(0, 0)): List<Sequence<State>> {
        if (position.x != 0 && position.y != 0) {
            val transformedArea = Area(
                xRange.first - position.x..xRange.last - position.x,
                yRange.first - position.y..yRange.last - position.y
            )
            return transformedArea.hittingTrajectories().map { trajectory ->
                trajectory.map {
                    it.copy(
                        position = Position(it.position.x + position.x, it.position.y + position.y)
                    )
                }
            }
        }
        val vXRange = if (0 in xRange) {
            xRange
        } else {
            val signX = xRange.first.sign
            val absXRange = min(
                xRange.first.absoluteValue,
                xRange.last.absoluteValue
            )..max(xRange.first.absoluteValue, xRange.last.absoluteValue)
            val minAbsVx = (1..absXRange.last).first { it * (it + 1) / 2 >= absXRange.first }
            if (signX >= 0) minAbsVx..absXRange.last
            else -minAbsVx..-absXRange.first
        }
        val vYRange = min(
            yRange.first,
            0
        )..if (yRange.first < 0) yRange.first.absoluteValue else 500 //if we shoot upward then wer will reach y=0 with vy=-vy0, so the next step should not overstep
        return vXRange.flatMap { vx ->
            vYRange.mapNotNull { vy ->
                val trajectory = State(Speed(vx, vy)).trajectory()
                if (trajectory.takeWhile { it.speed.vy >= 0 || it.position.y >= yRange.first }
                        .any { this.contains(it.position) })
                    trajectory.takeWhile { !this.contains(it.position) }
                else null
            }
        }
    }


    fun part1(target: Area): Int {
        return target.hittingTrajectories().map { t -> t.maxOf { it.position.y } }.maxOf { it }
    }

    fun part2(target: Area): Int {
        return target.hittingTrajectories().size
    }

    val testInput = readInput("Day17_test").first().toArea()
    check(part1(testInput) == 45)

    val input = readInput("Day17").first().toArea()
    println(part1(input))

    check(part2(testInput) == 112)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 100) { part1(input) }
        benchmark("part2", 100) { part2(input) }
    }

}
