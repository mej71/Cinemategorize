package application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.Rating;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvSeries;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class SelectionViewController implements Initializable {
	
	private final int numDirectorsAllowed = 5;
	private final int numWritersAllowed = 5;
	private final int numActorsAllowed = 15;
	
	@FXML private GridPane mainGrid;
	@FXML private ImageView posterImageView;
    @FXML private Label descriptionLabel;
    @FXML private GridPane canResumeGridPane;
    @FXML private JFXButton resumeButton;
    @FXML private JFXButton playBeginningButton1;
    @FXML private JFXButton trailerButton1;
    @FXML private GridPane noResumeGridPane;
    @FXML private JFXButton playBeginningButton2;
    @FXML private JFXButton trailerButton2;
    @FXML private GridPane tvTitleGridPane;
    @FXML private Label tvTitleLabel;
    @FXML private JFXComboBox<String> seasonComboBox;
    @FXML private JFXComboBox<String> episodeComboBox;
    @FXML private GridPane movieTitleGridPane;
    @FXML private Label movieTitleLabel;
    @FXML private StackPane youtubeStackPane;
    @FXML private Label genreLabel;
    @FXML private Label writtenLabel;
    @FXML private Label runTimeLabel;
    @FXML private JFXHamburger hamburger;
    @FXML private FlowPane directorFlowPane;
    @FXML private FlowPane writerFlowPane;
    @FXML private FlowPane actorFlowPane;
    @FXML private ScrollPane infoScrollPane;
    @FXML private Rating rating;
    @FXML private FlowPane genreFlowPane;
    @FXML private FlowPane tagsFlowPane;
    @FXML private Label ratingLabel;
    //other variables
    private MediaItem mediaItem;
    private CustomMovieDb showingCm;
    private TvSeries showingSeries;
    private JFXDialog dLink;
    private String videoLink;
    private FXMLLoader loader;
	private GridPane personView;
	private PersonViewController personViewController;
	private JFXDialog personViewDialog;
	private List<JFXPersonRippler> actorTiles = new ArrayList<JFXPersonRippler>();
	private List<JFXPersonRippler> directorTiles = new ArrayList<JFXPersonRippler>();
	private List<JFXPersonRippler> writerTiles = new ArrayList<JFXPersonRippler>();
	public HamburgerBackArrowBasicTransition burgertask;
	private Task<?> selectionTask;
    
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		JFXScrollPane.smoothScrolling(infoScrollPane);
		for (int i = 0; i < numDirectorsAllowed; ++i) {
			directorTiles.add(JFXPersonRippler.createBasicRippler());
		}
		for (int i = 0; i < numWritersAllowed; ++i) {
			writerTiles.add(JFXPersonRippler.createBasicRippler());
		}
		for (int i = 0; i < numActorsAllowed; ++i) {
			actorTiles.add(JFXPersonRippler.createBasicRippler());
		}
		// use the hamburger button to open/close the drawer
		burgertask = new HamburgerBackArrowBasicTransition(hamburger);
		burgertask.setRate(-1);
		hamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
			toggleDrawer(false);
		});
		rating.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (showingCm!=null) {
					mediaItem.rating = rating.getRating();
		    		if (!rating.getStyleClass().contains("loaded")) {
						rating.getStyleClass().add("loaded");
					}
		    	}
			}
			
		});
		infoScrollPane.prefWidthProperty().bind(mainGrid.widthProperty().multiply(0.64));
		infoScrollPane.prefHeightProperty().bind(mainGrid.heightProperty().multiply(0.70));
		directorFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		writerFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		actorFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		genreFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		tagsFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		posterImageView.fitHeightProperty().bind(mainGrid.heightProperty().multiply(0.83));
	}
	
	public void showMediaItem(JFXDialog d, MediaItem mi) {
		dLink = d;
		mediaItem = mi;
		if (personViewDialog == null) {
			try {
				loader = new FXMLLoader(getClass().getResource("PersonViewContent.fxml"));
				personView = loader.load();
				personViewController = loader.getController();
				personViewDialog = new JFXDialog(ControllerMaster.mainController.getBackgroundStackPane(), personView,
						JFXDialog.DialogTransition.CENTER);
				personView.prefWidthProperty().bind(ControllerMaster.mainController.mainPane.widthProperty().divide(1.35));
				personView.prefHeightProperty().bind(ControllerMaster.mainController.mainPane.heightProperty().divide(1.15));
				JFXPersonRippler.setStaticVariables(personViewController, personViewDialog);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dLink.show();
		dLink.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
		    if (event.getCode().equals(KeyCode.ESCAPE)) {
		        dLink.close();
		    }
		});
		
		selectionTask = new Task() {

			@Override
			protected Object call() throws Exception {
				fillInfo();				
				return null;
			}
			
		};
		selectionTask.run();
	}
	
	public void fillInfo() {
		infoScrollPane.setVvalue(0);
		if (mediaItem.isMovie()) {
			showingCm = ControllerMaster.userData.getMovieById(mediaItem.getId()).cMovie;
			if (showingCm == null) {
				return;
			}
			MovieDb movie = showingCm.movie;
			movieTitleLabel.setText(movie.getTitle()+ " (" + movie.getReleaseDate().substring(0, 4)+")");
			genreLabel.setText( (movie.getGenres().size()>1)? "Genres:" : "Genre:" );
			genreFlowPane.getChildren().clear();
			for (int i = 0; i < movie.getGenres().size(); ++i) {
				genreFlowPane.getChildren().add(JFXCustomChips.getGenreChip(movie.getGenres().get(i)));
			}
			
			
			int dirCount = 0;
			int writCount = 0;
			ObservableList<Node> workingDirectorCollection = FXCollections.observableArrayList();
			ObservableList<Node> workingWriterCollection = FXCollections.observableArrayList();
			ObservableList<Node> workingActorCollection = FXCollections.observableArrayList();
			PersonCrew pCrew;
			List<Integer> writerIds = new ArrayList<Integer>();
			for (int i = 0; i < movie.getCrew().size(); ++i) {
				pCrew = movie.getCrew().get(i);
				if (pCrew.getJob().equalsIgnoreCase("Director")) {
					directorTiles.get(dirCount).setPersonCrew(pCrew, mediaItem);
					workingDirectorCollection.add(directorTiles.get(dirCount));
					++dirCount;
				} else if (pCrew.getJob().equalsIgnoreCase("Screenplay") || 
						pCrew.getJob().equalsIgnoreCase("Writer") || pCrew.getJob().equalsIgnoreCase("Story") || 
						pCrew.getJob().equalsIgnoreCase("Author")) {
					if (writCount<numWritersAllowed && !writerIds.contains(pCrew.getId())) {
						writerTiles.get(writCount).setPersonCrew(pCrew, mediaItem);
						workingWriterCollection.add(writerTiles.get(writCount));
						writerIds.add(pCrew.getId());
					} 
					++writCount;
				}
			}
			
			PersonCast pCast;
			for (int i = 0; i < movie.getCast().size() && i<13; ++i) {
				pCast = movie.getCast().get(i);
				actorTiles.get(i).setPersonCast(pCast, mediaItem);
				workingActorCollection.add(actorTiles.get(i));				
			}
			directorFlowPane.getChildren().setAll(workingDirectorCollection);
			writerFlowPane.getChildren().setAll(workingWriterCollection);
			actorFlowPane.getChildren().setAll(workingActorCollection);
			if (movie.getVideos().size()>0) {
				videoLink = movie.getVideos().get(0).getKey();
			} else {
				videoLink = null;
			}
			movieTitleGridPane.setVisible(true);
			tvTitleGridPane.setVisible(false);
			descriptionLabel.setText("\t"+ movie.getOverview());
			if (mediaItem.rating==-1) {
				if (rating.getStyleClass().contains("loaded")) {
					rating.getStyleClass().remove("loaded");
				}
				rating.setRating(movie.getVoteAverage()/10*5);
			} else {
				rating.setRating(mediaItem.rating);
				if (!rating.getStyleClass().contains("loaded")) {
					rating.getStyleClass().add("loaded");
				}
			}
			tagsFlowPane.getChildren().clear();
			for (int i = 0; i < movie.getKeywords().size(); ++i) {
				tagsFlowPane.getChildren().add(JFXCustomChips.getTagChip(movie.getKeywords().get(i).getName()));
			}
			boolean found = false;
			for (int i = 0; i < movie.getReleases().size(); ++i) {
				if (movie.getReleases().get(i).getCountry().equalsIgnoreCase("US")) {
					for (int j = 0; j < movie.getReleases().get(i).getReleaseDates().size(); ++j) {
						if (movie.getReleases().get(i).getReleaseDates().get(j).getCertification()!=null && !movie.getReleases().get(i).getReleaseDates().get(j).getCertification().isEmpty()) {
							ratingLabel.setText(movie.getReleases().get(i).getReleaseDates().get(j).getCertification());
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
			}
		
			
			seasonComboBox.setVisible(false);
			episodeComboBox.setVisible(false);
			runTimeLabel.setText("Runtime: " + String.format("%d", movie.getRuntime()/60) + ":" + String.format("%02d", movie.getRuntime()%60));
			Platform.runLater(() -> directorFlowPane.requestLayout());
			Platform.runLater(() -> writerFlowPane.requestLayout());
			Platform.runLater(() -> actorFlowPane.requestLayout());
		} else {
			seasonComboBox.setVisible(true);
			episodeComboBox.setVisible(true);
			movieTitleGridPane.setVisible(false);
			tvTitleGridPane.setVisible(true);
		}
		posterImageView.setImage(MediaSearchHandler.getItemPoster(mediaItem, 500).getImage());
	}
	
	// toggle drawer menu and play hamburger animation
	public void toggleDrawer(boolean openOnly) {
		if (burgertask.getRate() > 0) {
			//drawerMenu.close();
		} else if (!openOnly) {
			//drawerMenu.open();
		} else {
			return;
		}
		burgertask.setRate(burgertask.getRate() * -1);
		burgertask.play();
	}
	
	
	
	
	@FXML public void playTrailer() {
		if (videoLink==null || videoLink.isEmpty()) {
			//notification?
			return;
		}
		try {
			Desktop.getDesktop().browse(new URL("https://www.youtube.com/watch?v=" + videoLink + "?autoplay=1").toURI());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
