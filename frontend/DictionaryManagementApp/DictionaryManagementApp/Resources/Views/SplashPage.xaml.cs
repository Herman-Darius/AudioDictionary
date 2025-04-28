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

        // once done, replace the MainPage with your actual page
        // wrap in a NavigationPage if you need a nav bar
        Application.Current.MainPage =
            new NavigationPage(_services.GetRequiredService<MainPage>());
    }
}