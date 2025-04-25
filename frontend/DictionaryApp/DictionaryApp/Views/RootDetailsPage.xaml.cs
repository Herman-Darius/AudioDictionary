
using DictionaryApp.Models;
using DictionaryApp.Services;
using Microsoft.Maui.Layouts;
using Microsoft.Maui.Platform;
using System.Runtime.CompilerServices;

namespace DictionaryApp.Views;

public partial class RootDetailsPage : ContentPage
{
    private readonly WordService _wordService;
    private readonly PhraseService _phraseService;
    private readonly AudioService _audioService;
    private WordRoot _root;
    private CancellationTokenSource _cts = new();

    public RootDetailsPage(WordRoot root, WordService wordService, PhraseService phraseService, AudioService audioService)
    {
        InitializeComponent();

        _wordService = wordService;
        _phraseService = phraseService;
        _audioService = audioService;

        LoadRootDetails(root);
    }

    public static async Task NavigateToAsync(WordRoot root, WordService wordService, PhraseService phraseService, AudioService audioService)
    {
        var newPage = new RootDetailsPage(root, wordService, phraseService, audioService);
        await Application.Current.MainPage.Navigation.PushAsync(newPage);
    }

    private void LoadRootDetails(WordRoot root)
    {
        _cts = new CancellationTokenSource();
        _root = root;

        RootLabel.Text = _root.name;
        DefinitionLabel.Text = _root.definition;

        LoadPhrases();
    }

    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        _cts.Cancel();
    }

    private async void LoadPhrases()
    {
        try
        {
            var token = _cts.Token;
            var allWords = await _wordService.GetAllWordsAsync();
            token.ThrowIfCancellationRequested();

            var wordMap = allWords.ToDictionary(w => w.wordName.ToLower(), w => w);

            var directPhrases = await _phraseService.GetPhrasesByRootIdAsync(_root.id);
            token.ThrowIfCancellationRequested();

            var relatedPhrases = await _phraseService.GetRelatedPhrasesByRootIdAsync(_root.id);
            token.ThrowIfCancellationRequested();

            MainThread.BeginInvokeOnMainThread(async () =>
            {
                DirectPhrasesStackLayoutInstance.Children.Clear();
                RelatedPhrasesStackLayoutInstance.Children.Clear();

                AddPhrasesToLayout(directPhrases, DirectPhrasesStackLayoutInstance, wordMap);
                AddPhrasesToLayout(relatedPhrases, RelatedPhrasesStackLayoutInstance, wordMap);

                await MainContent.FadeTo(1, 500);
            });
        }
        catch (OperationCanceledException)
        {
            Console.WriteLine("Phrase loading was cancelled.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading phrases: {ex.Message}");
        }
    }

    private void AddPhrasesToLayout(List<Phrase> phrases, StackLayout container, Dictionary<string, Word> wordMap)
    {
        container.Children.Clear();

        foreach (var phrase in phrases)
        {
            var phraseContainer = new Frame
            {
                Style = (Style)Resources["PhraseContainer"]
            };

            var formatted = new FormattedString();
            var words = phrase.content.Split(' ');

            foreach (var rawWord in words)
            {
                var clean = rawWord.Trim('.', ',', '!', '?', ';', ':', '"', '(', ')');
                var key = clean.ToLower();
                bool isLink = wordMap.ContainsKey(key) && key != _root.name.ToLower();

                var span = new Span
                {
                    Text = rawWord + " ",
                    FontSize = 16,
                    TextColor = isLink ? Colors.Blue : Colors.Black,
                    TextDecorations = isLink ? TextDecorations.Underline : TextDecorations.None
                };

                if (isLink)
                {
                    var tappedKey = key;
                    var tapGesture = new TapGestureRecognizer();
                    tapGesture.Tapped += async (s, e) =>
                    {
                        var wordObj = wordMap[tappedKey];
                        var rootData = await _wordService.GetRootByWordAsync(wordObj.wordName);
                        if (rootData != null)
                        {
                            await Navigation.PushAsync(new RootDetailsPage(rootData, _wordService, _phraseService, _audioService));
                        }
                    };
                    span.GestureRecognizers.Add(tapGesture);
                }

                formatted.Spans.Add(span);
            }

            var phraseLabel = new Label
            {
                FormattedText = formatted,
                VerticalOptions = LayoutOptions.Start,
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
                VerticalOptions = LayoutOptions.Start,
                Margin = new Thickness(0, 0, 0, 0)
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

            var phraseTopGrid = new Grid
            {
                ColumnDefinitions =
            {
                new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) },
                new ColumnDefinition { Width = GridLength.Auto }
            }
            };

            phraseTopGrid.Add(phraseLabel, 0, 0);
            phraseTopGrid.Add(new Grid
            {
                Children =
            {
                audioButton,
                loadingIndicator
            },
                HorizontalOptions = LayoutOptions.End,
                VerticalOptions = LayoutOptions.Start
            }, 1, 0);

            var innerLayout = new StackLayout
            {
                Orientation = StackOrientation.Vertical,
                Spacing = 6,
                Children = { phraseTopGrid, definitionLabel }
            };

            phraseContainer.Content = innerLayout;
            container.Children.Add(phraseContainer);
        }
    }


    private async void OnCopyRootClicked(object sender, EventArgs e)
    {
        if (!string.IsNullOrWhiteSpace(_root?.name))
        {
            await Clipboard.SetTextAsync(_root.name);
            await DisplayAlert("Copied!", $"'{_root.name}' has been copied to clipboard.", "OK");
        }
    }
}