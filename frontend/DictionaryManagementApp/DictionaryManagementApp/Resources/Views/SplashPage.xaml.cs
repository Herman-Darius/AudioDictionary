namespace DictionaryManagementApp.Resources.Views;

public partial class SplashPage : ContentPage
{
    private readonly IServiceProvider _services;
    public SplashPage(IServiceProvider services)
	{
        InitializeComponent();
        _services = services;
    }
    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await Task.Delay(2000);
        Application.Current.MainPage =
            new NavigationPage(_services.GetRequiredService<MainPage>());
    }
}