import Day19.parseScanResults
import Day19.toScannerLocations
import Day19.transform

private object Day19 {
    private val SCANNER_REGEX = Regex("""--- scanner (?<id>\d+) ---""")
    private val ORIENTATIONS = Vec3.UNITS.flatMap { x ->
        Vec3.UNITS.filter { it dot x == 0 }.map { y -> Mat3(x, y, x cross y) }
    }

    data class ScanResult(val scannerId: Int, val beaconLocations: List<Vec3>)

    data class ScannerLocation(val orientation: Mat3, val location: Vec3)

    infix fun ScannerLocation.transform(vec: Vec3): Vec3 {
        return orientation * vec + location
    }

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
                                    })
                            )
                        }

                }
            } while (line != null)
        }
    }

    private fun getOffsetOrNull(positions: List<Vec3>, candidatePositions: List<Vec3>): Vec3? {
        val setOfPositions = positions.toSet()
        positions.asSequence().forEach { position ->
            candidatePositions.asSequence().forEach { possiblePair ->
                val offset = position - possiblePair
                if (candidatePositions.count { it + offset in setOfPositions } >= 12) return offset
            }
        }
        return null
    }

    private fun findNextMatchingLocation(
        seen: Map<Int, ScannerLocation>,
        scans: List<ScanResult>
    ): Pair<Int, ScannerLocation>? {
        scans.asSequence().filter { it.scannerId in seen.keys }.forEach { processedScan ->
            val processedLocation = seen.getValue(processedScan.scannerId)
            val processedBeaconPositions = processedScan.beaconLocations.map { processedLocation transform it }
            scans.asSequence().filter { it.scannerId !in seen.keys }.forEach { candidateScan ->
                ORIENTATIONS.asSequence().forEach { orientation ->
                    val orientedCandidatePositions = candidateScan.beaconLocations.map { orientation * it }
                    val offset = getOffsetOrNull(processedBeaconPositions, orientedCandidatePositions)
                    if (offset != null) {
                        return candidateScan.scannerId to ScannerLocation(
                            orientation,
                            offset
                        )
                    }
                }
            }
        }
        return null
    }

    fun List<ScanResult>.toScannerLocations(): List<ScannerLocation> {
        val scannerLocations =
            mutableMapOf(
                this.first().scannerId to ScannerLocation(
                    Mat3(Vec3(x = 1), Vec3(y = 1), Vec3(z = 1)),
                    Vec3.ZERO
                )
            )
        do {
            val nextScannerLocation = findNextMatchingLocation(scannerLocations, this)?.also {
                scannerLocations[it.first] = it.second
            }
        } while (nextScannerLocation != null)
        return map { scannerLocations.getValue(it.scannerId) }
    }
}

fun main() {


    fun part1(input: List<String>): Int {
        val scans = input.parseScanResults()
        return scans.zip(scans.toScannerLocations()).flatMap { (scan, location) ->
            scan.beaconLocations.map { location transform it }
        }.toSet().size
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day19_test")

    check(part1(testInput) == 79)

    val input = readInput("Day19")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
