using System.Net.Http.Headers;
namespace DictionaryApp.Views;

public partial class UploadPage : ContentPage
{
    private readonly HttpClient _httpClient;

    public UploadPage()
    {
        InitializeComponent();
        _httpClient = new HttpClient();
    }

    private async void OnSelectFileClicked(object sender, EventArgs e)
    {
        try
        {
            // Use FilePickerFileType.Custom to restrict to specific file types
            var fileResult = await FilePicker.Default.PickAsync(new PickOptions
            {
                FileTypes = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>>()
            {
                { DevicePlatform.iOS, new[] { "xlsx", "xls" } }, // Excel formats for iOS
                { DevicePlatform.Android, new[] { "xlsx", "xls" } }, // Excel formats for Android
                { DevicePlatform.WinUI, new[] { "xlsx", "xls" } } // Excel formats for Windows
            })
            });

            if (fileResult != null)
            {
                StatusLabel.Text = $"File selected: {fileResult.FileName}";

                // Now upload the selected file
                await UploadFileAsync(fileResult);
            }
        }
        catch (Exception ex)
        {
            StatusLabel.Text = "Error selecting file: " + ex.Message;
        }
    }

    private async Task UploadFileAsync(FileResult file)
    {
        try
        {
            using (var stream = await file.OpenReadAsync())
            {
                var content = new MultipartFormDataContent();
                var fileContent = new StreamContent(stream);
                fileContent.Headers.ContentType = new MediaTypeHeaderValue("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                content.Add(fileContent, "file", file.FileName);

                var response = await _httpClient.PostAsync("http://localhost:8080/api/excel/upload", content);


                if (response.IsSuccessStatusCode)
                {
                    StatusLabel.Text = "File uploaded successfully!";
                }
                else
                {
                    StatusLabel.Text = "File upload failed.";
                }
            }
        }
        catch (Exception ex)
        {
            StatusLabel.Text = "Error uploading file: " + ex.Message;
        }

    }

}