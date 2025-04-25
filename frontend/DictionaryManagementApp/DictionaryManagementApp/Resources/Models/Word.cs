using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class Word
    {
        public int id { get; set; }
        public string wordName { get; set; }
        public string? definition { get; set; }
        public string? audioFile { get; set; }
        public string? imageFile { get; set; }
        public WordRoot Root { get; set; }
        public List<Phrase>? phrases { get; set; } = new();
    }
}
