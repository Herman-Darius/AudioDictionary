using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using DictionaryApp.Models;
using Plugin.Maui.Audio;
using System.Net.Http;
using System.Threading.Tasks;
using DictionaryApp.Services;


namespace DictionaryApp.Views;

public partial class WordDetailsPage : ContentPage
{
    private readonly WordService _wordService;
    private readonly PhraseService _phraseService;
    private readonly AudioService _audioService;
    private readonly Word _selectedWord;

    public WordDetailsPage(Word selectedWord, WordService wordService, PhraseService phraseService, AudioService audioService)
    {
        InitializeComponent();
        _selectedWord = selectedWord;
        _wordService = wordService;
        _phraseService = phraseService;
        _audioService = audioService;
        BindingContext = _selectedWord;
        LoadPhrases();
    }

    private async void LoadPhrases()
    {
        try
        {
            var (directPhrases, relatedPhrases) = await _phraseService.GetPhrasesAsync(_selectedWord.id);
            var allWordNames = (await _wordService.GetAllWordsAsync()).Select(w => w.wordName.ToLower()).ToList();


            DirectPhrasesStackLayoutInstance.Children.Clear();
            RelatedPhrasesStackLayoutInstance.Children.Clear();

            AddPhrasesToStackLayout(directPhrases, DirectPhrasesStackLayoutInstance, allWordNames);
            AddPhrasesToStackLayout(relatedPhrases, RelatedPhrasesStackLayoutInstance, allWordNames);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading phrases: {ex.Message}");
        }
    }

    private void AddPhrasesToStackLayout(List<Phrase> phrases, StackLayout stackLayout, List<string> allWordNames)
    {
        foreach (var phrase in phrases)
        {
            var phraseLayout = new StackLayout
            {
                Style = (Style)Resources["PhraseContainer"],
                Orientation = StackOrientation.Horizontal,
                Spacing = 10
            };

            var formattedText = new FormattedString();
            var phraseText = phrase.content.Split(' ');

            foreach (var word in phraseText)
            {
                var isLink = allWordNames.Contains(word.ToLower()) && word.ToLower() != _selectedWord.wordName.ToLower();
                var span = new Span
                {
                    Text = word + " ",
                    FontSize = 16,
                    TextColor = isLink ? Colors.Blue : Colors.Black,
                    TextDecorations = isLink ? TextDecorations.Underline : TextDecorations.None
                };

                if (isLink)
                {
                    var tapGesture = new TapGestureRecognizer();
                    tapGesture.Tapped += async (s, e) =>
                    {
                        var tappedWord = word.Trim();
                        var newWord = await _wordService.GetWordByNameAsync(tappedWord);
                        if (newWord != null)
                        {
                            await Navigation.PushAsync(new WordDetailsPage(newWord, _wordService, _phraseService, _audioService));
                        }
                    };
                    span.GestureRecognizers.Add(tapGesture);
                }

                formattedText.Spans.Add(span);
            }

            var phraseLabel = new Label
            {
                FormattedText = formattedText,
                VerticalOptions = LayoutOptions.Center,
                LineBreakMode = LineBreakMode.WordWrap,
                HorizontalOptions = LayoutOptions.StartAndExpand
            };

            var playButton = new Button
            {
                Text = "🔊",
                BackgroundColor = Color.FromArgb("#EEEEEE"),
                FontSize = 16,
                WidthRequest = 44,
                HeightRequest = 44,
                CornerRadius = 22,
                VerticalOptions = LayoutOptions.Center,
                HorizontalOptions = LayoutOptions.End
            };

            playButton.Clicked += async (s, e) =>
            {
                await _audioService.PlayPhraseAudioAsync(phrase.id);
            };

            phraseLayout.Children.Add(phraseLabel);
            phraseLayout.Children.Add(playButton);

            stackLayout.Children.Add(phraseLayout);
        }
    }

    private void ProcessPhrases(List<Phrase> phrases, StackLayout targetStack, HashSet<string> wordSet)
    {
        targetStack.Children.Clear(); // Clear previous items

        if (!phrases.Any())
        {
            targetStack.Children.Add(new Label { Text = "No phrases available.", Style = (Style)Resources["PhraseLabel"] });
            return;
        }

        foreach (var phrase in phrases)
        {
            var phraseLayout = new HorizontalStackLayout { Spacing = 10, HorizontalOptions = LayoutOptions.FillAndExpand };

            var formattedText = new FormattedString();
            var words = phrase.content.Split(' ');

            foreach (var word in words)
            {
                var span = new Span { Text = word + " " };

                if (wordSet.Contains(word.ToLower()))
                {
                    span.TextDecorations = TextDecorations.Underline;
                    span.TextColor = Colors.Blue;

                    // Attach Tap Gesture
                    var tapGesture = new TapGestureRecognizer();
                    tapGesture.Tapped += async (s, e) =>
                    {
                        var selectedWord = await _wordService.GetWordByNameAsync(word);
                        if (selectedWord != null)
                        {
                            await Navigation.PushAsync(new WordDetailsPage(selectedWord, _wordService, _phraseService, _audioService));
                        }
                    };

                    var clickableLabel = new Label { FormattedText = new FormattedString { Spans = { span } } };
                    clickableLabel.GestureRecognizers.Add(tapGesture);
                    phraseLayout.Children.Add(clickableLabel);
                }
                else
                {
                    formattedText.Spans.Add(span);
                }
            }

            if (formattedText.Spans.Count > 0)
            {
                phraseLayout.Children.Add(new Label { FormattedText = formattedText, Style = (Style)Resources["PhraseLabel"] });
            }

            targetStack.Children.Add(phraseLayout);
        }
    }
    private async void OnPlayAudioClicked(object sender, EventArgs e)
    {
        if (_selectedWord == null || string.IsNullOrEmpty(_selectedWord.wordName))
        {
            Console.WriteLine("Error: No word selected for audio playback.");
            return;
        }

        try
        {
            await _audioService.PlayWordAudioAsync(_selectedWord.wordName);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error playing audio: {ex.Message}");
        }
    }
    private async void OnCopyWordClicked(object sender, EventArgs e)
    {
        if (!string.IsNullOrEmpty(_selectedWord?.wordName))
        {
            await Clipboard.SetTextAsync(_selectedWord.wordName);
            await DisplayAlert("Copied!", $"'{_selectedWord.wordName}' has been copied to clipboard.", "OK");
        }
    }

}

