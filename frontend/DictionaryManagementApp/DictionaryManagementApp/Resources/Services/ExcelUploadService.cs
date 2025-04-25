using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Services
{
    public class ExcelUploadService
    {
        private readonly HttpClient _httpClient;

        public ExcelUploadService(IHttpClientFactory httpClientFactory)
        {
            _httpClient = httpClientFactory.CreateClient("custom-httpclient");
        }

        public async Task<string> UploadExcelFileAsync(FileResult file)
        {
            if (file == null)
                return "No file selected.";

            using var stream = await file.OpenReadAsync();
            using var content = new MultipartFormDataContent();

            var fileContent = new StreamContent(stream);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            content.Add(fileContent, "file", file.FileName);

            var response = await _httpClient.PostAsync("api/excel/upload", content);

            return response.IsSuccessStatusCode
                ? "File uploaded successfully!"
                : $"Upload failed: {response.ReasonPhrase}";
        }
    }
}
