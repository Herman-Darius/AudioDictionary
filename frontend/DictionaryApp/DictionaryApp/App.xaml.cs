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

            AppDomain.CurrentDomain.UnhandledException += (sender, e) =>
            {
                Console.WriteLine($"Unhandled exception: {e.ExceptionObject}");
            };

        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            return new Window(new AppShell());
            
        }
    }
}