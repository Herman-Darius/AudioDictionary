
using DictionaryManagementApp.Resources.Models;
using DictionaryManagementApp.Resources.Services;
using System.Collections.ObjectModel;
using CommunityToolkit.Maui;
using DictionaryManagementApp.Resources.Views;

namespace DictionaryManagementApp
{
    public partial class MainPage : ContentPage
    {
        private readonly WordAdminService _wordAdminService;
        private readonly ExcelUploadService _excelUploadService;
        private FileResult _selectedFile;
        public ObservableCollection<PhraseEditor> PhraseEditors { get; }
      = new ObservableCollection<PhraseEditor>();

        public ObservableCollection<WordPreviewItem> PreviewItems { get; set; } = new();

        private bool _charsVisible = false;
        private VisualElement? _lastFocusedInput = null;

        public MainPage(WordAdminService wordAdminService, ExcelUploadService excelUploadService)
        {
            InitializeComponent();
            BindingContext = this;

            _wordAdminService = wordAdminService;
            _excelUploadService = excelUploadService;

            PhraseEditors.Add(new PhraseEditor());

        }
        void OnAddPhraseClicked(object sender, EventArgs e)
        => PhraseEditors.Add(new PhraseEditor());

        void OnDeletePhraseClicked(object sender, EventArgs e)
        {
            if (sender is ImageButton btn && btn.BindingContext is PhraseEditor pe)
                PhraseEditors.Remove(pe);
        }
        async void OnSaveClicked(object sender, EventArgs e)
        {
            // 1) Validate required word fields
            if (string.IsNullOrWhiteSpace(WordEntry.Text)
             || string.IsNullOrWhiteSpace(RootEntry.Text)
             || string.IsNullOrWhiteSpace(DefinitionEntry.Text))
            {
                await DisplayAlert("Eroare",
                    "Completează Cuvânt, Rădăcină și Definiție.",
                    "OK");
                return;
            }

            // 2) Validate phrase slots: either both empty or both filled
            foreach (var pe in PhraseEditors)
            {
                bool bothEmpty = string.IsNullOrWhiteSpace(pe.Content)
                              && string.IsNullOrWhiteSpace(pe.Definition);
                if (bothEmpty)
                    continue;

                if (string.IsNullOrWhiteSpace(pe.Content)
                 || string.IsNullOrWhiteSpace(pe.Definition))
                {
                    await DisplayAlert("Eroare",
                        "Toate frazele trebuie completate cu text și definiție.",
                        "OK");
                    return;
                }
            }

            // 3) Build wrapper DTO
            var dto = new AddWordWithPhrasesDTO
            {
                WordName = WordEntry.Text.Trim(),
                RootName = RootEntry.Text.Trim(),
                Definition = DefinitionEntry.Text.Trim(),
                Phrases = PhraseEditors
                    .Where(pe => !string.IsNullOrWhiteSpace(pe.Content))
                    .Select(pe => new PhraseDto
                    {
                        Content = pe.Content!.Trim(),
                        Definition = pe.Definition!.Trim()
                    })
                    .ToList()
            };

            // 4) Send to server
            bool ok = await _wordAdminService.AddWordWithPhrasesAsync(dto);
            if (!ok)
            {
                await DisplayAlert("Eroare", "Nu am putut salva cuvântul.", "OK");
                return;
            }

            // 5) Success → notify and reset form (no navigation)
            await DisplayAlert("Succes", "Cuvântul și frazele au fost create.", "OK");
            WordEntry.Text = "";
            RootEntry.Text = "";
            DefinitionEntry.Text = "";
            PhraseEditors.Clear();
            PhraseEditors.Add(new PhraseEditor());
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

            DropLabel.Text = "📎 Trage fișierul aici...";
            PreviewItems.Clear();
            PreviewCollection.IsVisible = false;
            ParsingIndicator.IsRunning = true;
            ParsingIndicator.IsVisible = true;

            try
            {
                var validationResult = await _excelUploadService.ValidateExcelFileAsync(_selectedFile);

                if (!validationResult.IsValid)
                {
                    await DisplayAlert("⚠️ Fișier invalid", validationResult.ErrorMessage, "OK");
                    return;
                }
                DropLabel.Text = $"📎 Selectat: {result.FileName}";
                using var stream = await result.OpenReadAsync();

                var previewData = ExcelParserService.ParsePreviewFromExcel(stream);

                if (!previewData.Any())
                {
                    await DisplayAlert("⚠️ Fără conținut", "Fișierul nu conține date recunoscute.", "OK");
                    return;
                }

                foreach (var item in previewData)
                {
                    // 1️⃣ assign the word-level remove command
                    item.RemoveCommand = new Command(() =>
                    {
                        PreviewItems.Remove(item);
                    });

                    // 2️⃣ assign the phrase-level remove command on each phrase
                    foreach (var ph in item.Phrases)
                    {
                        ph.RemoveCommand = new Command(() =>
                        {
                            item.Phrases.Remove(ph);
                        });
                    }

                    PreviewItems.Add(item);
                }

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
            if (!resultMessage.Contains("successfully", StringComparison.OrdinalIgnoreCase))
            {
                string title;

                if (resultMessage.Contains("Formula‐injection", StringComparison.OrdinalIgnoreCase))
                    title = "⚠️ Tentativă de atac: formulă periculoasă";
                else if (resultMessage.Contains("Invalid MIME type", StringComparison.OrdinalIgnoreCase) ||
                         resultMessage.Contains("ZIP signature", StringComparison.OrdinalIgnoreCase))
                    title = "⚠️ Tentativă de atac: fișier falsificat";
                else
                    title = "⚠️ Alertă sau eroare";

                await DisplayAlert(title, resultMessage, "Înțeleg");
                return;
            }

            else
            {
                await DisplayAlert("Rezultat încărcare", resultMessage, "OK");
            }

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
        private async void OnWordsPageClicked(object sender, EventArgs e)
        {
            var services = Application.Current!.Handler.MauiContext.Services;
            var wordsPage = services.GetRequiredService<WordsPage>();
            await Navigation.PushAsync(wordsPage);
        }


        private async void OnToggleSpecialCharsClicked(object sender, EventArgs e)
        {
            if (_charsVisible)
            {
                await RightPanel.TranslateTo(180, 0, 250, Easing.CubicInOut);
                ToggleArrowButton.Text = "⯇";
                _charsVisible = false;
            }
            else
            {
                await RightPanel.TranslateTo(0, 0, 250, Easing.CubicInOut);
                ToggleArrowButton.Text = "⯈";
                _charsVisible = true;
            }
        }

        private void OnInputFocused(object sender, FocusEventArgs e)
        {
            _lastFocusedInput = sender as VisualElement;
        }

        private void OnSpecialCharClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && !string.IsNullOrEmpty(btn.Text) && _lastFocusedInput != null)
            {
                string charToInsert = btn.Text;

                if (_lastFocusedInput is Entry entry)
                {
                    if (entry.Text == null)
                        entry.Text = ""; // Prevent null insert

                    int pos = entry.CursorPosition;
                    entry.Text = entry.Text.Insert(pos, charToInsert);
                    entry.CursorPosition = pos + charToInsert.Length;
                }
                else if (_lastFocusedInput is Editor editor)
                {
                    if (editor.Text == null)
                        editor.Text = "";

                    int pos = editor.CursorPosition;
                    editor.Text = editor.Text.Insert(pos, charToInsert);
                    editor.CursorPosition = pos + charToInsert.Length;
                }
                else if (_lastFocusedInput is SearchBar search)
                {
                    if (search.Text == null)
                        search.Text = "";

                    int pos = search.CursorPosition;
                    search.Text = search.Text.Insert(pos, charToInsert);
                    search.CursorPosition = pos + charToInsert.Length;
                }
            }
        }

    }

}
