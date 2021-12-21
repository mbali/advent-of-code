import Day21.GameState
import Day21.roll
import Day21.toPlayer

object Day21 {
    data class Player(val position: Int, val score: Int = 0)
    data class GameState(
        val rollingPlayer: Player,
        val otherPlayer: Player,
        val dieIterator: Iterator<Int>,
        val winner: Player? = null,
        val loser: Player? = null,
        val rollCount: Int = 0
    )

    fun GameState.roll(): GameState? {
        if (winner != null) {
            return null
        }
        val newPosition = (1..3).fold(rollingPlayer.position) { p, _ -> (p + dieIterator.next() - 1) % 10 + 1 }
        val updatedPlayer = rollingPlayer.copy(
            position = newPosition,
            score = rollingPlayer.score + newPosition
        )

        return GameState(
            otherPlayer, updatedPlayer, dieIterator,
            if (updatedPlayer.score >= 1000) updatedPlayer else null,
            if (updatedPlayer.score >= 1000) otherPlayer else null,
            rollCount + 3
        )
    }

    fun String.toPlayer() = Player(position = Regex("""\d+$""").find(this)!!.value.toInt())
}

fun main() {

    fun part1(input: List<String>): Int {
        val (player1, player2) = input.map { it.toPlayer() }
        val dieIterator = generateSequence(1) {
            it % 100 + 1
        }.iterator()
        val finalState = generateSequence(GameState(player1, player2, dieIterator)) { s ->
            s.roll()
        }.last()
        return finalState.rollCount * finalState.loser!!.score
    }

    fun part2(input: List<String>): Int {
        TODO()
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput) == 739785)

    val input = readInput("Day21")
    println(part1(input))

    check(part2(testInput) == TODO())
    println(part2(input))
}
