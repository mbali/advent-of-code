import Day19.parseScanResults
import Day19.toScannerPositions

private object Day19 {
    private val SCANNER_REGEX = Regex("""--- scanner (?<id>\d+) ---""")
    private val ORIENTATIONS = Vec3.UNITS.flatMap { x ->
        Vec3.UNITS.filter { it dot x == 0 }.map { y -> Mat3(x, y, x cross y) }
    }

    data class ScanResult(val scannerId: Int, val beaconLocations: List<Vec3>)

    data class ScannerPosition(val orientation: Mat3, val location: Vec3)

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

    private fun getScannerPositionOrNull(
        scan: ScanResult,
        measurementsByOrientation: Map<Mat3, List<Vec3>>,
        knownPositions: Set<Vec3>
    ): Triple<ScanResult, ScannerPosition, List<Vec3>>? {
        ORIENTATIONS.asSequence().forEach { orientation ->
            val orientedCandidatePositions = measurementsByOrientation.getValue(orientation)
            val offset = getOffsetOrNull(knownPositions, orientedCandidatePositions)
            if (offset != null) {
                val position = ScannerPosition(orientation, offset)
                return Triple(scan, position, orientedCandidatePositions.map { it + offset })
            }
        }
        return null
    }

    /*
     * This function takes all already known beacon positions, instead of checking pairwise
     * The end result is the same, but it does not exactly follow the requirements
     */
    private fun findNextMatchingPositionUsingAllKnownPositions(
        possibleOrientations: Map<ScanResult, Map<Mat3, List<Vec3>>>,
        knownScanResults: Map<ScanResult, Pair<ScannerPosition, List<Vec3>>>,
        scans: List<ScanResult>
    ): Triple<ScanResult, ScannerPosition, List<Vec3>>? {
        val knownPositions = knownScanResults.values.flatMap { it.second }.toSet()
        scans.forEach scan@{ candidate ->
            if (candidate in knownScanResults) return@scan
            val measurementsByOrientation = possibleOrientations.getValue(candidate)
            getScannerPositionOrNull(candidate, measurementsByOrientation, knownPositions)?.let { return it }
        }
        return null
    }

    private fun findNextMatchingPosition(
        possibleOrientations: Map<ScanResult, Map<Mat3, List<Vec3>>>,
        knownScanResults: Map<ScanResult, Pair<ScannerPosition, List<Vec3>>>,
        scans: List<ScanResult>
    ): Triple<ScanResult, ScannerPosition, List<Vec3>>? {
        knownScanResults.values.forEach { (_, knownPositions) ->
            val knownPositionSet = knownPositions.toSet()
            scans.forEach scan@{ candidate ->
                if (candidate in knownScanResults) return@scan
                val measurementsByOrientation = possibleOrientations.getValue(candidate)
                getScannerPositionOrNull(candidate, measurementsByOrientation, knownPositionSet)?.let { return it }
            }
        }
        return null
    }

    fun List<ScanResult>.toScannerPositions(global: Boolean = false): Map<ScanResult, Pair<ScannerPosition, List<Vec3>>> {
        //precalculate orientations for scans
        val possibleOrientations: Map<ScanResult, Map<Mat3, List<Vec3>>> = this.associateWith { scan ->
            ORIENTATIONS.associateWith { orientation ->
                scan.beaconLocations.map { orientation * it }
            }
        }
        //seed
        val seedScan = possibleOrientations.entries.first().toPair()
        val seedOrientation = seedScan.second.entries.first().toPair()
        val scannerLocations =
            mutableMapOf(
                seedScan.first to (ScannerPosition(seedOrientation.first, Vec3.ZERO) to seedOrientation.second)
            )
        do {
            val nextScannerLocation =
                if (global)
                    findNextMatchingPositionUsingAllKnownPositions(
                        possibleOrientations,
                        scannerLocations,
                        this
                    ) else
                    findNextMatchingPosition(
                        possibleOrientations,
                        scannerLocations,
                        this
                    )
            nextScannerLocation?.let {
                scannerLocations[it.first] = it.second to it.third
            }
        } while (nextScannerLocation != null)
        if (scannerLocations.size != size) {
            throw IllegalStateException("Unexpected results, not all scanners were placed!")
        }
        return scannerLocations.toMap()
    }
}

fun main() {

    fun part1(input: List<String>, global: Boolean = false): Int {
        return input.parseScanResults().toScannerPositions(global).values.flatMap { it.second }.toSet().size
    }

    fun part2(input: List<String>, global: Boolean = false): Int {
        val scannerLocations = input.parseScanResults().toScannerPositions(global).values.map { it.first.location }
        return scannerLocations.maxOf { l1 ->
            scannerLocations.maxOf { l2 -> l1 manhattanDistance l2 }
        }
    }

    val testInput = readInput("Day19_test")

    check(part1(testInput) == 79)

    val input = readInput("Day19")
    println(part1(input))

    check(part2(testInput) == 3621)
    println(part2(input))

    benchmark("preprocess with pairwise location matching", 10) {
        input.parseScanResults().toScannerPositions(false)
    }
    benchmark("preprocess with global location matching", 10) {
        input.parseScanResults().toScannerPositions(true)
    }
}
