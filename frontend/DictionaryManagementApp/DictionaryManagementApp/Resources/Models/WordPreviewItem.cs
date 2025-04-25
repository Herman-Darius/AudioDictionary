using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace DictionaryManagementApp.Resources.Models
{
    public class WordPreviewItem
    {
        public string Root { get; set; }
        public string WordName { get; set; }
        public string Definition { get; set; }
        public ObservableCollection<PhrasePreviewItem> Phrases { get; set; } = new();

        public ICommand RemoveCommand { get; set; }
    }

}
