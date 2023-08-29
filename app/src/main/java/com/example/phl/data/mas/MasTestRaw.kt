package com.example.phl.data.mas

import android.hardware.SensorManager
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractData
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Entity(tableName = "mas_test")
data class MasTestRaw  (
    override val sessionId: String,
    override val time: LocalDateTime = LocalDateTime.now(),
    val accelerometerX: Float,
    val accelerometerY: Float,
    val accelerometerZ: Float,
    val gyroscopeX: Float,
    val gyroscopeY: Float,
    val gyroscopeZ: Float,
    val rotationAxisXComponent: Float, // x*sin(theta/2)
    val rotationAxisYComponent: Float, // y*sin(theta/2)
    val rotationAxisZComponent: Float, // z*sin(theta/2)
    val rotationAngleComponent: Float, // cos(theta/2)
    val rotationHeadingAccuracy: Float, // Does not seem to reflect the true accuracy of the headings.
    val stage: StageLabel = StageLabel.NONE,
    @PrimaryKey
    val id: String = UUID.randomUUID().toString()
) : AbstractData {


    fun getTimeInSeconds(): Double {
        val instant = time.toInstant(ZonedDateTime.now().offset)
        return (instant.epochSecond * 1_000_000_000 + instant.nano) / 1_000_000_000.0
    }

    fun getAngularAccelerationMagnitude(minus2: MasTestRaw?, minus1: MasTestRaw?, plus1: MasTestRaw?, plus2: MasTestRaw?): Float {
        val (x, y, z) = getAngularAcceleration(minus2, minus1, plus1, plus2)
        return sqrt(x * x + y * y + z * z)
    }

    fun getAngularAcceleration(minus2: MasTestRaw?, minus1: MasTestRaw?, plus1: MasTestRaw?, plus2: MasTestRaw?): Triple<Float, Float, Float> {

        val zeroX: Float = gyroscopeX
        val zeroY: Float = gyroscopeY
        val zeroZ: Float = gyroscopeZ
        val zeroT: Double = getTimeInSeconds()
        val minus1X: Float
        val minus1Y: Float
        val minus1Z: Float
        val minus1T: Double
        val plus1X: Float
        val plus1Y: Float
        val plus1Z: Float
        val plus1T: Double
        val minus2X: Float
        val minus2Y: Float
        val minus2Z: Float
        val minus2T: Double
        val plus2X: Float
        val plus2Y: Float
        val plus2Z: Float
        val plus2T: Double


        // Interpolate the missing values
        if (minus1 == null && plus1 == null) {
            throw IllegalArgumentException("Cannot calculate angular acceleration without at least one point before and after.")
        } else if (minus1 == null) {
            // plus1 is not null
            plus1X = plus1!!.gyroscopeX
            plus1Y = plus1.gyroscopeY
            plus1Z = plus1.gyroscopeZ
            plus1T = plus1.getTimeInSeconds()
            minus1X = zeroX - (plus1X - zeroX)
            minus1Y = zeroY - (plus1Y - zeroY)
            minus1Z = zeroZ - (plus1Z - zeroZ)
            minus1T = zeroT - (plus1T - zeroT)
        } else if (plus1 == null) {
            // minus1 is not null
            minus1X = minus1.gyroscopeX
            minus1Y = minus1.gyroscopeY
            minus1Z = minus1.gyroscopeZ
            minus1T = minus1.getTimeInSeconds()
            plus1X = zeroX + (zeroX - minus1X)
            plus1Y = zeroY + (zeroY - minus1Y)
            plus1Z = zeroZ + (zeroZ - minus1Z)
            plus1T = zeroT + (zeroT - minus1T)
        } else {
            minus1X = minus1.gyroscopeX
            minus1Y = minus1.gyroscopeY
            minus1Z = minus1.gyroscopeZ
            minus1T = minus1.getTimeInSeconds()
            plus1X = plus1.gyroscopeX
            plus1Y = plus1.gyroscopeY
            plus1Z = plus1.gyroscopeZ
            plus1T = plus1.getTimeInSeconds()
        }

        if (minus2 == null) {
            // minus1 is not null
            minus2X = minus1X - (zeroX - minus1X)
            minus2Y = minus1Y - (zeroY - minus1Y)
            minus2Z = minus1Z - (zeroZ - minus1Z)
            minus2T = minus1T - (zeroT - minus1T)
        } else {
            minus2X = minus2.gyroscopeX
            minus2Y = minus2.gyroscopeY
            minus2Z = minus2.gyroscopeZ
            minus2T = minus2.getTimeInSeconds()
        }
        if (plus2 == null) {
            // plus1 is not null
            plus2X = plus1X + (plus1X - zeroX)
            plus2Y = plus1Y + (plus1Y - zeroY)
            plus2Z = plus1Z + (plus1Z - zeroZ)
            plus2T = plus1T + (plus1T - zeroT)
        } else {
            plus2X = plus2.gyroscopeX
            plus2Y = plus2.gyroscopeY
            plus2Z = plus2.gyroscopeZ
            plus2T = plus2.getTimeInSeconds()
        }

        val angularAccelerationMinus21 = Triple((minus1X - minus2X) / (minus1T - minus2T), (minus1Y - minus2Y)/ (minus1T - minus2T), (minus1Z - minus2Z)/ (minus1T - minus2T))
        val angularAccelerationMinus10 = Triple(zeroX - minus1X / (zeroT - minus1T), zeroY - minus1Y / (zeroT - minus1T), zeroZ - minus1Z / (zeroT - minus1T))
        val angularAccelerationPlus01 = Triple(plus1X - zeroX / (plus1T - zeroT), plus1Y - zeroY / (plus1T - zeroT), plus1Z - zeroZ / (plus1T - zeroT))
        val angularAccelerationPlus12 = Triple((plus2X - plus1X) / (plus2T - plus1T), (plus2Y - plus1Y) / (plus2T - plus1T), (plus2Z - plus1Z) / (plus2T - plus1T))

        // weight: 0.1, 0.4, 0.4, 0.1
        val angularAccelerationX = 0.1f * angularAccelerationMinus21.first + 0.4f * angularAccelerationMinus10.first + 0.4f * angularAccelerationPlus01.first + 0.1f * angularAccelerationPlus12.first
        val angularAccelerationY = 0.1f * angularAccelerationMinus21.second + 0.4f * angularAccelerationMinus10.second + 0.4f * angularAccelerationPlus01.second + 0.1f * angularAccelerationPlus12.second
        val angularAccelerationZ = 0.1f * angularAccelerationMinus21.third + 0.4f * angularAccelerationMinus10.third + 0.4f * angularAccelerationPlus01.third + 0.1f * angularAccelerationPlus12.third

        return Triple(angularAccelerationX.toFloat(), angularAccelerationY.toFloat(), angularAccelerationZ.toFloat())
    }

    fun getAccelerationMagnitude(): Float {
        return sqrt(accelerometerX * accelerometerX + accelerometerY * accelerometerY + accelerometerZ * accelerometerZ)
    }

    fun getAngularVelocityMagnitude(): Float {
        return sqrt(gyroscopeX * gyroscopeX + gyroscopeY * gyroscopeY + gyroscopeZ * gyroscopeZ)
    }

    fun getRotationVector(): FloatArray {
        return floatArrayOf(rotationAxisXComponent, rotationAxisYComponent, rotationAxisZComponent, rotationAngleComponent)
    }

    fun getRotationMatrix(): FloatArray {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, getRotationVector())
        return rotationMatrix
    }

    fun getOrientation(): FloatArray {
        return FloatArray(3).apply {
            SensorManager.getOrientation(getRotationMatrix(), this)
        } // orientation contains: azimuth, pitch and roll
    }

    fun angleWithHorizontalPlane(): Float {
        val rotationMatrix = getRotationMatrix()
        val zComponent = rotationMatrix[8]
        val theta = acos(zComponent)
        val angleInDegrees = Math.toDegrees(theta.toDouble())
        return angleInDegrees.toFloat()
    }

    fun angleWith(other: MasTestRaw): Float {
        val thisRotationMatrix = getRotationMatrix()
        val otherRotationMatrix = other.getRotationMatrix()

        val rotatedThisNormalVector = floatArrayOf(thisRotationMatrix[2], thisRotationMatrix[5], thisRotationMatrix[8])
        val rotatedOtherNormalVector = floatArrayOf(otherRotationMatrix[2], otherRotationMatrix[5], otherRotationMatrix[8])

        val dotProduct = rotatedThisNormalVector[0] * rotatedOtherNormalVector[0] + rotatedThisNormalVector[1] * rotatedOtherNormalVector[1] + rotatedThisNormalVector[2] * rotatedOtherNormalVector[2]
        val magnitude1 = sqrt(rotatedThisNormalVector[0].toDouble() * rotatedThisNormalVector[0] + rotatedThisNormalVector[1] * rotatedThisNormalVector[1] + rotatedThisNormalVector[2] * rotatedThisNormalVector[2])
        val magnitude2 = sqrt(rotatedOtherNormalVector[0].toDouble() * rotatedOtherNormalVector[0] + rotatedOtherNormalVector[1] * rotatedOtherNormalVector[1] + rotatedOtherNormalVector[2] * rotatedOtherNormalVector[2])
        val cosTheta = min(1.0, max(-1.0, dotProduct / (magnitude1 * magnitude2)))
        val angle = acos(cosTheta) * 180 / Math.PI
        return angle.toFloat()
    }

    companion object {
        enum class StageLabel {
            NONE,
            STARTING_STILL,
            STARTING_MOVEMENT,
            MOVING,
            STOPPING_MOVEMENT,
            STOPPING_STILL
        }
    }
}