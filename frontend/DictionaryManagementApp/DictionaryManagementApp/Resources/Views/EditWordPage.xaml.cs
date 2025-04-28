using DictionaryManagementApp.Resources.Models;
using DictionaryManagementApp.Resources.Services;
using System.Collections.ObjectModel;
using CommunityToolkit.Maui.Storage;


namespace DictionaryManagementApp.Resources.Views;

public partial class EditWordPage : ContentPage
{
    readonly WordAdminService _wordService;
    public ObservableCollection<PhraseEditor> PhraseEditors { get; }
        = new();

    private Word _currentWord;
    private string _wordAudioFile, _wordImageFile;

    public EditWordPage(WordAdminService wordService)
    {
        InitializeComponent();
        _wordService = wordService;
        BindingContext = this;
    }

    // **Make this public so MainPage can call it**
    public async Task LoadWordAsync(int wordId)
    {
        _currentWord = await _wordService.GetWordByIdAsync(wordId);

        // fill all the inputs:
        WordNameEntry.Text = _currentWord.wordName;
        RootEntry.Text = _currentWord.Root.name;
        DefinitionEditor.Text = _currentWord.definition;

        _wordAudioFile = _currentWord.audioFile;
        _wordImageFile = _currentWord.imageFile;
        WordAudioLabel.Text = _wordAudioFile ?? "(niciun fișier)";
        WordImageLabel.Text = _wordImageFile ?? "(niciun fișier)";

        PhraseEditors.Clear();
        var phrases = await _wordService.GetPhrasesByWordIdAsync(wordId);
        foreach (var p in phrases)
        {
            PhraseEditors.Add(new PhraseEditor
            {
                Id = p.id,
                Content = p.content,
                Definition = p.definition,
                AudioFileName = p.audioFile
            });
        }
    }

    /// <summary>Adds one more blank phrase editor to the list.</summary>
    void OnAddPhraseClicked(object sender, EventArgs e)
    {
        PhraseEditors.Add(new PhraseEditor());
    }

    /// <summary>Pick a new audio file for the word itself.</summary>
    async void OnPickWordAudioClicked(object sender, EventArgs e)
    {
        var audioTypes = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>> {
                { DevicePlatform.iOS,     new[]{ "public.audio" } },
                { DevicePlatform.Android, new[]{ "audio/*"     } },
                { DevicePlatform.WinUI,   new[]{ ".mp3", ".wav", ".m4a" } }
            });

        var result = await FilePicker.PickAsync(new PickOptions
        {
            PickerTitle = "Selectați fișier audio",
            FileTypes = audioTypes
        });
        if (result != null)
            _wordAudioFile = result.FileName;

        WordAudioLabel.Text = _wordAudioFile ?? "(niciun fișier)";
    }

    /// <summary>Pick a new image file for the word itself.</summary>
    async void OnPickWordImageClicked(object sender, EventArgs e)
    {
        var result = await FilePicker.PickAsync(new PickOptions
        {
            PickerTitle = "Selectați imagine",
            FileTypes = FilePickerFileType.Images
        });
        if (result != null)
            _wordImageFile = result.FileName;

        WordImageLabel.Text = _wordImageFile ?? "(niciun fișier)";
    }

    /// <summary>Gathers all the current values (word + phrases + media) and calls your update API.</summary>
    async void OnSaveClicked(object sender, EventArgs e)
    {
        // 1) Build the word update DTO
        var updateReq = new UpdateWordRequest
        {
            Id = _currentWord.id,
            WordName = WordNameEntry.Text?.Trim() ?? "",
            Definition = DefinitionEditor.Text?.Trim() ?? "",
            RootName = RootEntry.Text?.Trim() ?? ""
        };

        // 2) Build the phrase DTOs
        var phraseDtos = PhraseEditors
            .Select(pe => new PhraseDto
            {
                Id = pe.Id,
                Content = pe.Content?.Trim() ?? "",
                Definition = pe.Definition?.Trim() ?? "",
                AudioFile = pe.AudioFileName
            })
            .ToList();

        // 3) Call your backend
        var success = await _wordService.UpdateWordWithPhrasesAsync(updateReq, phraseDtos);

        await DisplayAlert(
            success ? "Succes" : "Eroare",
            success ? "Modificări salvate." : "Nu am putut salva.",
            "OK");
    }
}