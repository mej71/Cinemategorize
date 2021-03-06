package application;


import application.controllers.AddMediaDialogController;
import application.controllers.CinemaController;
import application.controls.JFXMediaRippler;
import application.controls.JFXPersonRippler;
import application.mediainfo.MediaItem;
import application.mediainfo.MediaResultsPage;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.LinkedHashMap;

//Hold all controllers statically.  Eases communication between dialogs without excess methods
public class ControllerMaster {

	public static CinemaController mainController;
	public static UserData userData;
	public static ManualLookupController manualController;
	private static SelectionViewController selectionViewController;
	private static AddMediaDialogController addMediaDialogController;
	private static SettingsController settingsController;
	private static PlaylistManagerController playlistController;
	public static TileAnimator tileAnimator = new TileAnimator();

	private static JFXDialog selectionViewWindow;
	private static JFXDialog manualLookupWindow;
	private static JFXDialog addMediaWindow;
	private static JFXDialog settingsWindow;
	private static JFXDialog playlistWindow;

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
		tryLoadFile();
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

	public static void tryLoadFile() {
		InputStream inputStream;
		ObjectInputStream objectInputStream;
		new File("save_data").mkdirs();
		File file = new File("save_data/userdata.dat");
		URL url;
		try {
			url = file.toURI().toURL();
			if (file.exists()) {
				inputStream = url.openStream();
				objectInputStream = new ObjectInputStream(inputStream);
				UserData tempDat = (UserData)objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				ControllerMaster.userData = tempDat;
				return;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		ControllerMaster.userData = new UserData();
	}

	public static void closeAddMediaWindow() {
		addMediaWindow.close();
	}
}
