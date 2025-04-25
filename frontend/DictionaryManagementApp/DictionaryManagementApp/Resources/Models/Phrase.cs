using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class Phrase
    {
        public int id { get; set; }
        public string content { get; set; }
        public string? definition { get; set; }
        public string? audioFile { get; set; }
    }
}
