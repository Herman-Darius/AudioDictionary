using DictionaryManagementApp.Resources.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
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


        public async Task<List<Word>> SearchWordsAsync(string query)
        {
            var response = await _httpClient.GetAsync($"api/words/search?query={query}");

            if (!response.IsSuccessStatusCode) return new();

            return await response.Content.ReadFromJsonAsync<List<Word>>() ?? new();
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
    }
}
