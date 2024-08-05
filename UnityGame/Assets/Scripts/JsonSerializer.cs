using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Newtonsoft.Json.Converters;

using System;
using System.Collections.Generic; 

class CustomContractResolver : DefaultContractResolver
{
    protected override IList<JsonProperty> CreateProperties(Type type, MemberSerialization memberSerialization)
    {
        var properties = base.CreateProperties(type, memberSerialization);

        // Apply camel case naming strategy
        foreach (var property in properties)
        {
            property.PropertyName = Char.ToLowerInvariant(property.PropertyName[0]) + property.PropertyName.Substring(1);
        }

        return properties;
    }
}

public class JsonSerializer
{
    private static JsonSerializerSettings settings = new JsonSerializerSettings
    {
        ContractResolver = new CustomContractResolver(),
        Formatting = Formatting.None,
        Converters = new JsonConverter[] { new StringEnumConverter() }
    };

    public static string ToJson(object obj)
    {
        return JsonConvert.SerializeObject(obj, settings);
    }
}
