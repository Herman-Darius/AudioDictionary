using DictionaryManagementApp.Resources.Models;
using DictionaryManagementApp.Resources.Services;
using System.Collections.ObjectModel;

namespace DictionaryManagementApp.Resources.Views;

public partial class WordsPage : ContentPage
{
    public bool IsEmpty => DisplayedWords.Count == 0;
    const int PageSize = 50;
    readonly WordAdminService _wordAdminService;

    private ObservableCollection<Word> _allWordsMaster = new();

    private ObservableCollection<Word> _allWordsView = new();

    public ObservableCollection<Word> DisplayedWords { get; } = new();

    private int _currentPage = 1;
    private bool _isAsc = true;

    public bool CanGoPrev => _currentPage > 1;
    public bool CanGoNext => _currentPage < TotalPages;
    public int TotalPages =>
        (int)Math.Ceiling((double)_allWordsView.Count / PageSize);
    public string PageInfo =>
        $"Pagina {_currentPage} din {TotalPages}";

    public WordsPage(WordAdminService wordAdminService)
    {
        InitializeComponent();
        BindingContext = this;
        _wordAdminService = wordAdminService;
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        // 1 HTTP call
        var all = await _wordAdminService.GetAllWordsAsync();
        _allWordsMaster = new ObservableCollection<Word>(all);

        // init view → full
        _allWordsView = new ObservableCollection<Word>(_allWordsMaster);
        ApplySort();
        RefreshPage();
    }

    // TextChanged and SearchButtonPressed both funnel here:
    private async void SearchBar_TextChanged(object s, TextChangedEventArgs e)
        => await PerformSearch();

    private async void SearchBar_SearchButtonPressed(object s, EventArgs e)
        => await PerformSearch();

    private async Task PerformSearch()
    {
        _currentPage = 1;
        var q = SearchBar.Text?.Trim() ?? "";

        if (string.IsNullOrWhiteSpace(q))
        {
            // no-network branch
            _allWordsView = new ObservableCollection<Word>(_allWordsMaster);
        }
        else
        {
            // remote-lookup branch
            var results = await _wordAdminService.SearchWordsAsync(q);
            _allWordsView = new ObservableCollection<Word>(results);
        }

        ApplySort();
        RefreshPage();
    }

    private void ApplySort()
    {
        var sorted = _isAsc
            ? _allWordsView.OrderBy(w => w.wordName)
            : _allWordsView.OrderByDescending(w => w.wordName);
        _allWordsView = new ObservableCollection<Word>(sorted);
    }

    private void RefreshPage()
    {
        DisplayedWords.Clear();
        var slice = _allWordsView
            .Skip((_currentPage - 1) * PageSize)
            .Take(PageSize);
        foreach (var w in slice)
            DisplayedWords.Add(w);

        OnPropertyChanged(nameof(CanGoPrev));
        OnPropertyChanged(nameof(CanGoNext));
        OnPropertyChanged(nameof(PageInfo));
        OnPropertyChanged(nameof(IsEmpty));
    }

    // sort buttons
    private void OnSortAscClicked(object s, EventArgs e)
    {
        _isAsc = true; _currentPage = 1;
        ApplySort(); RefreshPage();
    }
    private void OnSortDescClicked(object s, EventArgs e)
    {
        _isAsc = false; _currentPage = 1;
        ApplySort(); RefreshPage();
    }

    // paging buttons
    private void OnPrevPageClicked(object s, EventArgs e)
    {
        if (CanGoPrev) _currentPage--;
        RefreshPage();
    }
    private void OnNextPageClicked(object s, EventArgs e)
    {
        if (CanGoNext) _currentPage++;
        RefreshPage();
    }

    private async void OnEditClicked(object s, EventArgs e)
    {
        if (s is Button btn && int.TryParse(btn.CommandParameter?.ToString(), out var wordId))
        {
            var services = Application.Current.Handler.MauiContext.Services;
            var editPage = services.GetRequiredService<EditWordPage>();

            // <-- load the data before you push
            await editPage.LoadWordAsync(wordId);

            await Navigation.PushAsync(editPage);
        }
    }


}