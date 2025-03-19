using Newtonsoft.Json;
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

        public AudioService(IHttpClientFactory httpClientFactory, IAudioManager audioManager)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
            _audioManager = audioManager;
        }
        public async Task PlayWordAudioAsync(string wordName)
        {
            try
            {
                // 🔹 Request audio file from backend
                var response = await _httpClient.GetAsync($"api/audio/play?word={wordName}");

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error fetching audio: {response.StatusCode}");
                    return;
                }

                // 🔹 Stream audio directly from the server
                var responseStream = await response.Content.ReadAsStreamAsync();
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
        public async Task<bool> CheckIfAudioFileExistsAsync(int phraseId)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/audio/checkPhraseAudio/{phraseId}");
                if (response.IsSuccessStatusCode)
                {
                    var content = await response.Content.ReadAsStringAsync();
                    bool audioExists = JsonConvert.DeserializeObject<bool>(content);
                    return audioExists;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error checking audio file: {ex.Message}");
            }
            return false;
        }


        public async Task PlayPhraseAudioAsync(int phraseId)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/audio/phrases/{phraseId}");
                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error: Received status code {response.StatusCode}");
                    return;
                }
                var responseStream = await response.Content.ReadAsStreamAsync();

                if (responseStream == null || responseStream.Length == 0)
                {
                    Console.WriteLine("Error: Audio stream is null or empty.");
                    return;
                }

                Console.WriteLine($"Audio stream successfully retrieved with length: {responseStream.Length} bytes.");

                var player = _audioManager.CreatePlayer(responseStream);

                if (player == null)
                {
                    Console.WriteLine("Error: Unable to create player.");
                    return;
                }
                player.Play();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error playing phrase audio: {ex.Message}");
            }
        }
    }
}
