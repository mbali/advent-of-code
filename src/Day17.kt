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

    fun Area.contains(position: Position): Boolean = position.x in xRange && position.y in yRange

    fun part1(target: Area): Int {
        //vx should be a value, that "decreases" to 0 in the target area
        //the absolute value of vx must be between 0 and the target areas maximum distance
        val vx = if (0 in target.xRange)
            0
        else {
            val signX = target.xRange.first.sign
            val absXRange = min(
                target.xRange.first.absoluteValue,
                target.xRange.last.absoluteValue
            )..max(target.xRange.first.absoluteValue, target.xRange.last.absoluteValue)
            val absVx = (1..absXRange.last).first { it * (it + 1) / 2 in absXRange }
            absVx * signX
        }
        val minVY = min(target.yRange.first, target.yRange.last)
        var maxY = Int.MIN_VALUE
        for (vy in minVY..500) { //TODO: sensible value
            val trajectory =
                State(Speed(vx, vy)).trajectory().takeWhile { it.speed.vy >= 0 || it.position.y >= target.yRange.first }
                    .toList()
            if (trajectory.any { target.contains(it.position) }) {
                maxY = max(maxY, trajectory.maxOf { it.position.y })
            }
        }
        return maxY
    }

    fun part2(target: Area): Int {
        var count = 0
        for (vx in -500..500) { //TODO: calculate interval
            for (vy in -500..500) { //TODO: some heuristics
                State(Speed(vx, vy)).trajectory().takeWhile { it.speed.vy >= 0 || it.position.y >= target.yRange.first }
                    .firstOrNull { target.contains(it.position) }?.let {
                        count++
                    }
            }
        }
        return count
    }

    val testInput = Area(20..30, -10..-5)
    val input = Area(195..238, -93..-67)
    check(part1(testInput) == 45)

    println(part1(input))

    check(part2(testInput) == 112)
    println(part2(input))
}
