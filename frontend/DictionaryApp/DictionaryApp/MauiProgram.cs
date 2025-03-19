using Microsoft.Extensions.Logging;
using CommunityToolkit.Maui;
using Plugin.Maui.Audio;
using Microsoft.Maui.Devices;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Http;
using DictionaryApp.Views;
using DictionaryApp.Services;
using CommunityToolkit.Maui.Core;

namespace DictionaryApp
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.UseMauiApp<App>().ConfigureFonts(fonts =>
            {
                fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
            })
            .UseMauiCommunityToolkit()
            .UseMauiCommunityToolkitMediaElement()
            .UseMauiCommunityToolkitCore();

            //Custom URL
            string baseAddress = DeviceInfo.Platform == DevicePlatform.Android ?
                                 "http://10.0.2.2:8080/" : "http://localhost:8080/";

            builder.Services.AddHttpClient("custom-httpclient", client =>
            {
                client.BaseAddress = new Uri(baseAddress);
            });


            //Services
            builder.Services.AddSingleton<WordService>();
            builder.Services.AddSingleton<PhraseService>();
            builder.Services.AddSingleton<AudioService>();
            builder.Services.AddSingleton<IAudioManager>(AudioManager.Current);

            /*
            builder.Services.AddHttpClient<AudioService>(client =>
            {
                client.BaseAddress = new Uri(baseAddress);
            });*/

            /*builder.Services.AddHttpClient<FileUploadService>(client =>
            {
                client.BaseAddress = new Uri(baseAddress);
            });*/

            builder.Services.AddSingleton<FileUploadService>();


            //Pages
            builder.Services.AddSingleton<MainPage>();
            builder.Services.AddTransient<WordDetailsPage>();
            builder.Services.AddTransient<UploadPage>();
#if DEBUG
            builder.Logging.AddDebug();
#endif
            return builder.Build();
        }
    }
}