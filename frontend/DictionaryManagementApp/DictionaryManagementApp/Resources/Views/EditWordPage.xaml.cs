using DictionaryManagementApp.Resources.Models;
using DictionaryManagementApp.Resources.Services;
using System.Collections.ObjectModel;
using CommunityToolkit.Maui.Storage;


namespace DictionaryManagementApp.Resources.Views;

public partial class EditWordPage : ContentPage
{
    readonly WordAdminService _wordService;
    VisualElement? _lastFocusedInput = null;
    bool _charsVisible = true;
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
    public async Task LoadWordAsync(int wordId)
    {
        _currentWord = await _wordService.GetWordByIdAsync(wordId);

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

    void OnAddPhraseClicked(object sender, EventArgs e)
    {
        PhraseEditors.Add(new PhraseEditor());
    }

    async void OnPickWordAudioClicked(object s, EventArgs e)
    {
        var result = await FilePicker.PickAsync(new PickOptions { /* … */ });
        if (result == null) return;

        var newName = await _wordService.UploadWordAudioAsync(_currentWord.id, result);
        if (newName == null)
        {
            await DisplayAlert("Eroare", "Nu am putut încărca audio.", "OK");
            return;
        }

        _wordAudioFile = newName;
        WordAudioLabel.Text = newName;
    }
    void OnInputFocused(object sender, FocusEventArgs e)
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
                    entry.Text = "";

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

    async void OnPickWordImageClicked(object s, EventArgs e)
    {
        var result = await FilePicker.PickAsync(new PickOptions { /* … */ });
        if (result == null) return;

        var newName = await _wordService.UploadWordImageAsync(_currentWord.id, result);
        if (newName == null)
        {
            await DisplayAlert("Eroare", "Nu am putut încărca imagine.", "OK");
            return;
        }

        _wordImageFile = newName;
        WordImageLabel.Text = newName;
    }
    async void OnPickPhraseAudioClicked(object s, EventArgs e)
    {
        if (!(s is Button btn && btn.BindingContext is PhraseEditor pe))
            return;

        var result = await FilePicker.PickAsync(new PickOptions { /* … */ });
        if (result == null) return;

        if (!pe.Id.HasValue)
        {
            await DisplayAlert("Eroare", "Fraza nu are încă un Id.", "OK");
            return;
        }
        var newName = await _wordService.UploadPhraseAudioAsync(pe.Id.Value, result);
        if (newName == null)
        {
            await DisplayAlert("Eroare", "Nu am putut încărca audio frază.", "OK");
            return;
        }
        pe.AudioFileName = newName;
    }
    async void OnSaveClicked(object sender, EventArgs e)
    {
        if (string.IsNullOrWhiteSpace(WordNameEntry.Text)
         || string.IsNullOrWhiteSpace(RootEntry.Text)
         || string.IsNullOrWhiteSpace(DefinitionEditor.Text))
        {
            await DisplayAlert(
                "Câmp obligatoriu",
                "Te rog completează Cuvânt, Rădăcină și Definiție.",
                "OK");
            return;
        }

        foreach (var pe in PhraseEditors)
        {
            if (string.IsNullOrWhiteSpace(pe.Content)
             || string.IsNullOrWhiteSpace(pe.Definition))
            {
                await DisplayAlert(
                    "Fraze incomplete",
                    "Toate frazele trebuie să aibă text și definiție.",
                    "OK");
                return;
            }
        }

        var updateReq = new UpdateWordRequest
        {
            Id = _currentWord.id,
            WordName = WordNameEntry.Text!.Trim(),
            Definition = DefinitionEditor.Text!.Trim(),
            RootName = RootEntry.Text!.Trim()
        };

        var phraseDtos = PhraseEditors
            .Select(pe => new PhraseDto
            {
                Id = pe.Id,
                Content = pe.Content!.Trim(),
                Definition = pe.Definition!.Trim(),
                AudioFile = pe.AudioFileName
            })
            .ToList();

        bool success = await _wordService
            .UpdateWordWithPhrasesAsync(updateReq, phraseDtos);

        await DisplayAlert(
            success ? "Succes" : "Eroare",
            success ? "Modificări salvate." : "Nu am putut salva.",
            "OK");

        if (success)
            await Navigation.PopAsync();
    }


    async void OnDeletePhraseClicked(object sender, EventArgs e)
    {
        // 1) Grab the PhraseEditor VM directly
        if (!(sender is ImageButton btn && btn.BindingContext is PhraseEditor editor))
            return;

        // 2) If it’s an existing phrase (has an Id), confirm + hit backend
        if (editor.Id.HasValue)
        {
            bool confirm = await DisplayAlert(
                "Șterge fraza",
                "Sigur vrei să ștergi această frază?",
                "Da", "Nu");
            if (!confirm)
                return;

            bool ok = await _wordService.DeletePhraseAsync(editor.Id.Value);
            if (!ok)
            {
                await DisplayAlert("Eroare", "Nu am putut șterge fraza.", "OK");
                return;
            }
        }

        // 3) Always remove it from the UI collection
        PhraseEditors.Remove(editor);
    }

    async void OnDeleteWordClicked(object sender, EventArgs e)
    {
        bool ok = await DisplayAlert(
            "Confirmă ștergerea",
            "Sigur vrei să ștergi acest cuvânt și toate frazele lui?",
            "Da", "Nu");
        if (!ok) return;

        bool removed = await _wordService.DeleteWordAsync(_currentWord.id);
        if (!removed)
        {
            await DisplayAlert("Eroare", "Nu am putut șterge cuvântul.", "OK");
            return;
        }

        await DisplayAlert("Șters", "Cuvântul și frazele asociate au fost șterse.", "OK");
        await Navigation.PopAsync();
    }

}