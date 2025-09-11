using DictionaryApp.Converters;
using Newtonsoft.Json;
using Plugin.Maui.Audio;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Maui.Controls;

namespace DictionaryApp.Services
{
    public class AudioService
    {
        private readonly HttpClient _httpClient;
        private readonly IAudioManager _audioManager;
        private IAudioPlayer _currentPlayer;
        private MemoryStream _currentStream;

        private DateTime _lastPlay = DateTime.MinValue;
        private static readonly TimeSpan _cooldown = TimeSpan.FromSeconds(2);

        public AudioService(IHttpClientFactory httpClientFactory, IAudioManager audioManager)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
            _audioManager = audioManager;
        }
        public async Task PlayWordAudioAsync(string wordName)
        {
            if (_currentPlayer != null)
            {
                await MainThread.InvokeOnMainThreadAsync(() =>
                    Application.Current.MainPage.DisplayAlert(
                        /*"Please wait",
                        "Audio is already playing.",
                        "OK"));*/
                        "Te rog așteaptă",
                        "Fișierul audio este în proces de redare.",
                        "OK"));
                return;
            }
            CleanupCurrent();

            var resp = await _httpClient.GetAsync(
                $"api/audio/play?word={Uri.EscapeDataString(wordName)}",
                HttpCompletionOption.ResponseHeadersRead);

            if (resp.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                await MainThread.InvokeOnMainThreadAsync(() =>
                    Application.Current.MainPage.DisplayAlert(
                        /*"No audio",
                        "No audio available for this phrase.",
                        "OK"));*/
                        "Eroare",
                        "Nu există un fișier audio!",
                        "OK"));
                return;
            }

            var netStream = await resp.Content.ReadAsStreamAsync();
            if (netStream == null)
                return;

            _currentStream = new MemoryStream();
            await netStream.CopyToAsync(_currentStream);
            _currentStream.Position = 0;

            _currentPlayer = _audioManager.CreatePlayer(_currentStream);
            _currentPlayer.PlaybackEnded += OnPlaybackEnded;
            _currentPlayer.Play();
        }

        public async Task PlayPhraseAudioAsync(int phraseId)
        {
            if (_currentPlayer != null)
            {
                await MainThread.InvokeOnMainThreadAsync(() =>
                    Application.Current.MainPage.DisplayAlert(
                        /*"Please wait",
                        "Audio is already playing.",
                        "OK"));*/
                        "Te rog așteaptă",
                        "Fișierul audio este în proces de redare.",
                        "OK"));

                return;
            }

            CleanupCurrent();

            var resp = await _httpClient.GetAsync(
                $"api/audio/phrases/{phraseId}",
                HttpCompletionOption.ResponseHeadersRead);
            if (resp.StatusCode == System.Net.HttpStatusCode.NotFound)
            {
                await MainThread.InvokeOnMainThreadAsync(() =>
                    Application.Current.MainPage.DisplayAlert(
                        /*"No audio",
                        "No audio available for this phrase.",
                        "OK"));*/
                        "Eroare",
                        "Nu există un fișier audio!",
                        "OK"));
                return;
            }

            resp.EnsureSuccessStatusCode();

            var netStream = await resp.Content.ReadAsStreamAsync();
            _currentStream = new MemoryStream();
            await netStream.CopyToAsync(_currentStream);
            _currentStream.Position = 0;

            _currentPlayer = _audioManager.CreatePlayer(_currentStream);
            _currentPlayer.PlaybackEnded += OnPlaybackEnded;
            _currentPlayer.Play();
        }

        private void OnPlaybackEnded(object sender, EventArgs e)
        {
            if (_currentPlayer != null)
            {
                _currentPlayer.PlaybackEnded -= OnPlaybackEnded;
                _currentPlayer.Dispose();
            }
            _currentPlayer = null;

            _currentStream?.Dispose();
            _currentStream = null;
        }

        private void CleanupCurrent()
        {
            if (_currentPlayer != null)
            {
                _currentPlayer.Stop();
                _currentPlayer.Dispose();
                _currentPlayer = null;
            }
            if (_currentStream != null)
            {
                _currentStream.Dispose();
                _currentStream = null;
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



    }

}
