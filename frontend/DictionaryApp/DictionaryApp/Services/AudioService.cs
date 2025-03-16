using Plugin.Maui.Audio;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Services
{
    public class AudioService
    {
        private readonly HttpClient _httpClient;
        private readonly IAudioManager _audioManager;

        public AudioService(HttpClient httpClient, IAudioManager audioManager)
        {
            _httpClient = httpClient;
            _audioManager = audioManager;
        }
        public async Task PlayWordAudioAsync(string wordName)
        {
            try
            {
                var audioUrl = $"api/audio/play?word={wordName}";
                var responseStream = await _httpClient.GetStreamAsync(audioUrl);
                var player = _audioManager.CreatePlayer(responseStream);
                player.Play();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error playing audio: {ex.Message}");
            }
        }
        public async Task<string> UploadAudioAsync(FileResult file)
        {
            try
            {
                using (var stream = await file.OpenReadAsync())
                {
                    var content = new MultipartFormDataContent();
                    var fileContent = new StreamContent(stream);
                    fileContent.Headers.ContentType = new MediaTypeHeaderValue("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                    content.Add(fileContent, "file", file.FileName);

                    var response = await _httpClient.PostAsync("api/audio/upload", content);

                    if (response.IsSuccessStatusCode)
                    {
                        return "File uploaded successfully!";
                    }
                    else
                    {
                        return "File upload failed.";
                    }
                }
            }
            catch (Exception ex)
            {
                return $"Error uploading file: {ex.Message}";
            }
        }
    }
}
