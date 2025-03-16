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

            // Add direct phrases
            if (directPhrases.Any())
            {
                foreach (var phrase in directPhrases)
                {
                    DirectPhrasesStackLayoutInstance.Children.Add(new Label
                    {
                        Text = phrase.content,
                        Style = (Style)Resources["PhraseLabel"]
                    });
                }
            }
            else
            {
                DirectPhrasesStackLayoutInstance.Children.Add(new Label { Text = "No direct phrases available.", Style = (Style)Resources["PhraseLabel"] });
            }

            // Add related phrases
            if (relatedPhrases.Any())
            {
                foreach (var phrase in relatedPhrases)
                {
                    string processedPhrase = await _phraseService.ProcessPhrasesWithHyperlinks(phrase.content);

                    // Add the processed phrase with hyperlinks
                    RelatedPhrasesStackLayoutInstance.Children.Add(new Label
                    {
                        Text = processedPhrase,
                        Style = (Style)Resources["PhraseLabel"],
                        TextDecorations = TextDecorations.Underline
                    });
                }
            }
            else
            {
                RelatedPhrasesStackLayoutInstance.Children.Add(new Label { Text = "No related phrases available.", Style = (Style)Resources["PhraseLabel"] });
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading phrases: {ex.Message}");
            RelatedPhrasesStackLayoutInstance.Children.Add(new Label { Text = "Error loading phrases.", Style = (Style)Resources["PhraseLabel"] });
        }
    }

    private async void OnPlayAudioClicked(object sender, EventArgs e)
    {
        try
        {
            await _audioService.PlayWordAudioAsync(_selectedWord.wordName);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error playing audio: {ex.Message}");
        }
    }

}

