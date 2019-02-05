package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jfoenix.controls.*;

import info.movito.themoviedbapi.model.Collection;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class CinemaController implements Initializable {

	// local content
	@FXML StackPane backgroundStackPane;
	@FXML StackPane stackPane;
	@FXML GridPane mainGrid;	
	@FXML JFXComboBox<SortTypes> sortCombo;
	@FXML JFXTextField searchField;
	@FXML JFXSlider scaleSlider;
	@FXML private Label scaleLabel;
	@FXML private JFXButton textClearButton;
	@FXML private JFXButton startDateClearButton;
	@FXML private JFXButton endDateClearButton;
	@FXML private JFXButton playlistClearButton;
	@FXML private JFXButton collectionsClearButton;

	@FXML JFXComboBox<Integer> startYearComboBox;
	@FXML JFXComboBox<Integer> endYearComboBox;
	@FXML JFXComboBox<MediaPlaylist> playlistCombo;
	@FXML JFXComboBox<Collection> collectionsCombo;
	@FXML JFXComboBox<MediaListDisplayType> mediaTypeCombo;
	@FXML private JFXRippler optionsRippler;

	private Window window;
	private JFXListView optionList;

	public TilePane tilePane;	
	public MovieScrollPane scrollPane;
	// other variables
	public final String[] supportedFileTypes = { "*.mp4", "*.avi", "*.wmv", "*.flv", "*.mov", "*.mkv" };
	public List<JFXMediaRippler> allTiles = new ArrayList<>();
	public LinkedHashMap<String, MediaItem> showingMedia = new LinkedHashMap<>();
	public MovieAutoCompletePopup autoCompletePopup;
	public MovieAutoCompleteEvent<SearchItem> autoEvent;
	public JFXDialog selectionViewWindow;
	public JFXDialog manualLookupWindow;	
	public JFXDialog addMediaWindow;
	public JFXDialog settingsWindow;
	JFXDialog playlistWindow;
	public Scene cinemaScene;
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		ControllerMaster.mainController = this;
		
		mainGrid.prefWidthProperty().bind(backgroundStackPane.widthProperty());
		mainGrid.prefHeightProperty().bind(backgroundStackPane.heightProperty());
		
		mediaTypeCombo.getItems().clear();
		mediaTypeCombo.setItems(FXCollections.observableArrayList( MediaListDisplayType.values()));
		mediaTypeCombo.setValue(MediaListDisplayType.ALL);
		mediaTypeCombo.valueProperty().addListener((ov, oldVal, newVal) -> refreshSearch());
		
		sortCombo.getItems().clear();
		sortCombo.setItems(FXCollections.observableArrayList( SortTypes.values()));
		sortCombo.setValue(SortTypes.NAME_ASC);
		sortCombo.valueProperty().addListener((arg0, arg1, arg2) -> ControllerMaster.userData.sortShownItems());
		
		updatePlaylistCombo();
		playlistCombo.setCellFactory(param -> new PlaylistComboCell());

		updateCollectionCombo();
		collectionsCombo.setCellFactory(param -> new CollectionComboCell());
		collectionsCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue!=null) {
				collectionsClearButton.setVisible(true);
			} else {
				collectionsClearButton.setVisible(false);
			}
			refreshSearch();
		});
		collectionsCombo.setConverter(new StringConverter<Collection>() {

			@Override
			public String toString(Collection object) {
				return object.getName();
			}

			@Override
			public Collection fromString(String string) {
				return null;
			}
			
		});

		// create tilepane and add to the scene
		tilePane = new TilePane();
		tilePane.setPadding(new Insets(5, 5, 5, 5));
		tilePane.setVgap(15);
		tilePane.setHgap(10);
		ControllerMaster.tileAnimator.observe(tilePane.getChildren());
		scaleSlider.valueProperty().addListener((ChangeListener) (source, oldValue, newValue) -> {
			ControllerMaster.userData.setScaleFactor(scaleSlider.getValue()*0.25);
			updateScale();
			scaleLabel.textProperty().setValue("Image Size: " + (Math.round(ControllerMaster.userData.getScaleFactor() * 100.0) / 100.0));
		});
		
		scrollPane = new MovieScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.setContent(tilePane);
		JFXSmoothScroll.smoothScrolling(scrollPane);
		scrollPane.getStyleClass().add("movie-scroll-pane");
		stackPane.getChildren().add(scrollPane);
		
		autoCompletePopup = new MovieAutoCompletePopup();
		autoCompletePopup.prefWidthProperty().bind(searchField.widthProperty().multiply(1.6));
		autoCompletePopup.setCellLimit(10);
	    autoCompletePopup.setMovieSelectionHandler(e -> {
	    	autoEvent = (MovieAutoCompleteEvent<SearchItem>)e;
	    	searchField.setText(autoEvent.getObject().getItemName());
	    	refreshSearch();
	    });

	    // filtering options
	    searchField.textProperty().addListener(observable -> {
	    	autoCompletePopup.getSuggestions().clear();
	    	autoCompletePopup.getSuggestions().addAll(ControllerMaster.userData.getAutoCompleteItems(searchField.getText()));
	    	textClearButton.setVisible(true);
	        if (autoCompletePopup.getFilteredSuggestions().isEmpty() || searchField.getText().isEmpty()) {
	            autoCompletePopup.hide();
	            if (searchField.getText().isEmpty()) {
	            	textClearButton.setVisible(false);
	            	if (showingMedia.size() != ControllerMaster.userData.numMediaItems()) {
		            	autoCompletePopup.getSuggestions().clear();
		            	autoEvent = null;
		            	refreshSearch();
	            	}
	            }             
	        } else {
                JFXMediaRippler.forceHidePopOver();
	            autoCompletePopup.show(searchField);
	        }
	    });
	    
	    searchField.setOnMouseClicked(arg0 -> {
			if (!autoCompletePopup.getFilteredSuggestions().isEmpty() && !searchField.getText().isEmpty()) {
                JFXMediaRippler.forceHidePopOver();
			    autoCompletePopup.show(searchField);
            }
		});
	    
	    fillYearCombos(ControllerMaster.userData.minYear, ControllerMaster.userData.maxYear);
	    //date range validation
	    startYearComboBox.valueProperty().addListener((ov, oldValue, newValue) -> {
			if (newValue!=null) {
				if (endYearComboBox.getValue() != null  && newValue > endYearComboBox.getValue()) {
					endYearComboBox.getSelectionModel().clearSelection();
				}
				startDateClearButton.setVisible(true);
            } else {
				startDateClearButton.setVisible(false);
            }
			refreshSearch();
		});

	    endYearComboBox.valueProperty().addListener((ov, oldValue, newValue) -> {
			if (newValue!=null) {
				if (startYearComboBox.getValue()!=null && newValue < startYearComboBox.getValue()) {
					startYearComboBox.getSelectionModel().clearSelection();
				}
				    endDateClearButton.setVisible(true);
                } else {
				    endDateClearButton.setVisible(false);
                }
			refreshSearch();
		});

		optionList = new JFXListView<>();
		optionList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		JFXPopup optionsPopup = new JFXPopup(optionList);
		optionsRippler.setOnMouseClicked(e -> {
			//hack to make selection clear (bug in Javafx)
			List<MainOptions.MainOptionTitles> options = FXCollections.observableArrayList(MainOptions.getOptions());
			optionList.getItems().clear();
			optionList.getItems().setAll(options);
			optionsPopup.show(optionsRippler, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT);
		});
		optionList.setOnMouseClicked( e -> {
			if (e.getTarget() != null) {
				if (e.getTarget() instanceof JFXListCell) {
					JFXListCell<MainOptions.MainOptionTitles> target = (JFXListCell) e.getTarget();
					switch (target.getItem()) {
						case ADDMOVIE:
							showAddMediaDialog();
							break;
						case MANAGEPLAYLISTS:
							showPlaylistDialog();
							break;
						case SETTINGS:
							showSettingsDialog();
							break;
						case ABOUT:
							showAboutDialog();
							break;
						default:
							break;
					}
					Platform.runLater(() ->{
						optionsPopup.hide();
					});
				}
			}
		});
	    
	    //set up dialogs
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("SelectionViewContent.fxml"));
			GridPane selectionView = loader.load();
			ControllerMaster.selectionViewController = loader.getController();
			selectionViewWindow = new JFXDialog(getBackgroundStackPane(), selectionView,
					JFXDialog.DialogTransition.CENTER);
			selectionView.prefWidthProperty().bind(backgroundStackPane.widthProperty().divide(1.15));
			selectionView.prefHeightProperty().bind(backgroundStackPane.heightProperty().divide(1.3));
			loader = new FXMLLoader(getClass().getClassLoader().getResource("ManualLookupContent.fxml"));
			GridPane manualLookupView = loader.load();
			ControllerMaster.manualController = loader.getController();			
			manualLookupWindow = new JFXDialog(getBackgroundStackPane(), manualLookupView,
					JFXDialog.DialogTransition.TOP);
			manualLookupView.prefWidthProperty().bind(backgroundStackPane.widthProperty().divide(1.15));
			manualLookupView.prefHeightProperty().bind(backgroundStackPane.heightProperty().divide(1.15));
			loader = new FXMLLoader(getClass().getClassLoader().getResource("AddMoviesDialogContent.fxml"));
			JFXDialogLayout addMediaDialogView = loader.load();
			ControllerMaster.addMediaDialogController = loader.getController();
			addMediaWindow = new JFXDialog(getBackgroundStackPane(), addMediaDialogView,
					JFXDialog.DialogTransition.TOP);
			loader = new FXMLLoader(getClass().getClassLoader().getResource("SettingsContent.fxml"));
			GridPane settingsView = loader.load();
			ControllerMaster.settingsController = loader.getController();
			settingsWindow = new JFXDialog(getBackgroundStackPane(), settingsView,
					JFXDialog.DialogTransition.TOP);
			settingsView.prefWidthProperty().bind(backgroundStackPane.widthProperty().multiply(0.25));
			settingsView.prefHeightProperty().bind(backgroundStackPane.heightProperty().multiply(0.30));
			loader = new FXMLLoader(getClass().getClassLoader().getResource("PlaylistManagerContent.fxml"));
			GridPane playlistView = loader.load();
			ControllerMaster.playlistController = loader.getController();
			playlistWindow = new JFXDialog(getBackgroundStackPane(), playlistView,
					JFXDialog.DialogTransition.TOP);
			playlistView.prefWidthProperty().bind(backgroundStackPane.widthProperty().multiply(0.70));
			playlistView.prefHeightProperty().bind(backgroundStackPane.heightProperty().multiply(0.85));
		} catch(Exception e) {
			e.printStackTrace();
		}

		createAllRipplers();
		refreshSearch();
	    scaleSlider.setValue(ControllerMaster.userData.getScaleFactor()*4);
		determinePrimaryStage();	
	}	
	
	@FXML private void clearText() {
		searchField.clear();
		searchField.requestFocus();
	}
	
	@FXML private void clearStartDate() {
		startYearComboBox.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}
	
	@FXML private void clearEndDate() {
		endYearComboBox.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}
	
	@FXML private void clearPlaylistSelection() {
		playlistCombo.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}
	
	@FXML private void clearCollectionSelection() {
		collectionsCombo.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}
	
	public void updateScale() {
		tilePane.setVgap(15*ControllerMaster.userData.getScaleFactor());
		tilePane.setHgap(10*ControllerMaster.userData.getScaleFactor());
		StackPane n;
		for (int i = 0; i < tilePane.getChildren().size(); ++i) {
			n = ((JFXMediaRippler)tilePane.getChildren().get(i)).getPane();
			n.setMaxWidth(139*ControllerMaster.userData.getScaleFactor());
			n.setMaxHeight(208*ControllerMaster.userData.getScaleFactor());
			n.resize(139*ControllerMaster.userData.getScaleFactor(), 208*ControllerMaster.userData.getScaleFactor());
		}
	}
	
	public void refreshSearch() {
		if (autoEvent != null) {
			ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs());
		} else {
			ControllerMaster.userData.refreshViewingList(new HashMap<>());
		}
	}
	
	public void fillYearCombos(int minYear, int maxYear) {
		startYearComboBox.setItems(
				FXCollections.observableArrayList(IntStream.rangeClosed(minYear,maxYear).boxed().collect(Collectors.toList()))
	    );  
	    endYearComboBox.setItems(
	    		FXCollections.observableArrayList(IntStream.rangeClosed(minYear,maxYear).boxed().collect(Collectors.toList()))
	    ); 
	}

	public void updatePlaylistCombo() {
		playlistCombo.setItems(FXCollections.observableArrayList(ControllerMaster.userData.userPlaylists));
	}
	
	public void updateCollectionCombo() {
		collectionsCombo.setItems( FXCollections.observableArrayList(ControllerMaster.userData.ownedCollections.keySet()));
	}	

	//make sure everything is loaded, then if the media list is empty force the player to add at least one item
	private void determinePrimaryStage() {
		mainGrid.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (oldScene == null && newScene != null) {
				cinemaScene = newScene;
				ThemeSelection.updateTheme(null);
				// scene is set for the first time. Now its the time to listen stage changes.
				newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
					if (oldWindow == null && newWindow != null) {
						window = newWindow;
						// if no directories to search, user must select at least one
						// opens InitialChooseDialogContent, and waits for user to add one to selection
						if (ControllerMaster.userData.numMediaItems() == 0) {
							ControllerMaster.addMediaDialogController.openDialogMenu(addMediaWindow, true);
						} 
						KeyCombination keyCombinationMac = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
				        KeyCombination keyCombinationWin = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
				        KeyCombination keyCombinationAdd = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
				        newScene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
				            if (keyCombinationMac.match(event) || keyCombinationWin.match(event)) {
				                ControllerMaster.userData.clearSaveData();
				            } else if (keyCombinationAdd.match(event)) {
				            	showAddMediaDialog();
				            }
				        });
				        backgroundStackPane.prefWidthProperty().bind(newScene.widthProperty());
				        backgroundStackPane.prefHeightProperty().bind(newScene.heightProperty());
					}
				});
			}
		});
	}
	
	public Window getMainWindow() {
		return window;
	}

	void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList) {
		showManualLookupDialog(mediaList, 0, 0, 0);
	}

	public void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList, int mId, int seasonNum, int epNum) {
        JFXMediaRippler.forceHidePopOver();
		ControllerMaster.manualController.setData(mediaList);
		ControllerMaster.manualController.openDialog(manualLookupWindow, mId, seasonNum, epNum);
	}
	
	@FXML
	public void showAddMediaDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.addMediaDialogController.openDialogMenu(addMediaWindow, false);		
	}
		
	public void showSelectionDialog(MediaItem mi) {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.selectionViewController.showMediaItem(selectionViewWindow, mi);	
	}
	
	@FXML
	public void showSettingsDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.settingsController.show(settingsWindow);
	}

	void showPlaylistDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.playlistController.show(playlistWindow);
	}
	
	@FXML
	public void showAboutDialog() {
		
	}

	public void closeDialogs() {
		selectionViewWindow.close();
		addMediaWindow.close();
		manualLookupWindow.close();
		JFXPersonRippler.closeWindow();
		JFXMediaRippler.forceHidePopOver();
	}
	
	
	//create pane for the media
	public JFXMediaRippler addMediaTile(MediaItem mediaObject) {
		JFXMediaRippler rippler = JFXMediaRippler.createBasicRippler(tilePane, scrollPane);
		rippler.setItem(mediaObject);
		return rippler;
	}
	
	public void createAllRipplers() {
		for (int i = 0; i < ControllerMaster.userData.getAllMedia().size(); ++i) {
			allTiles.add(ControllerMaster.mainController.addMediaTile(ControllerMaster.userData.getAllMedia().get(i)));
		}
	}

	public StackPane getBackgroundStackPane() {
		return backgroundStackPane;
	}

	
	
	public enum SortTypes {
		NAME_ASC("Name (Asc)"),
		NAME_DESC("Name (Desc)"),
		RELEASE_DATE_ASC("Release Date (Asc)"),
		RELEASE_DATE_DESC("Release Date (Desc)"),
		ADDED_DATE_ASC("Added Date (Asc)"),
		ADDED_DATE_DESC("Added Date (Desc)");
		
		
		private final String toString;
		
		SortTypes(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return this.toString;
		}
	}
	
}
