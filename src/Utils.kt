import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Reads lines from the given input txt file.
 */
fun readInput(year: Int, name: String) = File("src/year$year", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun String.ints(): List<Int> = Regex("""-?\d+""").findAll(this).map { it.value.toInt() }.toList()

inline fun SHOULD_NOT_REACH(): Nothing = throw IllegalStateException("Should not reach")

fun <T> Iterable<T>.splitAt(
    keepSplitter: Boolean = false,
    keepEmpty: Boolean = false,
    predicate: (T) -> Boolean
): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var current = mutableListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            if (keepSplitter) current.add(element)
            if (keepEmpty || current.isNotEmpty()) result.add(current)
            current = mutableListOf()
        } else {
            current.add(element)
        }
    }
    result.add(current)
    return result
}

fun <E> List<E>.middle(): E {
    return if (isEmpty()) throw NoSuchElementException("List is empty")
    else this[size / 2]
}

fun <E> List<E>.exactMiddle(): E {
    return if (size % 2 == 0) throw NoSuchElementException("List does not have odd number of elements")
    else this[size / 2]
}

@OptIn(ExperimentalTime::class)
fun benchmark(name: String, times: Int = 1, action: () -> Unit) {
    val measuredNanos = (1..times).map { measureTime(action).inWholeNanoseconds }
    val (mean, std) = measuredNanos.meanAndStd()
    println("Average duration of $name in $times executions is ${mean.nanoseconds} (std dev: ${std.nanoseconds})")
}

private fun List<Long>.meanAndStd(): Pair<Double, Double> {
    if (isEmpty()) throw IllegalArgumentException("At least one value is needed for calculation")
    val mean = average()
    val stdSquare = if (size == 1) 0.0 else this.sumOf { (it - mean).pow(2) / (size - 1) }
    return mean to sqrt(stdSquare)
}

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

fun <A, B> crossJoin(a: Iterable<A>, b: Iterable<B>): Sequence<Pair<A, B>> {
    val aSeq = a.asSequence()
    val bSeq = b.asSequence()
    return aSeq.flatMap { aValue ->
        bSeq.map { aValue to it }
    }
}

fun <A, B, C> crossJoin(a: Iterable<A>, b: Iterable<B>, c: Iterable<C>): Sequence<Triple<A, B, C>> {
    val cSeq = c.asSequence()
    return crossJoin(a, b).flatMap { (aValue, bValue) ->
        cSeq.map { Triple(aValue, bValue, it) }
    }
}