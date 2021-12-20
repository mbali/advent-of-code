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
            val newWidth = image.knownWidth + 4
            val newHeight = image.knownHeight + 4
            val newPixels = BooleanArray(newWidth * newHeight) { idx ->
                val x = idx % newWidth - 2
                val y = idx / newHeight - 2
                val lookupIndex = (-1..1).flatMap { dy ->
                    (-1..1).map { dx ->
                        if (image.at(x + dx, y + dy)) "1" else "0"
                    }
                }.joinToString("").toInt(2)
                lookupTable[lookupIndex]
            }
            return Image(
                newWidth,
                newHeight,
                newPixels,
                if (image.defaultPixel) lookupTable.last() else lookupTable.first()
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

    fun part1(input: List<String>): Int {
        val algorithm = input.first().toAlgorithm()
        val image = input.drop(2).toImage()
        val resultingImage = generateSequence(image) { i -> algorithm.apply(i) }.drop(1).take(2).last()
        if (resultingImage.defaultPixel) throw IllegalStateException("There are infinite light pixels in the resulting image")
        return resultingImage.knownPixels.count { it }
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day20_test")
    check(part1(testInput) == 35)

    val input = readInput("Day20")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
