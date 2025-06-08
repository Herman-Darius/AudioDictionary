using DictionaryApp.Services;
using System;
using System.Threading;
using System.Threading.Tasks;
namespace DictionaryApp.Views;

public partial class SplashPage : ContentPage
{
    private readonly Random _random = new();
    private bool _isActive = true;
    private int currentMessageIndex = 0;
    private CancellationTokenSource cancellationTokenSource = new();

    private readonly List<string> rusticMessages = new()
    {
        "„Încălzim vorbele moștenite...”",
        "„Căutăm printre rădăcini...”",
        "„Împletim graiul din bătrâni...”",
        "„Polișăm proverbele cu grijă...”",
        "„Punem lemnele pe focul limbii române...”",
        "„Ascultăm șoaptele limbii strămoșești...”"
    };
    public SplashPage()
    {
        InitializeComponent();
        SetRandomRusticMessage();
        NavigateToMainPage();
    }
    private void SetRandomRusticMessage()
    {
        var random = new Random();
        var index = random.Next(rusticMessages.Count);
        LoadingMessageLabel.Text = rusticMessages[index];
    }
    private void StartRotatingMessages()
    {
        var token = cancellationTokenSource.Token;

        Task.Run(async () =>
        {
            while (!token.IsCancellationRequested)
            {
                await MainThread.InvokeOnMainThreadAsync(async () =>
                {
                    // Fade out
                    await LoadingMessageLabel.FadeTo(0, 250, Easing.CubicOut);

                    // Update text
                    LoadingMessageLabel.Text = rusticMessages[currentMessageIndex];
                    currentMessageIndex = (currentMessageIndex + 1) % rusticMessages.Count;

                    // Fade in
                    await LoadingMessageLabel.FadeTo(1, 250, Easing.CubicIn);
                });

                await Task.Delay(1000, token);
            }
        }, token);
    }

    private async void NavigateToMainPage()
    {
        await Task.Delay(3000); 
        Application.Current.MainPage = new NavigationPage(new MainPage(
            App.Services.GetRequiredService<WordService>(),
            App.Services.GetRequiredService<PhraseService>(),
            App.Services.GetRequiredService<AudioService>(),
            App.Services.GetRequiredService<HealthService>()
        ));

        Navigation.RemovePage(this);
    }
    protected override void OnDisappearing()
    {
        base.OnDisappearing();
        _isActive = false;
    }
}