package year2022

import readInput

fun main() {

    val inputClassifier = "Day06"


    fun firstMarkerPosition(
        stream: String,
        countDistinct: Int
    ) = stream.windowed(countDistinct, 1).takeWhile { it.toSet().size < countDistinct }.count() + countDistinct

    fun firstPacketMarkerPosition(stream: String): Int {
        return firstMarkerPosition(stream, 4)
    }

    fun firstMessageMarkerPosition(stream: String): Int {
        return firstMarkerPosition(stream, 14)
    }

    fun part1(input: List<String>): Int {
        return firstPacketMarkerPosition(input.first());
    }


    fun part2(input: List<String>): Int {
        return firstMessageMarkerPosition(input.first());
    }

    // test if implementation meets criteria from the description, like:
    check(firstPacketMarkerPosition("bvwbjplbgvbhsrlpgdmjqwftvncz") == 5)
    check(firstPacketMarkerPosition("nppdvjthqldpwncqszvftbrmjlhg") == 6)
    check(firstPacketMarkerPosition("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg") == 10)
    check(firstPacketMarkerPosition("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw") == 11)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(firstMessageMarkerPosition("mjqjpqmgbljsphdztnvjfqwrcgsmlb") == 19)
    check(firstMessageMarkerPosition("bvwbjplbgvbhsrlpgdmjqwftvncz") == 23)
    check(firstMessageMarkerPosition("nppdvjthqldpwncqszvftbrmjlhg") == 23)
    check(firstMessageMarkerPosition("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg") == 29)
    check(firstMessageMarkerPosition("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw") == 26)
    println(part2(input))

}
