using DictionaryManagementApp.Resources.Views;

namespace DictionaryManagementApp
{
    public partial class App : Application
    {
        public App(SplashPage splash)
        {
            InitializeComponent();

            MainPage = splash;
        }

        protected override Window CreateWindow(IActivationState? activationState)        
            => new Window(MainPage);
        
    }
}