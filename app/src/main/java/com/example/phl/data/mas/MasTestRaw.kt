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
        val rotationMatrix1 = FloatArray(9)
        val rotationMatrix2 = FloatArray(9)

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