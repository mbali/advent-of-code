package year2022

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pmap
import readInput
import year2022.Day19.maxGeodes

object Day19 {
    enum class Material {
        ORE, CLAY, OBSIDIAN, GEODE;

        companion object {
            fun from(s: String) = when (s) {
                "clay" -> CLAY
                "ore" -> ORE
                "obsidian" -> OBSIDIAN
                "geode" -> GEODE
                else -> error("Unknown material: $s")
            }
        }
    }

    data class RobotCost(val material: Material, val cost: Map<Material, Int>) {
        companion object {
            private val COST_REGEX = Regex("""Each (?<material>[a-z]+) robot costs (?<cost>.*?)\.""")
            fun from(s: String): List<RobotCost> {
                return COST_REGEX.findAll(s).map { m ->
                    val mat = Material.from(m.groups["material"]!!.value)
                    val cost = m.groups["cost"]!!.value.split(" and ").associate {
                        val (c, m) = it.split(" ")
                        Material.from(m) to c.toInt()
                    }
                    RobotCost(mat, cost)
                }.toList()
            }
        }
    }

    data class Inventory(val clay: Int = 0, val ore: Int = 0, val obsidian: Int = 0, val geode: Int = 0) {
        fun add(material: Material, amount: Int) = when (material) {
            Material.CLAY -> copy(clay = clay + amount)
            Material.ORE -> copy(ore = ore + amount)
            Material.OBSIDIAN -> copy(obsidian = obsidian + amount)
            Material.GEODE -> copy(geode = geode + amount)
        }

        fun canAfford(cost: RobotCost) = cost.cost.all { (material, amount) ->
            when (material) {
                Material.CLAY -> clay >= amount
                Material.ORE -> ore >= amount
                Material.OBSIDIAN -> obsidian >= amount
                Material.GEODE -> geode >= amount
            }
        }

        operator fun plus(other: Inventory) = Inventory(
            clay = clay + other.clay,
            ore = ore + other.ore,
            obsidian = obsidian + other.obsidian,
            geode = geode + other.geode
        )

        operator fun minus(other: Inventory) = Inventory(
            clay = clay - other.clay,
            ore = ore - other.ore,
            obsidian = obsidian - other.obsidian,
            geode = geode - other.geode
        )

        operator fun minus(other: RobotCost) = Inventory(
            clay = clay - other.cost.getOrDefault(Material.CLAY, 0),
            ore = ore - other.cost.getOrDefault(Material.ORE, 0),
            obsidian = obsidian - other.cost.getOrDefault(Material.OBSIDIAN, 0),
            geode = geode - other.cost.getOrDefault(Material.GEODE, 0)
        )

        operator fun get(material: Material) = when (material) {
            Material.CLAY -> clay
            Material.ORE -> ore
            Material.OBSIDIAN -> obsidian
            Material.GEODE -> geode
        }
    }

    data class Blueprint(val id: Int, val costs: List<RobotCost>) {

        companion object {
            private val BLUEPRINT_REGEX = Regex("""^Blueprint (\d+):""")
            fun from(line: String): Blueprint {
                val id = BLUEPRINT_REGEX.find(line)!!.groups[1]!!.value.toInt()
                val costs = RobotCost.from(line)
                return Blueprint(id, costs)
            }
        }
    }

    fun Blueprint.maxGeodes(totalTime: Int): Int {

        data class State(val materialInventory: Inventory, val robotInventory: Inventory, val remainingTime: Int)

        val memo = mutableMapOf<State, Int>()

        val maxOreUseByMinute = costs.maxOf { it.cost[Material.ORE] ?: 0 };
        val maxClayUse = costs.maxOf { it.cost[Material.CLAY] ?: 0 }
        val maxObsidianUse = costs.maxOf { it.cost[Material.OBSIDIAN] ?: 0 }

        val maxRobotsNeeded = mapOf(
            Material.ORE to maxOreUseByMinute,
            Material.CLAY to maxClayUse,
            Material.OBSIDIAN to maxObsidianUse,
            Material.GEODE to Int.MAX_VALUE
        )

        fun limitMaterialInventoryByUseAndProduction(materials: Inventory, robots: Inventory, remainingTime: Int) =
            Inventory(
                ore = materials.ore.coerceAtMost((maxOreUseByMinute - robots.ore + 1) * remainingTime),
                clay = materials.clay.coerceAtMost((maxClayUse - robots.clay + 1) * remainingTime),
                obsidian = materials.obsidian.coerceAtMost((maxObsidianUse - robots.obsidian + 1) * remainingTime),
                geode = materials.geode
            )


        fun possibleAdditionalGeodes(materials: Inventory, robots: Inventory, remainingTime: Int): Int {
            val limitedInventory = limitMaterialInventoryByUseAndProduction(materials, robots, remainingTime)
            val key = State(limitedInventory.copy(geode = 0), robots, remainingTime)
            if (key in memo)
                return memo.getValue(key)
            var max = 0
            if (remainingTime > 0) {
                val newMaterialInventory = materials + robots
                val geodesOpened = robots[Material.GEODE]
                for (cost in costs) {
                    if (materials.canAfford(cost) && // can afford
                        maxRobotsNeeded[cost.material]!! > robots[cost.material] // may need a new one
                    ) {
                        val newRobotInventory = robots.add(cost.material, 1)
                        max = maxOf(
                            max,
                            possibleAdditionalGeodes(
                                newMaterialInventory - cost,
                                newRobotInventory,
                                remainingTime - 1
                            )
                        )
                    }
                }
                max = maxOf(
                    max,
                    possibleAdditionalGeodes(
                        newMaterialInventory,
                        robots,
                        remainingTime - 1
                    )
                )
                max += geodesOpened
            }
            memo[key] = max
            return max
        }

        return possibleAdditionalGeodes(Inventory(), Inventory(ore = 1), totalTime)
    }
}

fun main() {

    val inputClassifier = "Day19"

    fun part1(input: List<String>): Int = runBlocking(Dispatchers.Default) {
        val blueprints = input.map { Day19.Blueprint.from(it) }
        blueprints.pmap { it.id * it.maxGeodes(24) }.sum()
    }


    fun part2(input: List<String>): Int {
        return runBlocking(Dispatchers.Default) {
            val blueprints = input.map { Day19.Blueprint.from(it) }
            blueprints.take(3).pmap { it.maxGeodes(32) }.reduce(Int::times)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput(2022, "${inputClassifier}_test")
    check(part1(testInput) == 33)

    val input = readInput(2022, inputClassifier)
    println(part1(input))

    //part2
    check(part2(testInput) == 56 * 62)
    println(part2(input))

}
