using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Models
{
    public class RootResult
    {
        [JsonProperty("root")]
        public string Root { get; set; }

        [JsonProperty("rootDefinition")]
        public string RootDefinition { get; set; }
    }
}
