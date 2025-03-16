using DictionaryApp.Models;

namespace DictionaryApp
{
    public class Word
    {
        public int id { get; set; } 
        public string wordName { get; set; } 
        public string definition { get; set; } 
        public string audioFile { get; set; }
        public WordRoot rootName { get; set; }
    }
}