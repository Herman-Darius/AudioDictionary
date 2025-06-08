using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DictionaryManagementApp.Resources.Converters
{
    public class AlternationConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is int idx)
                return idx % 2 == 0
                  ? Color.FromHex("#FAF8F2") // your original “even” color
                  : Color.FromHex("#E8E4DD"); // a second “odd” color
            return Color.FromHex("#fadsfa");
        }


        public object? ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}
