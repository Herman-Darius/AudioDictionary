using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryApp.Services
{
    public class FileUploadService
    {
        private readonly HttpClient _httpClient;

        public FileUploadService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public async Task<FileResult> SelectFileAsync()
        {
            try
            {
                var fileTypes = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>>
        {
            { DevicePlatform.iOS, new[] { "com.microsoft.excel.xlsx", "com.microsoft.excel.xls" } }, // iOS UTI
            { DevicePlatform.Android, new[] { "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel" } }, // Android MIME types
            { DevicePlatform.WinUI, new[] { ".xlsx", ".xls" } }, // Windows extensions
            { DevicePlatform.MacCatalyst, new[] { "com.microsoft.excel.xlsx", "com.microsoft.excel.xls" } } // Mac
        });

                var fileResult = await FilePicker.Default.PickAsync(new PickOptions
                {
                    FileTypes = fileTypes,
                    PickerTitle = "Select an Excel file"
                });

                return fileResult;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error selecting file: {ex.Message}");
                return null;
            }
        }


        public async Task<string> UploadFileAsync(FileResult file)
        {
            if (file == null)
                return "No file selected.";

            try
            {
                using (var stream = await file.OpenReadAsync())
                {
                    using (var content = new MultipartFormDataContent())
                    {
                        var fileContent = new StreamContent(stream);
                        fileContent.Headers.ContentType = new MediaTypeHeaderValue("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                        content.Add(fileContent, "file", file.FileName);

                        var response = await _httpClient.PostAsync("http://localhost:8080/api/excel/upload", content);

                        return response.IsSuccessStatusCode ? "File uploaded successfully!" : $"File upload failed: {response.ReasonPhrase}";
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
