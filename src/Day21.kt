import Day21.GameState
import Day21.QuantumGameState
import Day21.part2
import Day21.roll
import Day21.toPlayer
import kotlin.math.max

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

    data class QuantumGameState(
        val rollingPlayerPosition: Int,
        val otherPlayerPosition: Int,
        val rollingPlayerScore: Int = 0,
        val otherPlayerScore: Int = 0,
        val winningScore: Int = 21
    ) {
        fun finished() =
            otherPlayerScore >= winningScore || rollingPlayerScore >= 21

        companion object {
            val POSSIBLE_ROLL_COUNTS = (1..3)
                .flatMap { r1 ->
                    (1..3).flatMap { r2 ->
                        (1..3).map { r3 -> r1 + r2 + r3 }
                    }
                }.groupingBy { it }.eachCount().toList()
        }
    }

    fun QuantumGameState.next(roll: Int): QuantumGameState? =
        if (finished()) null
        else {
            val newPosition = (rollingPlayerPosition + roll - 1) % 10 + 1
            val newScore = rollingPlayerScore + newPosition
            QuantumGameState(otherPlayerPosition, newPosition, otherPlayerScore, newScore)
        }

    fun QuantumGameState.part2(resultCache: MutableMap<QuantumGameState, Pair<Long, Long>> = mutableMapOf()): Pair<Long, Long> {
        if (this !in resultCache) {
            resultCache[this] = this.let { state ->
                if (state.finished()) {
                    if (state.otherPlayerPosition >= state.winningScore) {
                        1L to 0L
                    } else {
                        0L to 1L
                    }
                } else
                    QuantumGameState.POSSIBLE_ROLL_COUNTS.fold(0L to 0L) { acc, (roll, count) ->
                        this.next(roll)!!.let { nextState ->
                            val p2 = nextState.part2(resultCache)
                            acc.first + p2.second * count to acc.second + p2.first * count
                        }
                    }
            }
        }
        return resultCache.getValue(this)
    }
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

    fun part2(input: List<String>): Long {
        val (player1, player2) = input.map { it.toPlayer() }
        val wins = QuantumGameState(player1.position, player2.position).part2()
        return max(wins.first, wins.second)
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput) == 739785)

    val input = readInput("Day21")
    println(part1(input))

    check(part2(testInput) == 444356092776315)
    println(part2(input))

    for (round in 1..3) {
        println("------------ Round $round!-------- FIGHT!")
        benchmark("part1", 1000) { part1(input) }
        benchmark("part2", 30) { part2(input) }
    }

}
