using CommunityToolkit.Maui.Views;
namespace DictionaryApp.Views;

public partial class InfoPopup : Popup
{
	public InfoPopup()
	{
		InitializeComponent();
        var display = DeviceDisplay.Current.MainDisplayInfo;

        double screenWidthDp = display.Width / display.Density;
        double screenHeightDp = display.Height / display.Density;

        this.Size = new Size(
            screenWidthDp * 0.9,
            screenHeightDp * 0.85
        );
    }
    private void OnCloseClicked(object sender, EventArgs e)
    {
        this.Close();
    }
}