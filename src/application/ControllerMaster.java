package application;


//Hold all controllers statically.  Eases communication between dialogs without excess methods
public class ControllerMaster {

	public static CinemaController mainController;
	
	public static UserData userData = new UserData();
	
	public static ManualLookupController manualController;
	
	public static SelectionViewController selectionViewController;
	
	public static AddMediaDialogController addMediaDialogController;
	
	public static SettingsController settingsController;

	static PlaylistManagerController playlistController;

	static TileAnimator tileAnimator = new TileAnimator();
}
