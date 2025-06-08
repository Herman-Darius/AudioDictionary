using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class AddWordWithPhrasesDTO
    {
        public string WordName { get; set; } = string.Empty;
        public string Definition { get; set; } = string.Empty;
        public string RootName { get; set; } = string.Empty;
        public List<PhraseDto> Phrases { get; set; } = new();
    }
}
