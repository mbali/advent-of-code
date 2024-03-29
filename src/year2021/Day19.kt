package year2021

import Mat3
import Vec3
import benchmark
import cross
import dot
import inverse
import year2021.Day19.locateScanners
import year2021.Day19.parseScanResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import manhattanDistance
import minus
import plus
import pmap
import readInput
import times

private object Day19 {
    private val SCANNER_REGEX = Regex("""--- scanner (?<id>\d+) ---""")
    private val ORIENTATIONS = Vec3.UNITS.flatMap { x ->
        Vec3.UNITS.filter { it dot x == 0 }.map { y -> Mat3(x, y, x cross y) }
    }

    data class ScanResult(val scannerId: Int, val beaconLocations: Map<Mat3, List<Vec3>>)

    data class ScannerPosition(val orientation: Mat3, val location: Vec3)

    data class Vertex(val from: ScanResult, val to: ScanResult, val relativePosition: ScannerPosition)

    operator fun Vertex.unaryMinus() = Vertex(to, from, -relativePosition)

    operator fun ScannerPosition.plus(p: ScannerPosition) =
        ScannerPosition(orientation * p.orientation, location + orientation * p.location)

    operator fun ScannerPosition.unaryMinus() =
        ScannerPosition(orientation.inverse(), orientation.inverse() * location * -1)

    fun List<String>.parseScanResults(): List<ScanResult> {
        var i = 0
        fun nextLine(): String? {
            val line = if (i < this.size) this[i] else null
            i++
            return line
        }

        return buildList {
            do {
                val line = nextLine()
                line?.let {
                    SCANNER_REGEX.matchEntire(it)
                        ?.let { matchResult ->
                            val scannerId = matchResult.groups["id"]!!.value.toInt()
                            val scannedPositions = buildList {
                                do {
                                    val positions = nextLine()
                                    if (positions == null || positions.isEmpty()) continue
                                    val (x, y, z) = positions.split(',').map { pos -> pos.toInt() }
                                    add(Vec3(x, y, z))
                                } while (positions != null && positions.isNotEmpty())
                            }
                            add(
                                ScanResult(
                                    scannerId,
                                    ORIENTATIONS.associateWith { o ->
                                        scannedPositions.map { p -> o * p }
                                    }
                                )
                            )
                        }

                }
            } while (line != null)
        }
    }

    private fun getOffsetOrNull(knownPositions: Set<Vec3>, candidatePositions: List<Vec3>): Vec3? {
        val candidateCount = candidatePositions.size
        val maxCandidateIndex = candidateCount - 12
        candidatePositions.forEachIndexed { candidateIdx, candidate ->
            if (candidateIdx > maxCandidateIndex) return null
            val candidatesToCheck = candidatePositions.subList(candidateIdx, candidateCount)
            knownPositions.forEach { known ->
                val offset = known - candidate
                if (candidatesToCheck.count { it + offset in knownPositions } >= 12) return offset
            }
        }
        return null
    }

    fun ScanResult.relativePositionTo(other: ScanResult): ScannerPosition? {
        val knownPositions = other.beaconLocations.getValue(Mat3.IDENTITY).toSet()
        val result = ORIENTATIONS.firstNotNullOfOrNull { o ->
            val orientedPositions = this.beaconLocations.getValue(o)
            getOffsetOrNull(knownPositions, orientedPositions)?.let {
                ScannerPosition(o, it)
            }
        }
        return result
    }

    fun ScanResult.dfs(
        vertices: List<Vertex>,
        position: ScannerPosition = ScannerPosition(Mat3.IDENTITY, Vec3.ZERO),
        seen: MutableSet<Int> = mutableSetOf()
    ): List<Pair<ScanResult, ScannerPosition>> {
        if (scannerId in seen) {
            return emptyList()
        }
        seen.add(scannerId)
        return buildList {
            add(this@dfs to position)
            addAll(
                vertices
                    .filter { it.from == this@dfs }
                    .flatMap {
                        it.to.dfs(vertices, position + it.relativePosition, seen)
                    })
        }
    }

    fun List<ScanResult>.locateScanners(): Map<ScanResult, ScannerPosition> {
        val scans = this
        val vertices =
            runBlocking(Dispatchers.Default) {
                scans.indices.flatMap { i ->
                    (i + 1..scans.lastIndex).map { i to it }
                }.map { (i, j) ->
                    scans[i] to scans[j]
                }.pmap { (s1, s2) ->
                    s2.relativePositionTo(s1)?.let { Vertex(s1, s2, it) }
                }.filterNotNull()
                    .flatMap { v ->
                        listOf(v, -v)
                    }
            }
        val start = vertices.first().from
        return start.dfs(vertices).toMap()
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        return input.parseScanResults().locateScanners()
            .flatMap { (scan, position) ->
                scan.beaconLocations.getValue(position.orientation).map { position.location + it }
            }.toSet().size
    }

    fun part2(input: List<String>): Int {
        val scannerLocations = input.parseScanResults().locateScanners().values.map { it.location }
        return scannerLocations.maxOf { l1 ->
            scannerLocations.maxOf { l2 -> l1 manhattanDistance l2 }
        }
    }

    val testInput = readInput(2021, "Day19_test")
    val input = readInput(2021, "Day19")

    check(part1(testInput) == 79)
    println(part1(input))

    check(part2(testInput) == 3621)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!--------")
        benchmark("positioning scanners", 30) { input.parseScanResults().locateScanners() }
    }

}
