using CommunityToolkit.Maui;
using DictionaryManagementApp.Resources.Converters;
using DictionaryManagementApp.Resources.Services;
using DictionaryManagementApp.Resources.Views;
using Microsoft.Extensions.Logging;
using Polly;
using Polly.Extensions.Http;

namespace DictionaryManagementApp;

public static class MauiProgram
{
	public static MauiApp CreateMauiApp()
	{
		var builder = MauiApp.CreateBuilder();
        builder
            .UseMauiApp<App>()
            .UseMauiCommunityToolkit()
			.ConfigureFonts(fonts =>
			{
				fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
				fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
			});

			
        string baseAddress = DeviceInfo.Platform == DevicePlatform.Android ?
                             "http://10.0.2.2:8080/" : "http://localhost:8080/";

        builder.Services
          .AddHttpClient("custom-httpclient", client =>
          {
              client.BaseAddress = new Uri(baseAddress);
              client.DefaultRequestHeaders.ConnectionClose = true;
          })

          .AddPolicyHandler(HttpPolicyExtensions
            .HandleTransientHttpError()
            .Or<IOException>()
            .RetryAsync(1))

          .SetHandlerLifetime(TimeSpan.FromMinutes(2))
          .ConfigurePrimaryHttpMessageHandler(() => new SocketsHttpHandler
          {
              PooledConnectionLifetime = TimeSpan.FromMinutes(2),
              PooledConnectionIdleTimeout = TimeSpan.FromMinutes(1),
          });

        //services
        builder.Services.AddSingleton<ExcelUploadService>();
        builder.Services.AddTransient<WordAdminService>();
        builder.Services.AddSingleton<ZeroToBoolConverter>();
        

        //pages
        builder.Services.AddSingleton<MainPage>();
        builder.Services.AddSingleton<SplashPage>();
        builder.Services.AddTransient<WordsPage>();
        builder.Services.AddTransient<EditWordPage>();

#if DEBUG
        builder.Logging.AddDebug();
#endif

		return builder.Build();
	}
}
