using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class AddWordRequest
    {
        public string wordName { get; set; }
        public string? definition { get; set; }
        public string? rootName { get; set; } 
    }
}
