package year2022

import readInput

data class Day14Cavern(val particles: MutableMap<Position, Particle>, val hasVoid: Boolean) {
    private val floor = particles.keys.maxOf { it.y } + when {
        hasVoid -> 0
        else -> 2
    }

    enum class Particle {
        EMPTY,
        SAND,
        ROCK,
        VOID
    }

    data class Position(val x: Int, val y: Int)

    private fun Position.detectParticle(): Particle {
        return particles.getOrElse(this) {
            when {
                y < floor -> Particle.EMPTY
                hasVoid -> Particle.VOID
                else -> Particle.ROCK
            }
        }
    }

    private fun finalPositionOfNextSandGrain(): Position? {
        var position = Position(500, 0)
        var currentParticle = position.detectParticle()
        while (currentParticle == Particle.EMPTY) {
            val below = position.copy(y = position.y + 1)
            val left = below.copy(x = position.x - 1)
            val right = below.copy(x = position.x + 1)
            val next =
                listOf(below, left, right).firstNotNullOfOrNull { p ->
                    val particle = p.detectParticle()
                    if (particle == Particle.EMPTY || particle == Particle.VOID) p to particle else null
                } ?: break
            position = next.first
            currentParticle = next.second
        }
        return position.takeIf { currentParticle == Particle.EMPTY }
    }

    fun floodWithSand() {
        do {
            val p = finalPositionOfNextSandGrain() ?: break
            particles[p] = Particle.SAND
        } while (true)
    }


    companion object {

        fun create(input: List<String>, hasAbbys: Boolean = true): Day14Cavern {
            val walls = mutableMapOf<Position, Particle>()
            input.forEach { line ->
                line.split("->")
                    .map { it.trim() }
                    .map {
                        val (x, y) = it.split(",").map(String::toInt)
                        Position(x, y)
                    }.windowed(2, step = 1)
                    .flatMap { (p1, p2) ->
                        val (x1, y1) = p1
                        val (x2, y2) = p2
                        when {
                            x1 == x2 -> (minOf(y1, y2)..maxOf(y1, y2)).map { y -> Position(x1, y) }
                            y1 == y2 -> (minOf(x1, x2)..maxOf(x1, x2)).map { x -> Position(x, y1) }
                            else -> throw IllegalArgumentException("Unexpected input")
                        }
                    }.forEach { position ->
                        walls[position] = Particle.ROCK
                    }
            }
            return Day14Cavern(walls, hasAbbys)
        }
    }

}

fun main() {

    val inputClassifier = "Day14"

    fun solve(input: List<String>, hasAbbys: Boolean = true): Int {
        val cavern = Day14Cavern.create(input, hasAbbys)
        cavern.floodWithSand()
        return cavern.particles.values.count { it == Day14Cavern.Particle.SAND }
    }

    fun part1(input: List<String>) = solve(input, true)


    fun part2(input: List<String>) = solve(input, false)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 24)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 93)
    println(part2(input))

}
