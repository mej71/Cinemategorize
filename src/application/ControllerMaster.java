package application;


import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;

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

	public static JFXDialog selectionViewWindow;
	public static JFXDialog manualLookupWindow;
	public static JFXDialog addMediaWindow;
	public static JFXDialog settingsWindow;
	static JFXDialog playlistWindow;

	public static void init(StackPane stackPane) {
		//set up dialogs
		try {
			//Selection View
			FXMLLoader loader = new FXMLLoader(ControllerMaster.class.getClassLoader().getResource("SelectionViewContent.fxml"));
			GridPane selectionView = loader.load();
			selectionViewController = loader.getController();
			selectionViewWindow = new JFXDialog(stackPane, selectionView,
					JFXDialog.DialogTransition.CENTER);
			selectionView.prefWidthProperty().bind(stackPane.widthProperty().divide(1.15));
			selectionView.prefHeightProperty().bind(stackPane.heightProperty().divide(1.3));
			//Manual Lookup
			loader = new FXMLLoader(ControllerMaster.class.getClassLoader().getResource("ManualLookupContent.fxml"));
			GridPane manualLookupView = loader.load();
			manualController = loader.getController();
			manualLookupWindow = new JFXDialog(stackPane, manualLookupView,
					JFXDialog.DialogTransition.TOP);
			manualLookupView.prefWidthProperty().bind(stackPane.widthProperty().divide(1.15));
			manualLookupView.prefHeightProperty().bind(stackPane.heightProperty().divide(1.15));
			//Add Media
			loader = new FXMLLoader(ControllerMaster.class.getClassLoader().getResource("AddMediaDialogContent.fxml"));
			JFXDialogLayout addMediaDialogView = loader.load();
			addMediaDialogController = loader.getController();
			addMediaWindow = new JFXDialog(stackPane, addMediaDialogView,
					JFXDialog.DialogTransition.TOP);
			//Settings
			loader = new FXMLLoader(ControllerMaster.class.getClassLoader().getResource("SettingsContent.fxml"));
			GridPane settingsView = loader.load();
			settingsController = loader.getController();
			settingsWindow = new JFXDialog(stackPane, settingsView,
					JFXDialog.DialogTransition.TOP);
			settingsView.prefWidthProperty().bind(stackPane.widthProperty().multiply(0.25));
			settingsView.prefHeightProperty().bind(stackPane.heightProperty().multiply(0.30));
			//Playlist Manager
			loader = new FXMLLoader(ControllerMaster.class.getClassLoader().getResource("PlaylistManagerContent.fxml"));
			GridPane playlistView = loader.load();
			playlistController = loader.getController();
			playlistWindow = new JFXDialog(stackPane, playlistView,
					JFXDialog.DialogTransition.TOP);
			playlistView.prefWidthProperty().bind(stackPane.widthProperty().multiply(0.70));
			playlistView.prefHeightProperty().bind(stackPane.heightProperty().multiply(0.85));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void showPlaylistDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.playlistController.show(playlistWindow);
	}

	public static void closeDialogs() {
		selectionViewWindow.close();
		addMediaWindow.close();
		manualLookupWindow.close();
		JFXPersonRippler.closeWindow();
		JFXMediaRippler.forceHidePopOver();
	}

	public static void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList) {
		showManualLookupDialog(mediaList, 0, 0, 0);
	}

	public static void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList, int mId, int seasonNum, int epNum) {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.manualController.setData(mediaList);
		ControllerMaster.manualController.openDialog(manualLookupWindow, mId, seasonNum, epNum);
	}

	public static void showAddMediaDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.addMediaDialogController.openDialogMenu(addMediaWindow, false);
	}

	public static void showSelectionDialog(MediaItem mi) {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.selectionViewController.showMediaItem(selectionViewWindow, mi);
	}

	public static void showSettingsDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.settingsController.show(settingsWindow);
	}
}
