using ClosedXML.Excel;
using DictionaryManagementApp.Resources.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Services
{
    public static class ExcelParserService
    {
        public static List<WordPreviewItem> ParsePreviewFromExcel(Stream stream)
        {
            var result = new List<WordPreviewItem>();
            using var workbook = new XLWorkbook(stream);
            var sheet = workbook.Worksheet(1);

            string currentRoot = null;

            foreach (var row in sheet.RowsUsed().Skip(1))
            {
                var rootCell = row.Cell(1).GetString();
                if (!string.IsNullOrWhiteSpace(rootCell))
                    currentRoot = rootCell;

                var wordName = row.Cell(2).GetString();
                var definition = row.Cell(3).GetString();

                var item = new WordPreviewItem
                {
                    Root = currentRoot,
                    WordName = wordName,
                    Definition = definition,
                    Phrases = new ObservableCollection<PhrasePreviewItem>()
                };


                int col = 4;
                while (!string.IsNullOrWhiteSpace(row.Cell(col).GetString()))
                {
                    var phrase = row.Cell(col).GetString();
                    var phraseDef = row.Cell(col + 1).GetString();

                    item.Phrases.Add(new PhrasePreviewItem
                    {
                        Content = phrase,
                        Definition = phraseDef
                    });

                    col += 2;
                }

                result.Add(item);
            }

            return result;
        }

    }

}
