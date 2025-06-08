using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using DictionaryApp.Models;
using Plugin.Maui.Audio;
using System.Net.Http;
using System.Threading.Tasks;
using DictionaryApp.Services;
using Microsoft.Maui.Layouts;


namespace DictionaryApp.Views;

public partial class WordDetailsPage : ContentPage
{
    private readonly Word _word;
    private readonly PhraseService _phraseService;
    private readonly AudioService _audioService;
    private readonly WordService _wordService;
    private CancellationTokenSource _cts = new();

    public WordDetailsPage(Word word, WordService wordService, PhraseService phraseService, AudioService audioService)
    {
        InitializeComponent();

        _word = word;
        _phraseService = phraseService;
        _audioService = audioService;
        _wordService = wordService;

        LoadWordDetails();
    }

    private async void LoadWordDetails()
    {
        _cts = new CancellationTokenSource();

        WordLabel.Text = _word.wordName;
        DefinitionLabel.Text = _word.definition;
        WordImage.Source = await _wordService.GetWordImageAsync(_word.imageFile);


        await LoadPhrases();
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        _cts.Cancel();
    }

    private async Task LoadPhrases()
    {
        try
        {
            LoadingIndicator.IsRunning = true;
            LoadingIndicator.IsVisible = true;

            var phrases = await _phraseService.GetPhrasesForWordAsync(_word.id);

            MainThread.BeginInvokeOnMainThread(() =>
            {
                PhrasesStackLayout.Children.Clear();

                foreach (var phrase in phrases)
                {
                    var phraseContainer = new Frame
                    {
                        Style = (Style)Resources["PhraseContainer"]
                    };

                    var phraseLabel = new Label
                    {
                        Text = phrase.content,
                        FontSize = 16,
                        LineBreakMode = LineBreakMode.WordWrap,
                        Style = (Style)Resources["RusticPhraseText"]
                    };

                    var definitionLabel = new Label
                    {
                        Text = phrase.definition,
                        FontSize = 14,
                        TextColor = Colors.Gray,
                        FontAttributes = FontAttributes.Italic
                    };

                    var audioButton = new ImageButton
                    {
                        Source = "speaker_icon.png",
                        BackgroundColor = Color.FromArgb("#EEE5D5"),
                        WidthRequest = 32,
                        HeightRequest = 32,
                        CornerRadius = 16,
                        Padding = 4,
                        BorderColor = Color.FromArgb("#A67C52"),
                        BorderWidth = 1,
                        Aspect = Aspect.AspectFit,
                        HorizontalOptions = LayoutOptions.End,
                        VerticalOptions = LayoutOptions.Start
                    };

                    var loadingIndicator = new ActivityIndicator
                    {
                        IsRunning = false,
                        IsVisible = false,
                        Color = Colors.Brown,
                        WidthRequest = 20,
                        HeightRequest = 20,
                        VerticalOptions = LayoutOptions.Center,
                        HorizontalOptions = LayoutOptions.End
                    };

                    audioButton.Clicked += async (s, e) =>
                    {
                        try
                        {
                            await audioButton.ScaleTo(0.9, 80, Easing.CubicOut);
                            await audioButton.ScaleTo(1.05, 80, Easing.CubicIn);
                            await audioButton.ScaleTo(1.0, 50, Easing.Linear);

                            audioButton.Opacity = 0.5;
                            audioButton.IsEnabled = false;
                            loadingIndicator.IsRunning = true;
                            loadingIndicator.IsVisible = true;

                            await _audioService.PlayPhraseAudioAsync(phrase.id);
                        }
                        finally
                        {
                            loadingIndicator.IsRunning = false;
                            loadingIndicator.IsVisible = false;
                            audioButton.Opacity = 1.0;
                            audioButton.IsEnabled = true;
                        }
                    };

                    var topGrid = new Grid
                    {
                        ColumnDefinitions =
                        {
                            new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) },
                            new ColumnDefinition { Width = GridLength.Auto }
                        }
                    };

                    topGrid.Add(phraseLabel, 0, 0);
                    topGrid.Add(new Grid
                    {
                        Children = { audioButton, loadingIndicator },
                        HorizontalOptions = LayoutOptions.End,
                        VerticalOptions = LayoutOptions.Start
                    }, 1, 0);

                    var innerLayout = new StackLayout
                    {
                        Orientation = StackOrientation.Vertical,
                        Spacing = 6,
                        Children = { topGrid, definitionLabel }
                    };

                    phraseContainer.Content = innerLayout;
                    PhrasesStackLayout.Children.Add(phraseContainer);
                }

                LoadingIndicator.IsRunning = false;
                LoadingIndicator.IsVisible = false;
                MainContent.FadeTo(1, 500);
            });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading phrases: {ex.Message}");
        }
    }

    private async void OnCopyWordClicked(object sender, EventArgs e)
    {
        if (!string.IsNullOrWhiteSpace(_word.wordName))
        {
            await Clipboard.SetTextAsync(_word.wordName);
            await DisplayAlert("Copied!", $"'{_word.wordName}' has been copied to clipboard.", "OK");
        }
    }

    private async void OnPlayWordAudioClicked(object sender, EventArgs e)
    {
        if (!string.IsNullOrWhiteSpace(_word.wordName))
        {
            await _audioService.PlayWordAudioAsync(_word.wordName);
        }
    }

    private async void OnGoToMainPageClicked(object sender, EventArgs e)
    {
        await Navigation.PopToRootAsync();
    }

    private async void OnWordLabelTapped(object sender, EventArgs e)
    {
        if (!string.IsNullOrWhiteSpace(_word.wordName))
        {
            await Clipboard.SetTextAsync(_word.wordName);
            await DisplayAlert("Copiat!", $"Cuvântul '{_word.wordName}' a fost copiat în clipboard.", "OK");
        }
    }

    private async void OnCopyWordTapped(object sender, EventArgs e)
    {
        if (string.IsNullOrWhiteSpace(_word.wordName)) return;

        await Clipboard.SetTextAsync(_word.wordName);

        LeafLabel.Opacity = 0;
        LeafLabel.TranslationY = 0;
        LeafLabel.IsVisible = true;

        await LeafLabel.FadeTo(1, 150);
        await LeafLabel.TranslateTo(0, -20, 500, Easing.SinOut);
        await LeafLabel.FadeTo(0, 250);

        LeafLabel.TranslationY = 0;

        await WordLabel.ScaleTo(1.1, 100);
        await WordLabel.ScaleTo(1.0, 100);

        await DisplayToastAsync();
    }

    private async Task DisplayToastAsync()
    {
        CopyToastLabel.IsVisible = true;
        CopyToastLabel.Opacity = 0;
        await CopyToastLabel.FadeTo(1, 200);
        await Task.Delay(1000);
        await CopyToastLabel.FadeTo(0, 300);
        CopyToastLabel.IsVisible = false;
    }
}

