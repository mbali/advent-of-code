import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

inline fun SHOULD_NOT_REACH(): Nothing = throw IllegalStateException("Should not reach")

@OptIn(ExperimentalTime::class)
fun benchmark(name: String, times: Int, action: () -> Unit) {
    val avgDuration = (1..times).map { measureTime(action) }.reduce { acc, d -> acc + d }.div(times)
    println("Average duration of $name in $times executions is $avgDuration")
}

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}