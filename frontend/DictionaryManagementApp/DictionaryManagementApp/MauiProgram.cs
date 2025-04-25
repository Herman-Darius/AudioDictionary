using CommunityToolkit.Maui;
using DictionaryManagementApp.Resources.Converters;
using DictionaryManagementApp.Resources.Services;
using Microsoft.Extensions.Logging;

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

        builder.Services.AddHttpClient("custom-httpclient", client =>
        {
            client.BaseAddress = new Uri(baseAddress);
        });
        //services
        builder.Services.AddSingleton<ExcelUploadService>();
        builder.Services.AddSingleton<WordAdminService>();
        builder.Services.AddSingleton<ZeroToBoolConverter>();
        

        //pages
        builder.Services.AddSingleton<MainPage>();

#if DEBUG
        builder.Logging.AddDebug();
#endif

		return builder.Build();
	}
}
