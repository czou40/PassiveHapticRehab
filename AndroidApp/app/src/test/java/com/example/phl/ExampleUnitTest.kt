package com.example.phl

import com.example.phl.data.spasticity.data_collection.RawDataset
import com.example.phl.data.unity.ShoulderExtensionFlexionResult
import com.google.gson.Gson
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun regression_isCorrect() {
        // weight
        val y = doubleArrayOf(50.0, 60.0, 70.0, 80.0, 100.0)
        // height, waist
        val x = arrayOfNulls<DoubleArray>(5)
        x[0] = doubleArrayOf(1.0, 11.0)
        x[1] = doubleArrayOf(1.0, 13.0)
        x[2] = doubleArrayOf(1.0, 15.0)
        x[3] = doubleArrayOf(1.0, 17.0)
        x[4] = doubleArrayOf(1.0, 21.0)

        val rawDataset = RawDataset(2)

        rawDataset.addAll(x, y)

        Assert.assertEquals(90.0, rawDataset.predict(doubleArrayOf(1.0, 19.0)), 0.0001)
    }

    @Test
    fun serialization_isCorrect() {
        val command =
            "sendCompoundScore|Game1|{\"game\":\"Game1\",\"numRounds\":3,\"minAngles\":[0.0,0.0,0.0],\"maxAngles\":[180.0,180.0,180.0],\"score\":180,\"startTime\":1722855084956,\"endTime\":1722855086901}"
        val gson = Gson()
        val parts = command.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val game = parts[1]
        val json = parts[2]
        println(json)
        val result = gson.fromJson(json, ShoulderExtensionFlexionResult::class.java)
        Assert.assertEquals(result.maxAngles, listOf(180.0, 180.0, 180.0))
        Assert.assertEquals(result.minAngles, listOf(0.0, 0.0, 0.0))
        Assert.assertEquals(result.score, 180.0, 0.0001)
        Assert.assertEquals(result.startTime, 1722855084956)
        Assert.assertEquals(result.endTime, 1722855086901)
    }
}