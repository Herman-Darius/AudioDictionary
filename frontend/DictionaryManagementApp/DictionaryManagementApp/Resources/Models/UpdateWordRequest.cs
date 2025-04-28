using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Models
{
    public class UpdateWordRequest
    {
        public int Id { get; set; }
        public string WordName { get; set; }
        public string Definition { get; set; }
        public string RootName { get; set; }
    }
}
