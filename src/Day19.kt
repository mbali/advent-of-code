import Day19.locateScanners
import Day19.parseScanResults
import Day19.transform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private object Day19 {
    private val SCANNER_REGEX = Regex("""--- scanner (?<id>\d+) ---""")
    private val ORIENTATIONS = Vec3.UNITS.flatMap { x ->
        Vec3.UNITS.filter { it dot x == 0 }.map { y -> Mat3(x, y, x cross y) }
    }

    data class ScanResult(val scannerId: Int, val beaconLocations: List<Vec3>)

    data class ScannerPosition(val orientation: Mat3, val location: Vec3)

    infix fun ScannerPosition.transform(v: Vec3) = orientation * v + location

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
                            add(
                                ScanResult(scannerId,
                                    buildList {
                                        do {
                                            val positions = nextLine()
                                            if (positions == null || positions.isEmpty()) continue
                                            val (x, y, z) = positions.split(',').map { pos -> pos.toInt() }
                                            add(Vec3(x, y, z))
                                        } while (positions != null && positions.isNotEmpty())
                                    }
                                )
                            )
                        }

                }
            } while (line != null)
        }
    }

    private fun getOffsetOrNull(knownPositions: Set<Vec3>, candidatePositions: List<Vec3>): Vec3? {
        candidatePositions.forEach { candidate ->
            knownPositions.forEach { known ->
                val offset = known - candidate
                if (candidatePositions.count { it + offset in knownPositions } >= 12) {
                    return offset
                }
            }
        }
        return null
    }

    fun ScanResult.relativePositionTo(other: ScanResult): ScannerPosition? {
        val knownPositions = other.beaconLocations.toSet()
        val result = ORIENTATIONS.firstNotNullOfOrNull { o ->
            val orientedPositions = this.beaconLocations.map { o * it }
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
                scan.beaconLocations.map { position transform it }
            }.toSet().size
    }

    fun part2(input: List<String>): Int {
        val scannerLocations = input.parseScanResults().locateScanners().values.map { it.location }
        return scannerLocations.maxOf { l1 ->
            scannerLocations.maxOf { l2 -> l1 manhattanDistance l2 }
        }
    }

    val testInput = readInput("Day19_test")
    val input = readInput("Day19")

    check(part1(testInput) == 79)
    println(part1(input))

    check(part2(testInput) == 3621)
    println(part2(input))

    benchmark("positioning scanners", 10) {
        input.parseScanResults().locateScanners()
    }
}
