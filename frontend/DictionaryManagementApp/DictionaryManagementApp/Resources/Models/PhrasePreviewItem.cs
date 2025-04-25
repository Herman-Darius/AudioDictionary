using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace DictionaryManagementApp.Resources.Models
{
    public class PhrasePreviewItem
    {
        public string Content { get; set; }
        public string Definition { get; set; }
        public ICommand RemoveCommand { get; set; }
    }
}
