using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Models
{
    public class PhraseResponse
    {
        public List<Phrase> directPhrases { get; set; }
        public List<Phrase> relatedPhrases { get; set; }
        public Word word { get; set; }
    }
}
