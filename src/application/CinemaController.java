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
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

public class CinemaController implements Initializable {

	// local content
	@FXML BorderPane mainPane;
	@FXML private JFXHamburger hamburger;
	@FXML StackPane stackPane;
	@FXML private StackPane backgroundStackPane;
	@FXML private JFXButton searchButton;
	// included FXML controllers and content
	
	@FXML private GridPane mainGrid;
	
	
	@FXML JFXComboBox<SortTypes> sortCombo;
	@FXML JFXTextField searchField;
	@FXML JFXSlider scaleSlider;
	@FXML private Label scaleLabel;
	@FXML private JFXButton textClearButton;
	@FXML private JFXButton startDateClearButton;
	@FXML private JFXButton endDateClearButton;
	@FXML private JFXDrawer drawerMenu;
	@FXML JFXComboBox<Integer> startYearComboBox;
	@FXML JFXComboBox<Integer> endYearComboBox;
	
	// local content initialized outside of the fxml
	TilePane tilePane;
	
	MovieScrollPane scrollPane;
	public HamburgerBackArrowBasicTransition burgerTask;
	// other variables
	public final String[] supportedFileTypes = { "*.mp4", "*.avi", "*.wmv", "*.flv", "*.mov", "*.mkv" };
	public MediaListDisplayType showingType = MediaListDisplayType.MOVIES;
	public List<JFXMediaRippler> allTiles = new ArrayList<JFXMediaRippler>();
	public LinkedHashMap<String, MediaItem> showingMedia = new LinkedHashMap<String, MediaItem>();
	public MovieAutoCompletePopup autoCompletePopup;
	public MovieAutoCompleteEvent<SearchItem> autoEvent;
	public boolean isScrolling = false;
	JFXListView<String> list;
	
	private FXMLLoader loader;
	private GridPane selectionView;
	private SelectionViewController selectionViewController;
	private JFXDialog selectionViewWindow;
	
	private GridPane manualLookupView;
	private ManualLookupController manualLookupController;
	private JFXDialog manualLookupWindow;
	
	private JFXDialogLayout addMovieDialogView;
	private AddMoviesDialogController addMovieDialogController;
	private JFXDialog addMovieWindow;
	
	private GridPane sidePanelView;
	private SidePanelController sidePanelController;
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		ControllerMaster.mainController = this;
		
		sortCombo.setItems(FXCollections.observableArrayList( SortTypes.values()));
		sortCombo.setValue(SortTypes.NAME_ASC);
		sortCombo.valueProperty().addListener(new ChangeListener<SortTypes>() {

			@Override
			public void changed(ObservableValue<? extends SortTypes> arg0, SortTypes arg1, SortTypes arg2) {
				ControllerMaster.userData.sortShownItems();
			}
			
		});
		

		// create tilepane and add to the scene
		tilePane = new TilePane();
		tilePane.setPadding(new Insets(5, 5, 5, 5));
		tilePane.setVgap(15);
		tilePane.setHgap(10);
		//tilePane.setCache(true);
		
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
		JFXSmoothScroll.smoothScrolling(scrollPane);
		stackPane.getChildren().add(scrollPane);
		
		// use the hamburger button to open/close the drawer
		burgerTask = new HamburgerBackArrowBasicTransition(hamburger);
		burgerTask.setRate(-1);
		hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
			toggleDrawer(false);
		});

		// close drawer if clicked outside while open
		backgroundStackPane.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
			if (!inHierarchy(evt.getPickResult().getIntersectedNode(), sidePanelView)
					&& !inHierarchy(evt.getPickResult().getIntersectedNode(), hamburger)) {
				toggleDrawer(true);
			}
		});
		
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
	            	if (endYearComboBox.getValue() != null  && newValue >= endYearComboBox.getValue()) {
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
 	            	if (startYearComboBox.getValue()!=null && newValue <= startYearComboBox.getValue()) {
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
			loader = new FXMLLoader(getClass().getResource("SelectionViewContent.fxml"));
			selectionView = loader.load();
			selectionViewController = loader.getController();
			selectionViewWindow = new JFXDialog(getBackgroundStackPane(), selectionView,
					JFXDialog.DialogTransition.CENTER);
			selectionView.prefWidthProperty().bind(mainPane.widthProperty().divide(1.15));
			selectionView.prefHeightProperty().bind(mainPane.heightProperty().divide(1.3));
			loader = new FXMLLoader(getClass().getResource("ManualLookupContent.fxml"));
			manualLookupView = loader.load();
			manualLookupController = loader.getController();
			manualLookupWindow = new JFXDialog(getBackgroundStackPane(), manualLookupView,
					JFXDialog.DialogTransition.CENTER);
			manualLookupView.prefWidthProperty().bind(mainPane.widthProperty().divide(1.15));
			manualLookupView.prefHeightProperty().bind(mainPane.heightProperty().divide(1.15));
			loader = new FXMLLoader(getClass().getResource("AddMoviesDialogContent.fxml"));
			addMovieDialogView = loader.load();
			addMovieDialogController = loader.getController();
			addMovieWindow = new JFXDialog(getBackgroundStackPane(), addMovieDialogView,
					JFXDialog.DialogTransition.CENTER);
			loader = new FXMLLoader(getClass().getResource("MainSidePanelContent.fxml"));
			sidePanelView = loader.load();
			sidePanelController = loader.getController();
		} catch(Exception e) {
			e.printStackTrace();
		}
		// create the drawer and add to scene
		drawerMenu.setSidePane(sidePanelView);
		drawerMenu.minHeightProperty().bind(backgroundStackPane.heightProperty());
		sidePanelController.setDrawer(drawerMenu);
		drawerMenu.setOnDrawerClosed(e -> {
			burgerTask.setRate(-1);
			burgerTask.play();
			drawerMenu.setVisible(false);
			sidePanelController.mainMenuListView.getSelectionModel().clearSelection();	
	    });
		drawerMenu.setOnDrawerOpening(e -> {
			drawerMenu.setVisible(true);
	    });
		ControllerMaster.userData.createAllRipplers();
		ControllerMaster.userData.refreshViewingList(null, false);
	    scaleSlider.setValue(ControllerMaster.userData.getScaleFactor()*4);
		determinePrimaryStage();	
		
		list = new JFXListView<>();
		for (int i = 0; i <= 200; i++) {
			list.getItems().add(Integer.toString(1900 + i));
        }
		list.setVisible(false);
		list.setOpacity(0);
		list.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,
            CornerRadii.EMPTY,  Insets.EMPTY)));
		list.resize(100, 300);
		list.setMaxSize(150, 300);
		backgroundStackPane.getChildren().add(list);
	}	
	
	@FXML private void clearText() {
		searchField.clear();
		searchField.requestFocus();
	}
	
	@FXML private void clearStartDate() {
		startYearComboBox.getSelectionModel().clearSelection();
		mainPane.requestFocus();
	}
	
	@FXML private void clearEndDate() {
		endYearComboBox.getSelectionModel().clearSelection();
		mainPane.requestFocus();
	}
	
	public void fillYearCombos(int minYear, int maxYear) {
		startYearComboBox.setItems(
				FXCollections.observableArrayList(IntStream.rangeClosed(minYear,maxYear).boxed().collect(Collectors.toList()))
	    );  
	    endYearComboBox.setItems(
	    		FXCollections.observableArrayList(IntStream.rangeClosed(minYear,maxYear).boxed().collect(Collectors.toList()))
	    ); 
	}

	//make sure everything is loaded, then if the media list is empty force the player to add at least one item
	private void determinePrimaryStage() {
		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (oldScene == null && newScene != null) {
				// scene is set for the first time. Now its the time to listen stage changes.
				newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
					if (oldWindow == null && newWindow != null) {
						// if no directories to search, user must select at least one
						// opens InitialChooseDialogContent, and waits for user to add one to selection
						if (ControllerMaster.userData.numMediaItems() == 0) {
							addMovieDialogController.openDialogMenu(addMovieWindow, true);
						} 
					}
				});
			}
		});
	}
	
	public void showManualLookupDialog(LinkedHashMap<MediaItem, MediaResultsPage> mediaList, JFXDialog aLink) {
		manualLookupController.setData(mediaList);
		manualLookupController.openDialog(manualLookupWindow, aLink);
		manualLookupView.requestFocus();
	}
	
	public void showAddMovieDialog() {
		addMovieDialogController.openDialogMenu(addMovieWindow, false);
		addMovieDialogView.requestFocus();
	}
		
	public void showSelectionDialog(MediaItem mi) {
		
		selectionViewController.showMediaItem(selectionViewWindow, mi);
		selectionView.requestFocus();
	}
	
	public void closeDialogs() {
		selectionViewWindow.close();
		addMovieWindow.close();
		manualLookupWindow.close();
		JFXPersonRippler.closeWindow();
	}

	// toggle drawer menu and play hamburger animation
	public void toggleDrawer(boolean openOnly) {
		if (burgerTask.getRate() > 0) {
			drawerMenu.close();
		} else if (!openOnly) {
			burgerTask.setRate(1);
			burgerTask.play();
			drawerMenu.open();
		} else {
			return;
		}
	}
	
	
	//create pane for the media
	public JFXMediaRippler addMediaTile(MediaItem mediaObject) {
		JFXMediaRippler rippler = JFXMediaRippler.createBasicRippler(tilePane, scrollPane, burgerTask);
		rippler.setItem(mediaObject);
		return rippler;
	}
	

	private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
		if (potentialHierarchyElement == null) {
			return true;
		}
		while (node != null) {
			if (node == potentialHierarchyElement) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}
	
	public AddMoviesDialogController getAddMovieDialogController() {
		return addMovieDialogController;
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
