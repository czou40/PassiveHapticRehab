package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractData
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.acos
import kotlin.math.sqrt

@Entity(tableName = "ball_test")
data class BallTestRaw  (
    override val sessionId: String,
    val worldLandMarks : Array<DoubleArray>,
    val landMarks:  Array<DoubleArray>,
    val handedness: String,
    override val time: LocalDateTime = LocalDateTime.now(),
    @PrimaryKey
    val id: String = UUID.randomUUID().toString()
    ) : AbstractData {
    fun getStrength(): Double {
        val points = worldLandMarks.map { landmark -> Point3D(landmark[0], landmark[1], landmark[2]) }

        val thumbAngle0 = angle(points[0], points[1], points[2])
        val thumbAngle1 = angle(points[1], points[2], points[3])
        val thumbAngle2 = angle(points[2], points[3], points[4])
        // index = 5, 6, 7, 8
        val indexAngle0 = angle(points[0], points[5], points[6])
        val indexAngle1 = angle(points[5], points[6], points[7])
        val indexAngle2 = angle(points[6], points[7], points[8])
        // middle = 9, 10, 11, 12
        val middleAngle0 = angle(points[0], points[9], points[10])
        val middleAngle1 = angle(points[9], points[10], points[11])
        val middleAngle2 = angle(points[10], points[11], points[12])
        // ring = 13, 14, 15, 16
        val ringAngle0 = angle(points[0], points[13], points[14])
        val ringAngle1 = angle(points[13], points[14], points[15])
        val ringAngle2 = angle(points[14], points[15], points[16])
        // pinkie = 17, 18, 19, 20
        val pinkieAngle0 = angle(points[0], points[17], points[18])
        val pinkieAngle1 = angle(points[17], points[18], points[19])
        val pinkieAngle2 = angle(points[18], points[19], points[20])

        val thumbAverage = (thumbAngle0 + thumbAngle1 + thumbAngle2) / 3
        val indexAverage = (indexAngle0 + indexAngle1 + indexAngle2) / 3
        val middleAverage = (middleAngle0 + middleAngle1 + middleAngle2) / 3
        val ringAverage = (ringAngle0 + ringAngle1 + ringAngle2) / 3
        val pinkieAverage = (pinkieAngle0 + pinkieAngle1 + pinkieAngle2) / 3

        val average =
            (thumbAverage + indexAverage + middleAverage + ringAverage + pinkieAverage) / 5

        return ((180 - average) / 90 * 100)
    }
    fun getDescription(): String {
        val points = worldLandMarks.map { landmark -> Point3D(landmark[0], landmark[1], landmark[2]) }

        val thumbAngle0 = angle(points[0], points[1], points[2])
        val thumbAngle1 = angle(points[1], points[2], points[3])
        val thumbAngle2 = angle(points[2], points[3], points[4])
        // index = 5, 6, 7, 8
        val indexAngle0 = angle(points[0], points[5], points[6])
        val indexAngle1 = angle(points[5], points[6], points[7])
        val indexAngle2 = angle(points[6], points[7], points[8])
        // middle = 9, 10, 11, 12
        val middleAngle0 = angle(points[0], points[9], points[10])
        val middleAngle1 = angle(points[9], points[10], points[11])
        val middleAngle2 = angle(points[10], points[11], points[12])
        // ring = 13, 14, 15, 16
        val ringAngle0 = angle(points[0], points[13], points[14])
        val ringAngle1 = angle(points[13], points[14], points[15])
        val ringAngle2 = angle(points[14], points[15], points[16])
        // pinkie = 17, 18, 19, 20
        val pinkieAngle0 = angle(points[0], points[17], points[18])
        val pinkieAngle1 = angle(points[17], points[18], points[19])
        val pinkieAngle2 = angle(points[18], points[19], points[20])

        val thumbAverage = (thumbAngle0 + thumbAngle1 + thumbAngle2) / 3
        val indexAverage = (indexAngle0 + indexAngle1 + indexAngle2) / 3
        val middleAverage = (middleAngle0 + middleAngle1 + middleAngle2) / 3
        val ringAverage = (ringAngle0 + ringAngle1 + ringAngle2) / 3
        val pinkieAverage = (pinkieAngle0 + pinkieAngle1 + pinkieAngle2) / 3

        val average =
            (thumbAverage + indexAverage + middleAverage + ringAverage + pinkieAverage) / 5

        val strength = ((180 - average) / 90 * 100)
        return "Thumb extension: ${thumbAverage.toInt()}\n" +
                "Index extension: ${indexAverage.toInt()}\n" +
                "Middle extension: ${middleAverage.toInt()}\n" +
                "Ring extension: ${ringAverage.toInt()}\n" +
                "Pinkie extension: ${pinkieAverage.toInt()}\n" +
                "Average: ${average.toInt()}\n" +
                "Strength: $strength%"
    }

    data class Point3D(val x: Double, val y: Double, val z: Double)

    fun vector(point1: Point3D, point2: Point3D): Point3D {
        return Point3D(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z)
    }

    fun dotProduct(vector1: Point3D, vector2: Point3D): Double {
        return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z
    }

    fun magnitude(vector: Point3D): Double {
        return sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
    }

    fun angle(pointA: Point3D, pointB: Point3D, pointC: Point3D): Double {
        val vectorBA = vector(pointB, pointA)
        val vectorBC = vector(pointB, pointC)

        val dotProduct = dotProduct(vectorBA, vectorBC)

        val magnitudeProduct = magnitude(vectorBA) * magnitude(vectorBC)

        val angleInRad = acos(dotProduct / magnitudeProduct)

        // Convert to degrees
        return Math.toDegrees(angleInRad)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BallTestRaw

        if (sessionId != other.sessionId) return false
        if (!worldLandMarks.contentDeepEquals(other.worldLandMarks)) return false
        if (!landMarks.contentDeepEquals(other.landMarks)) return false
        if (handedness != other.handedness) return false
        if (time != other.time) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + worldLandMarks.contentDeepHashCode()
        result = 31 * result + landMarks.contentDeepHashCode()
        result = 31 * result + handedness.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    companion object {
        data class Point3D(val x: Double, val y: Double, val z: Double)

        fun vector(point1: Point3D, point2: Point3D): Point3D {
            return Point3D(point2.x - point1.x, point2.y - point1.y, point2.z - point1.z)
        }

        fun dotProduct(vector1: Point3D, vector2: Point3D): Double {
            return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z
        }

        fun magnitude(vector: Point3D): Double {
            return sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
        }

        fun angle(pointA: Point3D, pointB: Point3D, pointC: Point3D): Double {
            val vectorBA = vector(pointB, pointA)
            val vectorBC = vector(pointB, pointC)

            val dotProduct = dotProduct(vectorBA, vectorBC)

            val magnitudeProduct = magnitude(vectorBA) * magnitude(vectorBC)

            val angleInRad = acos(dotProduct / magnitudeProduct)

            // Convert to degrees
            return Math.toDegrees(angleInRad)
        }
    }
}
