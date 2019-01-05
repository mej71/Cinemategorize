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
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class SelectionViewController extends LoadingControllerBase implements Initializable {
	
	private final int numDirectorsAllowed = 5;
	private final int numWritersAllowed = 5;
	private final int numActorsAllowed = 15;
	
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
    @FXML private Label dirLabel;
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
    private String videoLink;
    private FXMLLoader loader;
	private GridPane personView;
	private PersonViewController personViewController;
	private JFXDialog personViewDialog;
	private List<JFXPersonRippler> actorTiles = new ArrayList<JFXPersonRippler>();
	private List<JFXPersonRippler> directorTiles = new ArrayList<JFXPersonRippler>();
	private List<JFXPersonRippler> writerTiles = new ArrayList<JFXPersonRippler>();
	public HamburgerBackArrowBasicTransition burgertask;
    
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		super.initialize(url, rb);
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
				if (mediaItem!=null) {
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
		super.setDialogLink(d);
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
		loadTask = new Task<Object>() {

			@Override
			protected Object call() throws Exception {
				fillMainInfo();
				fillInfo();	
				overlayPane.setVisible(false);
				overlayPane.setDisable(true);
				return null;
			}
		};
		dLink.show();	
		
		
	}
	
	//does any info not specific to episode
	public void fillMainInfo() {
		infoScrollPane.setVvalue(0);

		movieTitleLabel.setText(mediaItem.getTitle()+ " (" + mediaItem.getReleaseDate().substring(0, 4)+")");
		genreLabel.setText( (mediaItem.getGenres().size()>1)? "Genres:" : "Genre:" );
		genreFlowPane.getChildren().clear();
		for (int i = 0; i < mediaItem.getGenres().size(); ++i) {
			genreFlowPane.getChildren().add(JFXCustomChips.getGenreChip(mediaItem.getGenres().get(i)));
		}
		if (mediaItem.rating==-1) {
			if (rating.getStyleClass().contains("loaded")) {
				rating.getStyleClass().remove("loaded");
			}
			rating.setRating(mediaItem.getVoteAverage()/10*5);
		} else {
			rating.setRating(mediaItem.rating);
			if (!rating.getStyleClass().contains("loaded")) {
				rating.getStyleClass().add("loaded");
			}
		}
		tagsFlowPane.getChildren().clear();
		
		for (int i = 0; i < mediaItem.getKeywords().size(); ++i) {
			tagsFlowPane.getChildren().add(JFXCustomChips.getTagChip(mediaItem.getKeywords().get(i).getName()));
		}
		
		//movie ratings are stored in releases, tv in content rating
		boolean found = false;
		if (mediaItem.isMovie()) {
			for (int i = 0; i < mediaItem.getReleases().size(); ++i) {
				if (mediaItem.getReleases().get(i).getCountry().equalsIgnoreCase("US")) {
					for (int j = 0; j < mediaItem.getReleases().get(i).getReleaseDates().size(); ++j) {
						if (mediaItem.getReleases().get(i).getReleaseDates().get(j).getCertification()!=null && !mediaItem.getReleases().get(i).getReleaseDates().get(j).getCertification().isEmpty()) {
							ratingLabel.setText(mediaItem.getReleases().get(i).getReleaseDates().get(j).getCertification());
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < mediaItem.getContentRating().size(); ++i) {
				if (mediaItem.getContentRating().get(i).getLocale().equalsIgnoreCase("US")) {
					found = true;
					ratingLabel.setText(mediaItem.getContentRating().get(i).getRating());
					break;
				}
			}
		}
		if (!found) {
			ratingLabel.setText("N/A");
		}
		seasonComboBox.setVisible(mediaItem.isTvShow());
		episodeComboBox.setVisible(mediaItem.isTvShow());
		movieTitleGridPane.setVisible(mediaItem.isMovie());
		tvTitleGridPane.setVisible(mediaItem.isTvShow());
		posterImageView.setImage(MediaSearchHandler.getItemPoster(mediaItem, 500).getImage());
	}
	
	public void fillInfo() {		
		int dirCount = 0;
		int writCount = 0;
		ObservableList<Node> workingDirectorCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingWriterCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingActorCollection = FXCollections.observableArrayList();
		PersonCrew pCrew;
		List<Integer> writerIds = new ArrayList<Integer>();
		for (int i = 0; i < mediaItem.getCrew().size(); ++i) {
			pCrew = mediaItem.getCrew().get(i);
			if (pCrew.getJob().equalsIgnoreCase("Director")) {
				directorTiles.get(dirCount).setPerson(pCrew, mediaItem);
				workingDirectorCollection.add(directorTiles.get(dirCount));
				++dirCount;
			} else if (pCrew.getJob().equalsIgnoreCase("Screenplay") || 
					pCrew.getJob().equalsIgnoreCase("Writer") || pCrew.getJob().equalsIgnoreCase("Story") || 
					pCrew.getJob().equalsIgnoreCase("Author")) {
				if (writCount<numWritersAllowed && !writerIds.contains(pCrew.getId())) {
					writerTiles.get(writCount).setPerson(pCrew, mediaItem);
					workingWriterCollection.add(writerTiles.get(writCount));
					writerIds.add(pCrew.getId());
				} 
				++writCount;
			}
		}
		
		PersonCast pCast;
		for (int i = 0; i < mediaItem.getCast().size() && i<13; ++i) {
			pCast = mediaItem.getCast().get(i);
			actorTiles.get(i).setPerson(pCast, mediaItem);
			workingActorCollection.add(actorTiles.get(i));				
		}
		directorFlowPane.getChildren().setAll(workingDirectorCollection);
		writerFlowPane.getChildren().setAll(workingWriterCollection);
		actorFlowPane.getChildren().setAll(workingActorCollection);
		if (mediaItem.getVideos().size()>0) {
			videoLink = mediaItem.getVideos().get(0).getKey();
		} else {
			videoLink = null;
		}
		movieTitleGridPane.setVisible(true);
		tvTitleGridPane.setVisible(false);
		descriptionLabel.setText("\t"+ mediaItem.getOverview());		
		
		//use runtime 
		String runtimeString = (mediaItem.isMovie())? "Runtime: " : "Episode Avg: ";
		runTimeLabel.setText(runtimeString + String.format("%d", mediaItem.getRuntime()/60) + ":" + String.format("%02d", mediaItem.getRuntime()%60));
		Platform.runLater(() -> directorFlowPane.requestLayout());
		Platform.runLater(() -> writerFlowPane.requestLayout());
		Platform.runLater(() -> actorFlowPane.requestLayout());		
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
