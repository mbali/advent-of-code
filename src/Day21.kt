import Day21.GameState
import Day21.POSSIBLE_DIRAC_ROLL_COUNTS
import Day21.next
import Day21.toInitialState
import kotlin.math.max
import kotlin.math.min

object Day21 {
    data class GameState(
        val rollingPlayerPosition: Int,
        val otherPlayerPosition: Int,
        val winningScore: Int,
        val rollingPlayerScore: Int = 0,
        val otherPlayerScore: Int = 0,
    ) {
        fun finished() =
            otherPlayerScore >= winningScore || rollingPlayerScore >= winningScore

    }

    fun GameState.next(roll: Int): GameState? =
        if (finished()) null
        else {
            val newPosition = (rollingPlayerPosition + roll - 1) % 10 + 1
            val newScore = rollingPlayerScore + newPosition
            GameState(otherPlayerPosition, newPosition, winningScore, otherPlayerScore, newScore)
        }

    val POSSIBLE_DIRAC_ROLL_COUNTS = (1..3)
        .flatMap { r1 ->
            (1..3).flatMap { r2 ->
                (1..3).map { r3 -> r1 + r2 + r3 }
            }
        }.groupingBy { it }.eachCount().toList()

    fun List<String>.toInitialState(winningScore: Int): GameState {
        val (p1, p2) = this.map { line -> Regex("""\d+$""").find(line)!!.value.toInt() }
        return GameState(p1, p2, winningScore)
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        val dieIterator = generateSequence(1) {
            it % 100 + 1
        }.withIndex().iterator()

        val finalState = generateSequence(input.toInitialState(1000)) { s ->
            s.next(dieIterator.next().value + dieIterator.next().value + dieIterator.next().value)
        }.last()
        return (dieIterator.next().index - 3) * //we checked the roll for the last state
                min(finalState.rollingPlayerScore, finalState.otherPlayerScore)
    }

    fun part2(input: List<String>): Long {
        val resultCache: MutableMap<GameState, Pair<Long, Long>> = mutableMapOf()
        fun GameState.helper(): Pair<Long, Long> {
            if (this !in resultCache) {
                resultCache[this] = this.let { state ->
                    if (state.finished()) {
                        if (state.otherPlayerPosition >= state.winningScore) {
                            1L to 0L
                        } else {
                            0L to 1L
                        }
                    } else
                        POSSIBLE_DIRAC_ROLL_COUNTS.fold(0L to 0L) { acc, (roll, count) ->
                            this.next(roll)!!.let { nextState ->
                                val p2 = nextState.helper()
                                acc.first + p2.second * count to acc.second + p2.first * count
                            }
                        }
                }
            }
            return resultCache.getValue(this)
        }
        return input.toInitialState(21).helper().let { max(it.first, it.second) }
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
        benchmark("part2", 100) { part2(input) }
    }

}
