using DictionaryApp.Services;
using System.Net.Http.Headers;
namespace DictionaryApp.Views;

public partial class UploadPage : ContentPage
{
    private readonly FileUploadService _fileUploadService;
    private readonly AudioService _audioService;

    public UploadPage(FileUploadService fileUploadService, AudioService audioService)
    {
        InitializeComponent();
        _fileUploadService = fileUploadService;
        _audioService = audioService;
    }

    public async Task RequestPermissionsAsync()
    {
        var statusStorage = await Permissions.RequestAsync<Permissions.Photos>();
        if (statusStorage != PermissionStatus.Granted)
        {
            await DisplayAlert("Permission Denied", "Photo Library permission is required to access files.", "OK");
        }

        var statusAudio = await Permissions.RequestAsync<Permissions.Microphone>();
        if (statusAudio != PermissionStatus.Granted)
        {
            await DisplayAlert("Permission Denied", "Microphone permission is required to record audio.", "OK");
        }
    }

    private async void OnSelectFileClicked(object sender, EventArgs e)
    {
        await RequestPermissionsAsync();
        try
        {
            var selectedFile = await _fileUploadService.SelectFileAsync();

            if (selectedFile != null)
            {
                UploadStatusLabel.Text = $"File selected: {selectedFile}";

                var fileResult = await FilePicker.Default.PickAsync();
                if (fileResult != null)
                {
                    string uploadStatus = await _fileUploadService.UploadFileAsync(fileResult);
                    UploadStatusLabel.Text = uploadStatus;
                }
            }
            else
            {
                UploadStatusLabel.Text = "No file selected.";
            }
        }
        catch (Exception ex)
        {
            UploadStatusLabel.Text = "Error selecting file: " + ex.Message;
        }
    }

    private async void OnSelectAudioFilesClicked(object sender, EventArgs e)
    {
        await RequestPermissionsAsync();
        try
        {
            var filePickerResult = await FilePicker.PickMultipleAsync(new PickOptions
            {
                FileTypes = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>>
                {
                    { DevicePlatform.Android, new[] { "audio/*" } },
                    { DevicePlatform.iOS, new[] { "public.audio" } },
                    { DevicePlatform.MacCatalyst, new[] { "public.audio" } },
                    { DevicePlatform.WinUI, new[] { ".mp3", ".wav", ".ogg", ".m4a" } }
                }),
                PickerTitle = "Select Audio Files"
            });

            if (filePickerResult != null && filePickerResult.Any())
            {
                UploadStatusLabel.Text = "Uploading audio files...";
                UploadStatusLabel.TextColor = Colors.Blue;

                string uploadStatus = await _fileUploadService.UploadAudioFilesAsync(filePickerResult);

                UploadStatusLabel.Text = uploadStatus.Contains("successfully")
                    ? "Audio files uploaded successfully!"
                    : "Audio file upload failed.";

                UploadStatusLabel.TextColor = uploadStatus.Contains("successfully") ? Colors.Green : Colors.Red;
            }
            else
            {
                UploadStatusLabel.Text = "No audio files selected.";
                UploadStatusLabel.TextColor = Colors.Red;
            }
        }
        catch (Exception ex)
        {
            UploadStatusLabel.Text = "Error selecting audio files: " + ex.Message;
            UploadStatusLabel.TextColor = Colors.Red;
        }
    }
}
