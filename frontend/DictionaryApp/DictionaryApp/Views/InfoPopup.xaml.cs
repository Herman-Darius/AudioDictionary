using CommunityToolkit.Maui.Views;
namespace DictionaryApp.Views;

public partial class InfoPopup : Popup
{
	public InfoPopup()
	{
		InitializeComponent();

        // get display metrics
        var display = DeviceDisplay.Current.MainDisplayInfo;
        // convert to device‐independent units (dp)
        double screenWidthDp = display.Width / display.Density;
        double screenHeightDp = display.Height / display.Density;

        // set popup to 90% width, 85% height
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