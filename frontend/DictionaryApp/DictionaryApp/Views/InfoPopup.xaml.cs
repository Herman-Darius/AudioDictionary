using CommunityToolkit.Maui.Views;
namespace DictionaryApp.Views;

public partial class InfoPopup : Popup
{
	public InfoPopup()
	{
		InitializeComponent();
	}
    private void OnCloseClicked(object sender, EventArgs e)
    {
        this.Close();
    }
}