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

                // Log the response content for debugging
                var jsonResponse = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"SearchWords Response: {jsonResponse}");

                return JsonConvert.DeserializeObject<List<Word>>(jsonResponse) ?? new List<Word>();
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

                // Log the response content for debugging
                var jsonResponse = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"GetWordsByLetter Response: {jsonResponse}");

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
            if (string.IsNullOrEmpty(wordName))
                return null;

            try
            {
                var response = await _httpClient.GetStringAsync($"api/words/searchByName?wordName={wordName}");

                // Deserialize the response into a Word object
                return JsonConvert.DeserializeObject<Word>(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching word by name: {ex.Message}");
                return null;
            }
        }





        //--------------> Asta o las pentru mai tarziu <--------------

        /*public async Task<List<Word>> SearchWordsAsync(string query)
        {
            var response = await _httpClient.GetStringAsync($"api/words/search?query={query}");
            return JsonConvert.DeserializeObject<List<Word>>(response) ?? new List<Word>();
        }

        public async Task<List<Word>> GetWordsByLetterAsync(string letter)
        {
            var response = await _httpClient.GetStringAsync($"api/words/letter/{letter}");
            return JsonConvert.DeserializeObject<List<Word>>(response) ?? new List<Word>();
        }

        public async Task<List<Word>> GetAllWordsAsync()
        {
            var response = await _httpClient.GetStringAsync("api/words");
            return JsonConvert.DeserializeObject<List<Word>>(response) ?? new List<Word>();
        }*/

    }
}
