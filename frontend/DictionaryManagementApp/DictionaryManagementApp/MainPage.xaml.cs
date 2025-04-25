
using DictionaryManagementApp.Resources.Models;
using DictionaryManagementApp.Resources.Services;
using System.Collections.ObjectModel;
using CommunityToolkit.Maui;

namespace DictionaryManagementApp
{
    public partial class MainPage : ContentPage
    {
        private readonly WordAdminService _wordAdminService;
        private readonly ExcelUploadService _excelUploadService;
        private FileResult _selectedFile;
        public ObservableCollection<WordPreviewItem> PreviewItems { get; set; } = new();

        public MainPage(WordAdminService wordAdminService, ExcelUploadService excelUploadService)
        {
            InitializeComponent();
            BindingContext = this;

            _wordAdminService = wordAdminService;
            _excelUploadService = excelUploadService;
            
        }
        private async void OnAddWordClicked(object sender, EventArgs e)
        {
            var word = WordEntry.Text?.Trim();
            var def = DefinitionEntry.Text?.Trim();
            var root = RootEntry.Text?.Trim();

            if (string.IsNullOrWhiteSpace(word) || string.IsNullOrWhiteSpace(root))
            {
                await DisplayAlert("Eroare", "Cuvântul și rădăcina sunt obligatorii.", "OK");
                return;
            }

            var newWord = new AddWordRequest
            {
                wordName = word,
                definition = def,
                rootName = root 

            };

            var success = await _wordAdminService.AddWordAsync(newWord);
            await DisplayAlert(success ? "Succes" : "Eroare", success ? "Cuvânt adăugat!" : "Eroare la salvare", "OK");
        }

        private async void OnSelectFileClicked(object sender, EventArgs e)
        {
            var result = await FilePicker.PickAsync(new PickOptions
            {
                PickerTitle = "Selectează un fișier Excel",
                FileTypes = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>>
                {
                    { DevicePlatform.WinUI, new[] { ".xlsx" } },
                    { DevicePlatform.Android, new[] { "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" } },
                    { DevicePlatform.iOS, new[] { "com.microsoft.excel.xlsx" } }
                })
            });

            if (result == null)
                return;
            _selectedFile = result;

            // 🧹 Reset first
            DropLabel.Text = "📎 Trage fișierul aici...";
            PreviewItems.Clear();
            PreviewCollection.IsVisible = false;
            ParsingIndicator.IsRunning = true;
            ParsingIndicator.IsVisible = true;

            try
            {
                DropLabel.Text = $"📎 Selectat: {result.FileName}";
                using var stream = await result.OpenReadAsync();

                var previewData = ExcelParserService.ParsePreviewFromExcel(stream);

                if (!previewData.Any())
                {
                    await DisplayAlert("⚠️ Fără conținut", "Fișierul nu conține date recunoscute.", "OK");
                    return;
                }

                foreach (var item in previewData)
                    PreviewItems.Add(item);

                PreviewCollection.IsVisible = true;
            }
            catch (Exception ex)
            {
                await DisplayAlert("Eroare", $"A apărut o problemă la citirea fișierului: {ex.Message}", "OK");
            }
            finally
            {
                ParsingIndicator.IsRunning = false;
                ParsingIndicator.IsVisible = false;
            }
        }


        private async void OnConfirmUploadClicked(object sender, EventArgs e)
        {
            if (_selectedFile == null)
            {
                await DisplayAlert("Eroare", "Nu există niciun fișier selectat!", "OK");
                return;
            }

            var resultMessage = await _excelUploadService.UploadExcelFileAsync(_selectedFile);
            await DisplayAlert("Rezultat încărcare", resultMessage, "OK");

            if (resultMessage.Contains("successfully", StringComparison.OrdinalIgnoreCase))
            {
                PreviewItems.Clear();
                DropLabel.Text = "📎 Trage fișierul aici...";
            }
        }
        private void OnCancelUploadClicked(object sender, EventArgs e)
        {
            PreviewItems.Clear();
            _selectedFile = null;
            DropLabel.Text = "📎 Trage fișierul aici...";
            PreviewCollection.IsVisible = false;
        }




    }

}
