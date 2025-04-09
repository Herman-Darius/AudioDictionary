using DictionaryApp.Models;
using Newtonsoft.Json;

namespace DictionaryApp
{
    public class Word
    {
        [JsonProperty("id")]
        public int id { get; set; }

        [JsonProperty("wordName")]
        public string wordName { get; set; }

        [JsonProperty("definition")]
        public string? definition { get; set; }

        [JsonProperty("audioFile")]
        public string? audioFile { get; set; }

        [JsonProperty("root")]
        public WordRoot Root { get; set; }
    }
}