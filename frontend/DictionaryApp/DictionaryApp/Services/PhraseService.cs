using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DictionaryApp.Models;

namespace DictionaryApp.Services
{
    public class PhraseService
    {
        private readonly HttpClient _httpClient;

        public PhraseService(IHttpClientFactory httpClientFactory)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
        }
        public async Task<List<Phrase>> GetPhrasesByWordIdAsync(long wordId)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/phrases/by-word/{wordId}");
                response.EnsureSuccessStatusCode();

                var json = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<List<Phrase>>(json) ?? new List<Phrase>();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching phrases for wordId {wordId}: {ex.Message}");
                return new List<Phrase>();
            }
        }
        public async Task<List<Phrase>> GetPhrasesForWordAsync(int wordId)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/phrases/by-word/{wordId}");

                if (!response.IsSuccessStatusCode)
                {
                    Console.WriteLine($"Failed to get phrases for word {wordId}: {response.StatusCode}");
                    return new List<Phrase>();
                }

                var json = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<List<Phrase>>(json) ?? new List<Phrase>();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching phrases for word {wordId}: {ex.Message}");
                return new List<Phrase>();
            }
        }
        


        /*
         * CODUL VECHI PENTRU HYPERLINKS
         * 
        public async Task<(List<Phrase> DirectPhrases, List<Phrase> RelatedPhrases)> GetPhrasesAsync(int wordId)
        {
            try
            {
                var response = await _httpClient.GetAsync($"api/words/{wordId}/phrases");

                if (!response.IsSuccessStatusCode)
                {
                    // Log the response status if it's unsuccessful
                    Console.WriteLine($"Error fetching phrases: {response.StatusCode}");
                    return (new List<Phrase>(), new List<Phrase>());
                }

                var jsonResponse = await response.Content.ReadAsStringAsync();
                Console.WriteLine($"GetPhrases Response: {jsonResponse}");

                // Attempt deserialization of the JSON response
                try
                {
                    var phrasesResponse = JsonConvert.DeserializeObject<PhraseResponse>(jsonResponse);

                    var directPhrases = phrasesResponse?.directPhrases ?? new List<Phrase>();
                    var relatedPhrases = phrasesResponse?.relatedPhrases ?? new List<Phrase>();

                    return (directPhrases, relatedPhrases);
                }
                catch (JsonException jsonEx)
                {
                    // Handle JSON parsing errors
                    Console.WriteLine($"Error parsing phrases JSON: {jsonEx.Message}");
                    return (new List<Phrase>(), new List<Phrase>());
                }
            }
            catch (HttpRequestException httpEx)
            {
                // Handle network-related exceptions (e.g., no internet connection)
                Console.WriteLine($"Network error while fetching phrases: {httpEx.Message}");
                return (new List<Phrase>(), new List<Phrase>());
            }
            catch (Exception ex)
            {
                // Catch all other exceptions
                Console.WriteLine($"Unexpected error: {ex.Message}");
                return (new List<Phrase>(), new List<Phrase>());
            }
        }

        public async Task<string> ProcessPhrasesWithHyperlinks(string phrase)
        {
            try
            {
                var response = await _httpClient.GetAsync("api/words");

                if (!response.IsSuccessStatusCode)
                {
                    // Handle failed words retrieval
                    Console.WriteLine($"Error fetching words: {response.StatusCode}");
                    return phrase;
                }

                var wordsJson = await response.Content.ReadAsStringAsync();
                var wordList = JsonConvert.DeserializeObject<List<Word>>(wordsJson) ?? new List<Word>();

                foreach (var word in wordList)
                {
                    if (phrase.Contains(word.wordName, StringComparison.OrdinalIgnoreCase))
                    {
                        phrase = phrase.Replace(word.wordName,
                            $"<a href='/word/{word.id}'>{word.wordName}</a>",
                            StringComparison.OrdinalIgnoreCase);
                    }
                }

                return phrase;
            }
            catch (HttpRequestException httpEx)
            {
                // Handle network-related errors in hyperlink processing
                Console.WriteLine($"Network error while processing hyperlinks: {httpEx.Message}");
                return phrase;
            }
            catch (JsonException jsonEx)
            {
                // Handle errors while deserializing word list
                Console.WriteLine($"Error parsing words JSON: {jsonEx.Message}");
                return phrase;
            }
            catch (Exception ex)
            {
                // Catch all other exceptions
                Console.WriteLine($"Unexpected error while processing hyperlinks: {ex.Message}");
                return phrase;
            }
        }

        public async Task<List<Phrase>> GetPhrasesByRootIdAsync(int rootId)
        {
            var response = await _httpClient.GetAsync($"api/phrases/{rootId}/phrases");
            if (!response.IsSuccessStatusCode) return new List<Phrase>();

            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<Phrase>>(json) ?? new List<Phrase>();
        }
        public async Task<List<Phrase>> GetRelatedPhrasesByRootIdAsync(int rootId)
        {
            var response = await _httpClient.GetAsync($"api/phrases/{rootId}/related-phrases");
            if (!response.IsSuccessStatusCode) return new List<Phrase>();

            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<Phrase>>(json) ?? new List<Phrase>();
        }

        public async Task<List<Phrase>> GetPhrasesWithLinksByRootIdAsync(int rootId)
        {
            var response = await _httpClient.GetAsync($"api/phrases/{rootId}/phrases-with-links");
            if (!response.IsSuccessStatusCode) return new List<Phrase>();

            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<Phrase>>(json) ?? new List<Phrase>();
        }
        *
        *
        */
        

    }

}
