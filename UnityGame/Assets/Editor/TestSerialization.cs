using System.Collections;
using NUnit.Framework;
using UnityEngine;
using UnityEngine.TestTools;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

public class TestSerialization
{
    // A Test behaves as an ordinary method
    [Test]
    public void TestSerializationSimplePasses()
    {
        // Use the Assert class to test conditions
        Game1Score score = new Game1Score();

        score.MarkStartTime();

        score.AddRound(-40f, 170f);
        score.AddRound(-30f, 160f);
        score.AddRound(-20f, 150f);

        score.MarkEndTime();

        // Act
        string json = score.ToJson();

        Debug.Log(json);

        // Parse the JSON string using Newtonsoft.Json.Linq.JObject
        JObject jsonObject = JObject.Parse(json);

        // Assert that the fields match
        Assert.AreEqual(score.StartTime, jsonObject["startTime"].ToObject<long>());
        Assert.AreEqual(score.EndTime, jsonObject["endTime"].ToObject<long>());
        Assert.AreEqual(score.MaxAngles.Count, ((JArray)jsonObject["maxAngles"]).Count);
        Assert.AreEqual(score.MinAngles.Count, ((JArray)jsonObject["minAngles"]).Count);
        Assert.AreEqual(score.NumRounds, jsonObject["numRounds"].ToObject<int>());
        Assert.AreEqual(score.MinAngles.Count, score.NumRounds);
        Assert.AreEqual(score.MaxAngles.Count, score.NumRounds);

        for (int i = 0; i < score.NumRounds; i++)
        {
            Assert.AreEqual(score.MinAngles[i], jsonObject["minAngles"][i].ToObject<float>());
            Assert.AreEqual(score.MaxAngles[i], jsonObject["maxAngles"][i].ToObject<float>());
        } 
    }

    // A UnityTest behaves like a coroutine in Play Mode. In Edit Mode you can use
    // `yield return null;` to skip a frame.
    [UnityTest]
    public IEnumerator TestSerializationWithEnumeratorPasses()
    {
        // Use the Assert class to test conditions.
        // Use yield to skip a frame.
        yield return null;
    }
}
