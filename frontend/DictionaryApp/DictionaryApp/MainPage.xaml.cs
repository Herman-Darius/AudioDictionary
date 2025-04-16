using DictionaryApp.Models;
using DictionaryApp.Services;
using DictionaryApp.Views;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Collections.ObjectModel;
using Microsoft.Maui.Controls;
using CommunityToolkit.Maui.Views;

namespace DictionaryApp
{
    public partial class MainPage : ContentPage
    {
        private readonly WordService _wordService;
        private readonly PhraseService _phraseService;
        private readonly AudioService _audioService;
        public ObservableCollection<string> Alphabet { get; set; }

        public MainPage(WordService wordService, PhraseService phraseService, AudioService audioService)
        {
            InitializeComponent();
            _audioService = audioService;
            _wordService = wordService;
            _phraseService = phraseService;
            
            SearchBarInstance.TextChanged += OnSearchTextChanged;
            SearchBarInstance.Text = " ";
            SearchBarInstance.Text = "";
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

            if (string.IsNullOrEmpty(query))
            {
                Device.BeginInvokeOnMainThread(() =>
                {
                    SearchLoadingIndicator.IsRunning = false;
                    SearchLoadingIndicator.IsVisible = false;

                    WordListView.ItemsSource = null;
                    WordListView.IsVisible = false;
                    NoResultsLabel.IsVisible = false;
                });
                return;
            }

            // 🔁 Spinner ON before awaiting
            Device.BeginInvokeOnMainThread(() =>
            {
                SearchLoadingIndicator.IsRunning = true;
                SearchLoadingIndicator.IsVisible = true;
            });

            var roots = await _wordService.SearchRootsAsync(query);

            // 🛑 Spinner OFF after search completes
            Device.BeginInvokeOnMainThread(() =>
            {
                SearchLoadingIndicator.IsRunning = false;
                SearchLoadingIndicator.IsVisible = false;

                WordListView.ItemsSource = roots;
                WordListView.IsVisible = roots.Count > 0;
                NoResultsLabel.IsVisible = roots.Count == 0;
            });
        }


        private void OnSpaceClicked(object sender, EventArgs e)
        {
            SearchBarInstance.Text += " ";
        }

        private void OnBackspaceClicked(object sender, EventArgs e)
        {
            if (!string.IsNullOrEmpty(SearchBarInstance.Text))
            {
                SearchBarInstance.Text = SearchBarInstance.Text.Substring(0, SearchBarInstance.Text.Length - 1);
            }
        }

        private void OnClearClicked(object sender, EventArgs e)
        {
            SearchBarInstance.Text = string.Empty;
        }





        private async void OnWordSelected(object sender, SelectionChangedEventArgs e)
        {
            if (e.CurrentSelection.FirstOrDefault() is RootResult selectedRoot)
            {
                WordListView.SelectedItem = null;

                var fullRoot = await _wordService.GetRootByNameAsync(selectedRoot.Root);
                if (fullRoot != null)
                {
                    await Navigation.PushAsync(new RootDetailsPage(fullRoot, _wordService, _phraseService, _audioService));
                }
                else
                {
                    await DisplayAlert("Not Found", "Unable to load full root data.", "OK");
                }
            }
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
        private void OnLetterClicked(object sender, EventArgs e)
        {
            if (sender is Button button)
            {
                string letter = button.Text;
                SearchBarInstance.Text += letter;
            }
        }

        private async void OnPlayWordAudioClicked(object sender, EventArgs e)
        {
            if (sender is Button button && button.CommandParameter is string wordName)
            {
                try
                {
                    await _audioService.PlayWordAudioAsync(wordName);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Error playing audio: {ex.Message}");
                }
            }
        }

        private async void OnPageAppearing(object sender, EventArgs e)
        {
            MainContent.Opacity = 0;
            await MainContent.FadeTo(1, 500, Easing.CubicIn);

            if (WordListView.ItemsSource is not null && WordListView.ItemsSource.Cast<object>().Any())
            {
                WordListView.IsVisible = true;
                NoResultsLabel.IsVisible = false;
            }

            WordListView.SelectedItem = null;
        }
        private void OnInfoClicked(object sender, EventArgs e)
        {
            var popup = new InfoPopup();
            this.ShowPopup(popup);
        }
    }

}
