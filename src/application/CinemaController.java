package application;

import com.jfoenix.controls.*;
import info.movito.themoviedbapi.model.Collection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CinemaController implements Initializable {

	// local content
	@FXML StackPane backgroundStackPane;
	@FXML StackPane stackPane;
	@FXML GridPane mainGrid;	
	@FXML
	JFXComboBox<SortTypes> sortCombo;
	@FXML
	JFXTextField searchField;
	@FXML
	JFXSlider scaleSlider;
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
	public SearchPopup autoCompletePopup;
	public SearchItem autoSelection = null;
	public Scene cinemaScene;
	public boolean tempStopSearchDelay = false;
	

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
		playlistCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue!=null) {
				playlistClearButton.setVisible(true);
			} else {
				playlistClearButton.setVisible(false);
			}
			refreshSearch();
		});

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

		autoCompletePopup = new SearchPopup();
		autoCompletePopup.prefWidthProperty().bind(searchField.widthProperty().subtract(20));

	    // filtering options
	    searchField.textProperty().addListener(observable -> {
			searchField.requestFocus();
	    	if (!tempStopSearchDelay) {
				PauseTransition pause = new PauseTransition(Duration.millis(100));
				pause.setOnFinished(event -> updateAutoComplete(searchField.getText()));
				pause.playFromStart();
			} else {
				updateAutoComplete(searchField.getText());
			}
	    });
	    
	    searchField.setOnMouseClicked(arg0 -> {
			if (!autoCompletePopup.isEmpty() && !searchField.getText().isEmpty()) {
				showAutoComplete();
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
							ControllerMaster.showAddMediaDialog();
							break;
						case MANAGEPLAYLISTS:
							ControllerMaster.showPlaylistDialog();
							break;
						case SETTINGS:
							ControllerMaster.showSettingsDialog();
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
	    
	   ControllerMaster.init(getBackgroundStackPane());

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
	
	@FXML public void clearPlaylistSelection() {
		playlistCombo.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}
	
	@FXML public void clearCollectionSelection() {
		collectionsCombo.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
	}

	private void updateAutoComplete(String oldText) {
		//don't run if text has changed since
		if (!oldText.equalsIgnoreCase(searchField.getText())) {
			return;
		}
		autoCompletePopup.setItems(ControllerMaster.userData.getAutoCompleteItems(searchField.getText()));
		textClearButton.setVisible(true);
		if (autoCompletePopup.isEmpty() || searchField.getText().isEmpty()) {
			autoCompletePopup.hide();
			if (searchField.getText().isEmpty()) {
				textClearButton.setVisible(false);
				//reset media list if the text field has been cleared, only if displaying search results
				if (showingMedia.size() != ControllerMaster.userData.numMediaItems()) {
					autoCompletePopup.clearItems();
					autoSelection = null;
					refreshSearch();
				}
			}
		} else {
			showAutoComplete();
		}
	}

	private void showAutoComplete(){
		JFXMediaRippler.forceHidePopOver();
		if (!autoCompletePopup.isShowing()) {
			autoCompletePopup.show(searchField, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0, searchField.getHeight());
		}
	}
	
	public void updateScale() {
		tilePane.setVgap(15*ControllerMaster.userData.getScaleFactor());
		tilePane.setHgap(10*ControllerMaster.userData.getScaleFactor());
		JFXMediaRippler n;
		for (int i = 0; i < tilePane.getChildren().size(); ++i) {
			n = ((JFXMediaRippler)tilePane.getChildren().get(i));
			n.updateScale();
		}
	}
	
	public void refreshSearch() {
		if (autoSelection != null) {
			ControllerMaster.userData.refreshViewingList(autoSelection.getTargetIDs());
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
		playlistCombo.getItems().clear();
		playlistCombo.setItems(FXCollections.observableArrayList(ControllerMaster.userData.userPlaylists));
	}

	//don't show collections with only one movie (setting maybe?)
	public void updateCollectionCombo() {
		collectionsCombo.getItems().clear();
		List<Collection> collections = new ArrayList<>();
		for (Collection c : ControllerMaster.userData.ownedCollections.keySet()) {
			if (ControllerMaster.userData.ownedCollections.get(c).size() >1) {
				collections.add(c);
			}
		}
		collectionsCombo.setItems( FXCollections.observableArrayList(collections));
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
							ControllerMaster.addMediaDialogController.openDialogMenu(ControllerMaster.addMediaWindow, true);
						}
				        KeyCombination keyCombinationAdd = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
				        newScene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
				            if (event.getCode().equals(KeyCode.DOWN) && autoCompletePopup.isShowing()) {

				            } else if (keyCombinationAdd.match(event)) {
				            	ControllerMaster.showAddMediaDialog();
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



	@FXML
	public void showAboutDialog() {

	}
	
	
	//create pane for the media
	public JFXMediaRippler addMediaTile(MediaItem mediaObject) {
		JFXMediaRippler rippler = JFXMediaRippler.createBasicRippler(scrollPane);
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
