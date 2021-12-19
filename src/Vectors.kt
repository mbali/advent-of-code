import kotlin.math.absoluteValue

data class Vec3(val x: Int = 0, val y: Int = 0, val z: Int = 0) {
    companion object {
        val ZERO = Vec3()
        val UNITS = listOf(1, -1).flatMap {
            listOf(Vec3(x = it), Vec3(y = it), Vec3(z = it))
        }
    }
}

data class Mat3(val x: Vec3, val y: Vec3, val z: Vec3)

operator fun Vec3.times(s: Int) = Vec3(x * s, y * s, z * s)
operator fun Vec3.plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
operator fun Vec3.minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
infix fun Vec3.dot(v: Vec3) = x * v.x + y * v.y + z * v.z
infix fun Vec3.cross(v: Vec3) = Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
operator fun Mat3.times(v: Vec3) = Vec3(x dot v, y dot v, z dot v)
infix fun Vec3.manhattanDistance(v: Vec3) = (x - v.x).absoluteValue + (y - v.y).absoluteValue + (z - v.z).absoluteValue