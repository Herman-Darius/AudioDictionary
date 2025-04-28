using DictionaryManagementApp.Resources.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Services
{
    public class WordAdminService
    {
        private readonly HttpClient _httpClient;

        public WordAdminService(IHttpClientFactory httpClientFactory)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
        }

        public async Task<bool> AddWordAsync(AddWordRequest request)
        {
            var response = await _httpClient.PostAsJsonAsync("api/words/add", request);
            return response.IsSuccessStatusCode;
        }




        public async Task<bool> DeleteWordAsync(int wordId)
        {
            var response = await _httpClient.DeleteAsync($"api/words/delete/{wordId}");
            return response.IsSuccessStatusCode;
        }

        public async Task<bool> UpdateWordAsync(Word word)
        {
            var response = await _httpClient.PutAsJsonAsync("api/words/update", word);
            return response.IsSuccessStatusCode;
        }


        /// <summary>Fetches absolutely everything once.</summary>
        public async Task<List<Word>> GetAllWordsAsync()
        {
            var response = await _httpClient.GetAsync("api/words/all");
            if (!response.IsSuccessStatusCode) return new List<Word>();
            return await response.Content.ReadFromJsonAsync<List<Word>>()
                   ?? new List<Word>();
        }

        /// <summary>Searches only when query is non-empty; otherwise returns empty.</summary>
        public async Task<List<Word>> SearchWordsAsync(string query)
        {
            var esc = Uri.EscapeDataString(query);
            var response = await _httpClient.GetAsync($"api/words/search?query={esc}");
            if (!response.IsSuccessStatusCode)
                return new List<Word>();

            // Read it as a string so we can inspect it
            var json = await response.Content.ReadAsStringAsync();

            try
            {
                using var doc = JsonDocument.Parse(json);
                if (doc.RootElement.ValueKind == JsonValueKind.Array)
                {
                    // It's the expected array of Word
                    return JsonSerializer.Deserialize<List<Word>>(json, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    })!;
                }
                else
                {
                    // It's an object (e.g. { "message": "No words found…" })
                    return new List<Word>();
                }
            }
            catch (JsonException)
            {
                // In case of any weird payload, just return empty
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

        public async Task<bool> UpdateWordWithPhrasesAsync(UpdateWordRequest wordReq, List<PhraseDto> phrases)
        {
            var payload = new { word = wordReq, phrases };
            var resp = await _httpClient.PutAsJsonAsync("api/words/update-with-phrases", payload);
            return resp.IsSuccessStatusCode;
        }
    }
}
