package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import info.movito.themoviedbapi.model.Collection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CinemaController implements Initializable {

	// local content
	@FXML StackPane backgroundStackPane;
	@FXML StackPane stackPane;
	// included FXML controllers and content	
	@FXML GridPane mainGrid;
	
	
	@FXML JFXComboBox<SortTypes> sortCombo;
	@FXML JFXTextField searchField;
	@FXML JFXSlider scaleSlider;
	@FXML private Label scaleLabel;
	@FXML private JFXButton textClearButton;
	@FXML private JFXButton startDateClearButton;
	@FXML private JFXButton endDateClearButton;
	@FXML private JFXButton playlistClearButton;
	@FXML private JFXButton playlistAddButton;
	@FXML private JFXButton collectionsClearButton;

	@FXML JFXComboBox<Integer> startYearComboBox;
	@FXML JFXComboBox<Integer> endYearComboBox;
	@FXML JFXComboBox<String> playlistCombo;
	@FXML JFXComboBox<Collection> collectionsCombo;
	
	// local content initialized outside of the fxml
	TilePane tilePane;
	
	MovieScrollPane scrollPane;
	// other variables
	public final String[] supportedFileTypes = { "*.mp4", "*.avi", "*.wmv", "*.flv", "*.mov", "*.mkv" };
	public MediaListDisplayType showingType = MediaListDisplayType.MOVIES;
	public List<JFXMediaRippler> allTiles = new ArrayList<JFXMediaRippler>();
	public LinkedHashMap<String, MediaItem> showingMedia = new LinkedHashMap<String, MediaItem>();
	public MovieAutoCompletePopup autoCompletePopup;
	public MovieAutoCompleteEvent<SearchItem> autoEvent;
	public boolean isScrolling = false;
	
	private FXMLLoader loader;
	private GridPane selectionView;
	JFXDialog selectionViewWindow;
	
	private GridPane manualLookupView;
	JFXDialog manualLookupWindow;
	
	private JFXDialogLayout addMediaDialogView;
	JFXDialog addMediaWindow;
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		ControllerMaster.mainController = this;
		
		mainGrid.prefWidthProperty().bind(backgroundStackPane.widthProperty());
		mainGrid.prefHeightProperty().bind(backgroundStackPane.heightProperty());
		
		sortCombo.setItems(FXCollections.observableArrayList( SortTypes.values()));
		sortCombo.setValue(SortTypes.NAME_ASC);
		sortCombo.valueProperty().addListener(new ChangeListener<SortTypes>() {

			@Override
			public void changed(ObservableValue<? extends SortTypes> arg0, SortTypes arg1, SortTypes arg2) {
				ControllerMaster.userData.sortShownItems();
			}
			
		});
		
		updatePlaylistCombo();
		playlistCombo.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

			@Override
			public PlaylistCell<String> call(ListView<String> param) {
				return new PlaylistCell();
			}
			
		});
		playlistCombo.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue!=null) {
					playlistClearButton.setVisible(true);
	            } else {
	            	playlistClearButton.setVisible(false);
	            }
				if (autoEvent!=null) {
	            	ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs(), false);
				} else {
					ControllerMaster.userData.refreshViewingList(null, false);
				}
			}
		});
		
		updateCollectionCombo();
		collectionsCombo.setCellFactory(new Callback<ListView<Collection>, ListCell<Collection>>() {
			@Override
			public CollectionCell<Collection> call(ListView<Collection> param) {
				return new CollectionCell();
			}
			
		});
		collectionsCombo.valueProperty().addListener(new ChangeListener<Collection>() {

			@Override
			public void changed(ObservableValue<? extends Collection> observable, Collection oldValue, Collection newValue) {
				if (newValue!=null) {
					collectionsClearButton.setVisible(true);
	            } else {
	            	collectionsClearButton.setVisible(false);
	            }
				if (autoEvent!=null) {
	            	ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs(), false);
				} else {
					ControllerMaster.userData.refreshViewingList(null, false);
				}
			}
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
		TileAnimator tileAnimator = new TileAnimator();
		tileAnimator.observe(tilePane.getChildren());
		scaleSlider.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue source, Object oldValue, Object newValue) {
            	ControllerMaster.userData.setScaleFactor(scaleSlider.getValue()*0.25);
            	tilePane.setVgap(15*ControllerMaster.userData.getScaleFactor());
        		tilePane.setHgap(10*ControllerMaster.userData.getScaleFactor());
        		StackPane n;
        		for (int i = 0; i < tilePane.getChildren().size(); ++i) {
        			n = ((JFXMediaRippler)tilePane.getChildren().get(i)).getPane();
        			n.setMaxWidth(139*ControllerMaster.userData.getScaleFactor());
        			n.setMaxHeight(208*ControllerMaster.userData.getScaleFactor());
        			n.resize(139*ControllerMaster.userData.getScaleFactor(), 208*ControllerMaster.userData.getScaleFactor());
        		}
            	scaleLabel.textProperty().setValue("Image Size: " + (Math.round(ControllerMaster.userData.getScaleFactor() * 100.0) / 100.0));
            } 
        });
		
		scrollPane = new MovieScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.setContent(tilePane);
		JFXScrollPane.smoothScrolling(scrollPane);
		stackPane.getChildren().add(scrollPane);
		
		autoCompletePopup = new MovieAutoCompletePopup();
		autoCompletePopup.prefWidthProperty().bind(searchField.widthProperty());
		autoCompletePopup.setCellLimit(7);
	    autoCompletePopup.setMovieSelectionHandler(e -> {
	    	autoEvent = (MovieAutoCompleteEvent<SearchItem>)e;
	    	ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs(), false);
	    	searchField.setText(autoEvent.getObject().getItemName());
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
	            	if (showingMedia.size()!=ControllerMaster.userData.numMediaItems()) {
	            		ControllerMaster.userData.refreshViewingList(null, false);
		            	autoCompletePopup.getSuggestions().clear();
		            	autoEvent = null;
	            	}
	            }             
	        } else {
	            autoCompletePopup.show(searchField);
	        }
	    });
	    
	    searchField.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (!autoCompletePopup.getFilteredSuggestions().isEmpty() && !searchField.getText().isEmpty()) {
                	autoCompletePopup.show(searchField);
                }				
			}
	    });
	    
	    fillYearCombos(ControllerMaster.userData.minYear, ControllerMaster.userData.maxYear);
	    //date range validation
	    startYearComboBox.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> ov, Integer oldValue, Integer newValue) {
				if (newValue!=null) {
	            	if (endYearComboBox.getValue() != null  && newValue > endYearComboBox.getValue()) {
	            		endYearComboBox.getSelectionModel().clearSelection();
	            	}
	            	startDateClearButton.setVisible(true);
	            } else {
	            	startDateClearButton.setVisible(false);
	            }
	            if (autoEvent!=null) {
	            	ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs(), false);
				} else {
					ControllerMaster.userData.refreshViewingList(null, false);
				}
			}
		});

	    endYearComboBox.valueProperty().addListener(new ChangeListener<Integer>() {
 			@Override
 			public void changed(ObservableValue<? extends Integer> ov, Integer oldValue, Integer newValue) {
 				if (newValue!=null) {
 	            	if (startYearComboBox.getValue()!=null && newValue < startYearComboBox.getValue()) {
 	            		startYearComboBox.getSelectionModel().clearSelection();
 	            	}
 	            	endDateClearButton.setVisible(true);
 	            } else {
 	            	endDateClearButton.setVisible(false);
 	            }
 	            if (autoEvent!=null) {
 	            	ControllerMaster.userData.refreshViewingList(autoEvent.getObject().getTargetIDs(), false);
 				} else {
 					ControllerMaster.userData.refreshViewingList(null, false);
 				}
 			}
	 	});
	    
	    //set up dialogs
		try {
			loader = new FXMLLoader(getClass().getClassLoader().getResource("SelectionViewContent.fxml"));
			selectionView = loader.load();
			ControllerMaster.selectionViewController = loader.getController();
			selectionViewWindow = new JFXDialog(getBackgroundStackPane(), selectionView,
					JFXDialog.DialogTransition.CENTER);
			selectionView.prefWidthProperty().bind(backgroundStackPane.widthProperty().divide(1.15));
			selectionView.prefHeightProperty().bind(backgroundStackPane.heightProperty().divide(1.3));
			loader = new FXMLLoader(getClass().getClassLoader().getResource("ManualLookupContent.fxml"));
			manualLookupView = loader.load();
			ControllerMaster.manualController = loader.getController();			
			manualLookupWindow = new JFXDialog(getBackgroundStackPane(), manualLookupView,
					JFXDialog.DialogTransition.CENTER);
			manualLookupView.prefWidthProperty().bind(backgroundStackPane.widthProperty().divide(1.15));
			manualLookupView.prefHeightProperty().bind(backgroundStackPane.heightProperty().divide(1.15));
			loader = new FXMLLoader(getClass().getClassLoader().getResource("AddMoviesDialogContent.fxml"));
			addMediaDialogView = loader.load();
			ControllerMaster.addMediaDialogController = loader.getController();
			addMediaWindow = new JFXDialog(getBackgroundStackPane(), addMediaDialogView,
					JFXDialog.DialogTransition.CENTER);
		} catch(Exception e) {
			e.printStackTrace();
		}

		createAllRipplers();
		ControllerMaster.userData.refreshViewingList(null, false);
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
	
	@FXML private void addPlaylist() {
		
	}
	
	@FXML private void clearCollectionSelection() {
		collectionsCombo.getSelectionModel().clearSelection();
		mainGrid.requestFocus();
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
		playlistCombo.setItems( FXCollections.observableArrayList(ControllerMaster.userData.userPlaylists.getPlaylistNames()));
	}
	
	public void updateCollectionCombo() {
		collectionsCombo.setItems( FXCollections.observableArrayList(ControllerMaster.userData.ownedCollections.keySet()));
	}	

	//make sure everything is loaded, then if the media list is empty force the player to add at least one item
	private void determinePrimaryStage() {
		mainGrid.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (oldScene == null && newScene != null) {
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
	
	private Window window;
	public Window getMainWindow() {
		return window;
	}
	
	public void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList) {
		ControllerMaster.manualController.setData(mediaList);
		ControllerMaster.manualController.openDialog(manualLookupWindow);
		JFXMediaRippler.forceHidePopOver();
	}
	
	public void showAddMediaDialog() {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.addMediaDialogController.openDialogMenu(addMediaWindow, false);		
	}
		
	public void showSelectionDialog(MediaItem mi) {
		JFXMediaRippler.forceHidePopOver();
		ControllerMaster.selectionViewController.showMediaItem(selectionViewWindow, mi);	
		
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
		
		private SortTypes(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return this.toString;
		}
	}
	
}
