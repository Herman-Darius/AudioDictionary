using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class PhraseDto
    {
        public int? Id { get; set; } 
        public string Content { get; set; }
        public string Definition { get; set; }
        public string AudioFile { get; set; }
    }
}
