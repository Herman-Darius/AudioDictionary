using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using CommunityToolkit.Maui.Storage;

namespace DictionaryManagementApp.Resources.Models
{
    public class PhraseEditor : INotifyPropertyChanged
    {
        public int? Id { get; set; }

        string content;
        public string Content
        {
            get => content;
            set { content = value; OnPropertyChanged(); }
        }

        string definition;
        public string Definition
        {
            get => definition;
            set { definition = value; OnPropertyChanged(); }
        }

        // only audio now
        string audioFileName;
        public string AudioFileName
        {
            get => audioFileName;
            set { audioFileName = value; OnPropertyChanged(); }
        }

        public ICommand PickAudioCommand { get; }

        public PhraseEditor()
        {
            PickAudioCommand = new Command(async () =>
            {
                var types = new FilePickerFileType(new Dictionary<DevicePlatform, IEnumerable<string>> {
                { DevicePlatform.iOS,     new[]{ "public.audio" } },
                { DevicePlatform.Android, new[]{ "audio/*"     } },
                { DevicePlatform.WinUI,   new[]{ ".mp3", ".wav", ".m4a" } }
            });
                var picked = await FilePicker.PickAsync(new PickOptions
                {
                    PickerTitle = "Selectați fișier audio",
                    FileTypes = types
                });
                if (picked != null)
                    AudioFileName = picked.FileName;
            });
        }

        public event PropertyChangedEventHandler PropertyChanged;
        void OnPropertyChanged([CallerMemberName] string p = null)
            => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));
    }

}
