import Day20.toAlgorithm
import Day20.toImage

private object Day20 {
    class Image(
        val knownWidth: Int,
        val knownHeight: Int,
        val knownPixels: BooleanArray,
        val defaultPixel: Boolean = false
    ) {
        init {
            if (knownPixels.size != knownWidth * knownHeight) throw IllegalArgumentException("Unexpected image size")
        }

        fun at(x: Int, y: Int): Boolean =
            if (x < 0 || x >= knownWidth || y < 0 || y >= knownHeight) defaultPixel
            else knownPixels[y * knownWidth + x]
    }

    class Algorithm(private val lookupTable: BooleanArray) {
        init {
            if (lookupTable.size != 512) throw IllegalArgumentException("Lookup table should have 512 elements")
        }

        fun apply(image: Image): Image {
            val newWidth = image.knownWidth + 2
            val newHeight = image.knownHeight + 2
            val defaultIndex = if (image.defaultPixel) 511 else 0
            val indices = IntArray(newWidth * newHeight)
            for (y in 0 until newHeight) {
                val rowBase = y * newWidth
                //reuse index of bit to the left
                var index = defaultIndex
                for (x in 0 until newWidth) {
                    index = index.and(0b011011011)
                        .shl(1) //unset bits that reference to the column sliding out of the window, and shift everything left
                    if (image.at(x, y - 2)) index =
                        index or 0b001000000 // if the pixel towards(1,-1) is set, then set the corresponding bit in index
                    if (image.at(x, y - 1)) index =
                        index or 0b000001000 //if the pixel to the right is set, set the corresponding bit
                    if (image.at(x, y)) index = index or 0b000000001 //same for (dx=1,dy=1)
                    indices[rowBase + x] = index
                }
            }
            val newPixels = BooleanArray(indices.size) { lookupTable[indices[it]] }
            return Image(
                newWidth,
                newHeight,
                newPixels,
                lookupTable[defaultIndex]
            )
        }
    }

    fun String.toAlgorithm() = Algorithm(map { it == '#' }.toBooleanArray())

    fun List<String>.toImage() =
        if (isEmpty()) {
            Image(0, 0, BooleanArray(0))
        } else {
            Image(first().length, size, flatMap { row -> row.map { it == '#' } }.toBooleanArray())
        }

}

fun main() {

    fun solution(input: List<String>, enhancements: Int): Int {
        val algorithm = input.first().toAlgorithm()
        val image = input.drop(2).toImage()
        val resultingImage = generateSequence(image) { i -> algorithm.apply(i) }.take(enhancements + 1).last()
        if (resultingImage.defaultPixel) throw IllegalStateException("There are infinite light pixels in the resulting image")
        return resultingImage.knownPixels.count { it }
    }

    fun part1(input: List<String>): Int {
        return solution(input, 2)
    }

    fun part2(input: List<String>): Int {
        return solution(input, 50)
    }

    val testInput = readInput("Day20_test")
    check(part1(testInput) == 35)

    val input = readInput("Day20")
    println(part1(input))

    check(part2(testInput) == 3351)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 1000) { part2(input) }
    }
}
