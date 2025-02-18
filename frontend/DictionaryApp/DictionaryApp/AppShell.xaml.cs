using Microsoft.Win32;
using System.Collections;

namespace DictionaryApp
{
    public partial class AppShell : Shell
    {
        public AppShell()
        {
            InitializeComponent();
            // Register routes for navigation
            //Routing.RegisterRoute(nameof(DictionaryPage), typeof(DictionaryPage));
        }
    }
}
