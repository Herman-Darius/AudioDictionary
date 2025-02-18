using DictionaryApp.Views;
using System.Collections;

namespace DictionaryApp
{
    public partial class MainPage : ContentPage
    {
        int count = 0;

        public MainPage()
        {
            InitializeComponent();
        }

        private async void OnNavigateToDictionary(object sender, EventArgs e)
        {
            await Navigation.PushAsync(new UploadPage()); // Navigate to DictionaryPage
        }
    }

}
