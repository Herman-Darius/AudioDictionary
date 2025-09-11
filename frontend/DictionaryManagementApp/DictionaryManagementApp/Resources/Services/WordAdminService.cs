using DictionaryManagementApp.Resources.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;


namespace DictionaryManagementApp.Resources.Services
{
    public class WordAdminService
    {
        private readonly IHttpClientFactory _httpClientFactory;

        public WordAdminService(IHttpClientFactory httpClientFactory)
        {
            _httpClientFactory = httpClientFactory;
        }

        private HttpClient _httpClient => _httpClientFactory.CreateClient("custom-httpclient");

        public async Task<bool> AddWordAsync(AddWordRequest request)
        {
            var response = await _httpClient.PostAsJsonAsync("api/words/add", request);
            return response.IsSuccessStatusCode;
        }




        public async Task<bool> DeleteWordAsync(int wordId)
        {
            var response = await _httpClient.DeleteAsync($"api/words/{wordId}");
            return response.IsSuccessStatusCode;
        }

        public async Task<bool> UpdateWordAsync(Word word)
        {
            var response = await _httpClient.PutAsJsonAsync("api/words/update", word);
            return response.IsSuccessStatusCode;
        }


        public async Task<List<Word>> GetAllWordsAsync()
        {
            try
            {
                return await _httpClient.GetFromJsonAsync<List<Word>>("api/words/all");
            }
            catch (IOException)
            {
                await Task.Delay(50);
                return await _httpClient.GetFromJsonAsync<List<Word>>("api/words/all");
            }
        }

        public async Task<List<Word>> SearchWordsAsync(string query)
        {
            var esc = Uri.EscapeDataString(query);
            var response = await _httpClient.GetAsync($"api/words/search?query={esc}");
            if (!response.IsSuccessStatusCode)
                return new List<Word>();

            var json = await response.Content.ReadAsStringAsync();

            try
            {
                using var doc = JsonDocument.Parse(json);
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    return JsonSerializer.Deserialize<List<Word>>(json, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    })!;
                }
                else
                {
                    return new List<Word>();
                }
            }
            catch (JsonException)
            {
                return new List<Word>();
            }
        }
        public async Task<Word> GetWordByIdAsync(int id)
        {
            var resp = await _httpClient.GetAsync($"api/words/{id}");
            resp.EnsureSuccessStatusCode();
            return await resp.Content.ReadFromJsonAsync<Word>();
        }

        public async Task<List<Phrase>> GetPhrasesByWordIdAsync(int wordId)
        {
            var resp = await _httpClient.GetAsync($"api/phrases/by-word/{wordId}");
            resp.EnsureSuccessStatusCode();
            return await resp.Content.ReadFromJsonAsync<List<Phrase>>();
        }

        public async Task<bool> UpdateWordWithPhrasesAsync(
            UpdateWordRequest wordDto,
            List<PhraseDto> phraseDtos)
        {
            var payload = new
            {
                word = wordDto,
                phrases = phraseDtos
            };

            var response = await _httpClient
                .PutAsJsonAsync("api/words/update-with-phrases", payload);

            return response.IsSuccessStatusCode;
        }
        public async Task<bool> DeletePhraseAsync(int phraseId)
        {
            var resp = await _httpClient.DeleteAsync($"api/phrases/{phraseId}");
            return resp.IsSuccessStatusCode;
        }
        public async Task<string?> UploadWordAudioAsync(int wordId, FileResult file)
        {
            using var stream = await file.OpenReadAsync();
            using var content = new MultipartFormDataContent();
            var fileContent = new StreamContent(stream);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("audio/mpeg");
            content.Add(fileContent, "file", file.FileName);

            var resp = await _httpClient.PostAsync($"api/upload/word-audio/{wordId}", content);
            if (!resp.IsSuccessStatusCode) return null;
            return await resp.Content.ReadAsStringAsync();
        }

        public async Task<string?> UploadWordImageAsync(int wordId, FileResult file)
        {
            using var stream = await file.OpenReadAsync();
            using var content = new MultipartFormDataContent();
            var fileContent = new StreamContent(stream);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
            content.Add(fileContent, "file", file.FileName);

            var resp = await _httpClient.PostAsync($"api/upload/word-image/{wordId}", content);
            if (!resp.IsSuccessStatusCode) return null;
            return await resp.Content.ReadAsStringAsync();
        }

        public async Task<string?> UploadPhraseAudioAsync(int phraseId, FileResult file)
        {
            using var stream = await file.OpenReadAsync();
            using var content = new MultipartFormDataContent();
            var fileContent = new StreamContent(stream);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("audio/mpeg");
            content.Add(fileContent, "file", file.FileName);

            var resp = await _httpClient.PostAsync($"api/upload/phrase-audio/{phraseId}", content);
            if (!resp.IsSuccessStatusCode) return null;
            return await resp.Content.ReadAsStringAsync();
        }
        public async Task<bool> AddWordWithPhrasesAsync(AddWordWithPhrasesDTO dto)
        {
            
            var resp = await _httpClient.PutAsJsonAsync("api/words/add-with-phrases", dto);
            return resp.IsSuccessStatusCode;
        }
    }
}
