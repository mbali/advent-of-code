import kotlin.math.absoluteValue

data class Vec3(val x: Int = 0, val y: Int = 0, val z: Int = 0) {

    companion object {
        val ZERO = Vec3()
        val UNITS = listOf(1, -1).flatMap {
            listOf(Vec3(x = it), Vec3(y = it), Vec3(z = it))
        }
    }

    override fun toString(): String {
        return "[$x, $y, $z]"
    }
}

data class Mat3(val x: Vec3, val y: Vec3, val z: Vec3) {
    override fun toString(): String {
        return "[$x\n$y\n$z]"
    }

    companion object {
        val IDENTITY = Mat3(Vec3(x = 1), Vec3(y = 1), Vec3(z = 1))
    }
}

operator fun Vec3.times(s: Int) = Vec3(x * s, y * s, z * s)
operator fun Vec3.div(d: Int): Vec3 {
    assert(x % d == 0 && y % d == 0 && z % d == 0)
    return Vec3(x / d, y / d, z / d)
}

operator fun Vec3.plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
operator fun Vec3.minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
infix fun Vec3.dot(v: Vec3) = x * v.x + y * v.y + z * v.z
infix fun Vec3.cross(v: Vec3) = Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
operator fun Mat3.times(v: Vec3) = Vec3(x dot v, y dot v, z dot v)
fun Mat3.transpose(): Mat3 = Mat3(Vec3(x.x, y.x, z.x), Vec3(x.y, y.y, z.y), Vec3(x.z, y.z, z.z))
operator fun Mat3.times(m: Mat3) = m.transpose().let { Mat3(it * x, it * y, it * z) }
fun Mat3.determinant(): Int {
    val (x0, x1, x2) = transpose()
    return x0 dot (x1 cross x2)
}

fun Mat3.inverse(): Mat3 {
    val det = this.determinant()
    if (det == 0) throw IllegalArgumentException("Determinant must not be null")
    val (x0, x1, x2) = transpose()
    return Mat3(x1 cross x2, x2 cross x0, x0 cross x1) / det
}

private operator fun Mat3.div(d: Int): Mat3 {
    return Mat3(x / d, y / d, z / d)
}

infix fun Vec3.manhattanDistance(v: Vec3) = (x - v.x).absoluteValue + (y - v.y).absoluteValue + (z - v.z).absoluteValue