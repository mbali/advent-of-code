package year2022

import readInput
import kotlin.math.abs

fun main() {

    data class Position(val x: Int, val y: Int)
    data class Measurement(val sensor: Position, val beacon: Position) {
        val distance = abs(sensor.x - beacon.x) + abs(sensor.y - beacon.y)
    }

    fun Int.asRowCoveredBy(measurement: Measurement): IntRange? {
        val (sensor, beacon) = measurement
        val dy = abs(sensor.y - this)
        val maxDx = measurement.distance - dy
        return when {
            dy > measurement.distance -> null
            else -> (sensor.x - maxDx)..(sensor.x + maxDx)
        }
    }

    fun Measurement.covers(position: Position) = distance <= abs(sensor.x - position.x) + abs(sensor.y - position.y)

    fun List<String>.parseMeasurements(): List<Measurement> {
        val regex = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
        return mapNotNull { line ->
            regex.matchEntire(line)?.destructured?.let { (sx, sy, bx, by) ->
                Measurement(Position(sx.toInt(), sy.toInt()), Position(bx.toInt(), by.toInt()))
            }
        }
    }

    val inputClassifier = "Day15"

    fun part1(input: List<String>, y: Int): Int {
        val measurements = input.parseMeasurements()
        return buildSet {
            measurements.forEach { m ->
                y.asRowCoveredBy(m)?.let { range ->
                    addAll(range)
                }
                if (m.beacon.y == y) {
                    remove(m.beacon.x)
                }
            }
        }.size
    }


    fun part2(input: List<String>, size: Int): Long {
        val measurements = input.parseMeasurements()
        (0..size).forEach { y ->
            var x = 0
            val ranges = measurements.mapNotNull { y.asRowCoveredBy(it) }
            while (x <= size) {
                val position = Position(x, y)
                val lastCovered = ranges.filter { it.contains(x) }.maxOfOrNull { it.last }
                if (lastCovered == null) {
                    return x * 4000000L + y
                } else {
                    x = lastCovered + 1
                }
            }
        }
        throw IllegalArgumentException("No solution found")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput, 10) == 26)

    val input = readInput(2022, inputClassifier)
    println(part1(input, 2_000_000))

    //part2
    check(part2(testInput, 20) == 56_000_011L)
    println(part2(input, 4_000_000))

}
