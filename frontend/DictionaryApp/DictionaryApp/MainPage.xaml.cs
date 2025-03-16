using DictionaryApp.Services;
using DictionaryApp.Views;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Collections.ObjectModel;

namespace DictionaryApp
{
    public partial class MainPage : ContentPage
    {
        private readonly WordService _wordService;
        private readonly PhraseService _phraseService;
        public ObservableCollection<string> Alphabet { get; set; }

        public MainPage(WordService wordService, PhraseService phraseService)
        {
            InitializeComponent();
            _wordService = wordService;
            _phraseService = phraseService;

            Alphabet = new ObservableCollection<string>
            {
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"
            };

            BindingContext = this;
        }

        private async void OnSearchTextChanged(object sender, TextChangedEventArgs e)
        {
            var query = e.NewTextValue?.Trim();
            if (string.IsNullOrEmpty(query)) return;

            Console.WriteLine($"Searching for: {query}");

            var words = await _wordService.SearchWordsAsync(query);
            Device.BeginInvokeOnMainThread(() => {
                WordListView.ItemsSource = words;
            });
            WordListView.IsVisible = words.Count > 0;
            NoResultsLabel.IsVisible = words.Count == 0;


        }


        private async void OnWordSelected(object sender, SelectedItemChangedEventArgs e)
        {
            if (e.SelectedItem == null) return;

            var selectedWord = e.SelectedItem as Word;
            if (selectedWord == null) return;

            var wordDetailsPage = new WordDetailsPage(
                selectedWord,
                App.Services.GetRequiredService<WordService>(),
                App.Services.GetRequiredService<PhraseService>(),
                App.Services.GetRequiredService<AudioService>()
            );

            await Navigation.PushAsync(wordDetailsPage);
        }

        private async void OnNavigateToDictionary(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new DictionaryPage());
        }

        private async void OnNavigateToUploadPage(object sender, EventArgs e)
        {
            var fileUploadService = App.Services.GetRequiredService<FileUploadService>();
            var audioService = App.Services.GetRequiredService<AudioService>();

            await Navigation.PushAsync(new UploadPage(fileUploadService, audioService));
        }
        private async void OnLetterClicked(object sender, EventArgs e)
        {
            if (sender is Button button && button.Text is string letter)
            {
                var words = await _wordService.GetWordsByLetterAsync(letter);
                WordListView.ItemsSource = words;
                WordListView.IsVisible = words.Count > 0;
                NoResultsLabel.IsVisible = words.Count == 0;
            }
        }

    }

}
