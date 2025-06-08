using DictionaryApp.Views;
using Microsoft.Extensions.DependencyInjection;
using System;

namespace DictionaryApp
{
    public partial class App : Application
    {
        public static IServiceProvider Services { get; private set; }
        public App(IServiceProvider serviceProvider)
        {
            InitializeComponent();
            Services = serviceProvider;
            MainPage = App.Services.GetRequiredService<SplashPage>();
            AppDomain.CurrentDomain.UnhandledException += (sender, e) =>
            {
                Console.WriteLine($"Unhandled exception: {e.ExceptionObject}");
            };

        }


    }
}