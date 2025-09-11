using DictionaryApp.Models;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Services
{
    public class WordService
    {
        private readonly HttpClient _httpClient;

        public WordService(IHttpClientFactory httpClientFactory)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
        }

        public async Task<List<Word>> SearchWordsAsync(string query)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/words/search?query={query}");

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error: {response.StatusCode}");
                    return new List<Word>();
                }

                var jsonResponse = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<List<Word>>(jsonResponse) ?? new List<Word>();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching words: {ex.Message}");
                return new List<Word>();
            }
        }

        public async Task<Word> GetWordByNameAsync(string wordName)
        {
            try
            {
                var response = await _httpClient.GetStringAsync($"api/words/name/{wordName}");
                return JsonConvert.DeserializeObject<Word>(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching word details: {ex.Message}");
                return null;
            }
        }

        public async Task<List<Word>> GetAllWordsAsync()
        {
            try
            {
                var response = await _httpClient.GetStringAsync("api/words/all");
                return JsonConvert.DeserializeObject<List<Word>>(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching words: {ex.Message}");
                return new List<Word>();
            }
        }

        public async Task<List<Word>> GetWordsByLetterAsync(string letter)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/words/letter/{letter}");

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Error: {response.StatusCode}");
                    return new List<Word>();
                }

                var jsonResponse = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<List<Word>>(jsonResponse) ?? new List<Word>();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching words: {ex.Message}");
                return new List<Word>();
            }
        }

        public async Task<ImageSource> GetWordImageAsync(string imageFileName, string defaultImage = "default_image.png")
        {
            if (string.IsNullOrWhiteSpace(imageFileName))
                return defaultImage;

            try
            {
                var uri = new Uri(_httpClient.BaseAddress, $"api/media/images/{imageFileName}");
                var response = await _httpClient.SendAsync(new HttpRequestMessage(HttpMethod.Head, uri));
                if (!response.IsSuccessStatusCode)
                    return defaultImage;
                var source = new UriImageSource
                {
                    Uri = uri,
                    CachingEnabled = false,
                    CacheValidity = TimeSpan.Zero
                };

                return source;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[Image Fetch Error] {ex.Message}");
                return defaultImage;
            }
        }



        /*
        public async Task<List<RootResult>> SearchRootsAsync(string query)
        {
            var response = await _httpClient.GetAsync($"api/roots/search-root-by-word?query={query}");

            if (response.IsSuccessStatusCode)
            {
                var json = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<List<RootResult>>(json);
            }

            return new List<RootResult>();
        }

        public async Task<WordRoot?> GetRootByNameAsync(string name)
        {
            var response = await _httpClient.GetAsync($"api/roots/name/{name}");
            if (response.IsSuccessStatusCode)
            {
                var json = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<WordRoot>(json);
            }

            return null;
        }
        public async Task<WordRoot?> GetRootByWordAsync(string wordName)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/words/by-word?wordName={wordName}");

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Failed to fetch root for word: {wordName}, Status: {response.StatusCode}");
                    return null;
                }

                var json = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<WordRoot>(json);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Exception in GetRootByWordAsync: {ex.Message}");
                return null;
            }
        }
        */
    }

}
