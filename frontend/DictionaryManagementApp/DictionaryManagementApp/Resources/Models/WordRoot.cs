using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class WordRoot
    {
        public int id { get; set; }
        public string name { get; set; }
        public string? normalizedName { get; set; }
        public string? definition { get; set; }
    }
}
