package application;

import java.awt.Desktop;
import java.io.File;
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
import com.jfoenix.controls.JFXScrollPane;

import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

@SuppressWarnings("rawtypes")
public class SelectionViewController extends LoadingControllerBase implements Initializable {

	private final int numWritersAllowed = 5;

	@FXML private ImageView posterImageView;
    @FXML private Text descLabel;
    @FXML private GridPane tvTitleGridPane;
    @FXML private Label tvTitleLabel;
    @FXML private JFXComboBox<String> seasonComboBox;
    @FXML private JFXComboBox<String> episodeComboBox;
    @FXML private GridPane movieTitleGridPane;
    @FXML private Label movieTitleLabel;
    @FXML private Label dirLabel;
    @FXML private Label genreLabel;
    @FXML private Label runTimeLabel;
    @FXML private FlowPane directorFlowPane;
    @FXML private FlowPane writerFlowPane;
    @FXML private FlowPane actorFlowPane;
    @FXML private ScrollPane infoScrollPane;
    @FXML private Rating rating;
    @FXML private FlowPane genreFlowPane;
    @FXML private FlowPane tagsFlowPane;
    @FXML private Label ratingLabel;
    @FXML private Label tagsLabel;
    @FXML private Label actLabel;
    @FXML private Label writLabel;
    @FXML private Label episodeTitleLabel;
    @FXML private JFXButton playAllEpisodesButton;
    //other variables
    private MediaItem mediaItem;
    private String videoLink;
	private JFXDialog personViewDialog;
	private List<JFXPersonRippler> actorTiles = new ArrayList<>();
	private List<JFXPersonRippler> directorTiles = new ArrayList<>();
	private List<JFXPersonRippler> writerTiles = new ArrayList<>();
	private TileAnimator tileAnimator = new TileAnimator();
    
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		super.initialize(url, rb);
		JFXScrollPane.smoothScrolling(infoScrollPane);
		int numDirectorsAllowed = 5;
		for (int i = 0; i < numDirectorsAllowed; ++i) {
			directorTiles.add(JFXPersonRippler.createBasicRippler());
		}
		for (int i = 0; i < numWritersAllowed; ++i) {
			writerTiles.add(JFXPersonRippler.createBasicRippler());
		}
		int numActorsAllowed = 15;
		for (int i = 0; i < numActorsAllowed; ++i) {
			actorTiles.add(JFXPersonRippler.createBasicRippler());
		}

		rating.setOnMouseClicked(event -> {
			if (mediaItem!=null) {
				mediaItem.rating = rating.getRating();
				if (!rating.getStyleClass().contains("loaded")) {
					rating.getStyleClass().add("loaded");
				}
			}
		});
		seasonComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && newValue != oldValue) {
				int season = Integer.parseInt(newValue);
				mediaItem.tvShow.lastViewedSeason = season;
				episodeComboBox.getItems().clear();
				for (Integer i : mediaItem.tvShow.getOwnedEpisodeNumbers(season)) {
					episodeComboBox.getItems().add(i.toString());
				}
				//select first available episode on season change
				episodeComboBox.getSelectionModel().select(0);
				startTask();
			}
		});
		episodeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && newValue != oldValue) {
				mediaItem.tvShow.lastViewedEpisode= Integer.parseInt(newValue);
				startTask();
			}
		});
		infoScrollPane.prefWidthProperty().bind(mainGrid.widthProperty().multiply(0.64));
		infoScrollPane.prefHeightProperty().bind(mainGrid.heightProperty().multiply(0.70));
		directorFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		actorFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		writerFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		genreFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		tagsFlowPane.prefWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		posterImageView.fitHeightProperty().bind(mainGrid.heightProperty().multiply(0.83));
		posterImageView.fitWidthProperty().bind(mainGrid.widthProperty().multiply(0.30));
		descLabel.wrappingWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		tileAnimator.observe(genreFlowPane.getChildren());
		tileAnimator.observe(directorFlowPane.getChildren());
		tileAnimator.observe(actorFlowPane.getChildren());
		tileAnimator.observe(writerFlowPane.getChildren());
		tileAnimator.observe(tagsFlowPane.getChildren());
		playAllEpisodesButton.managedProperty().bindBidirectional(playAllEpisodesButton.visibleProperty());
	}
	
	public void showMediaItem(JFXDialog d, MediaItem mi) {
		super.setDialogLink(d, !mi.hasLoaded());
		mediaItem = mi;
		tvTitleGridPane.setVisible(!mediaItem.isMovie());
		movieTitleGridPane.setVisible(mediaItem.isMovie());
		playAllEpisodesButton.setVisible(!mediaItem.isMovie());
		if (!mi.isMovie() ) {
			seasonComboBox.getItems().clear();
			for (Integer i : mediaItem.tvShow.getOwnedSeasonNumbers()) {
				seasonComboBox.getItems().add(i.toString());
			}
			seasonComboBox.getSelectionModel().select(String.valueOf(mediaItem.tvShow.lastViewedSeason));
			episodeComboBox.getSelectionModel().select(String.valueOf(mediaItem.tvShow.lastViewedEpisode));
		}
		if (personViewDialog == null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("PersonViewContent.fxml"));
				GridPane personView = loader.load();
				PersonViewController personViewController = loader.getController();
				personViewDialog = new JFXDialog(ControllerMaster.mainController.getBackgroundStackPane(), personView,
						JFXDialog.DialogTransition.CENTER);
				personView.prefWidthProperty().bind(ControllerMaster.mainController.mainGrid.widthProperty().divide(1.35));
				personView.prefHeightProperty().bind(ControllerMaster.mainController.mainGrid.heightProperty().divide(1.15));
				JFXPersonRippler.setStaticVariables(personViewController, personViewDialog);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dLink.show();
	}
	
	@Override
	protected void runTasks() {
		super.runTasks();
		fillMainInfo();
		fillInfo();	
	}
	
	@Override
	protected void successTasks() {
		super.successTasks();
		fillControlls();
		mediaItem.setLoaded();
	}
	
	public void fillControlls() {
		for (JFXPersonRippler<?> rip : actorTiles) {
			rip.updateImage();
		}
		for (JFXPersonRippler<?> rip : writerTiles) {
			rip.updateImage();
		}
		for (JFXPersonRippler<?> rip : directorTiles) {
			rip.updateImage();
		}
		
	}
	
	//does any info not specific to episode
	public void fillMainInfo() {
		infoScrollPane.setVvalue(0);
		String releaseDate = "";
    	if (mediaItem.getReleaseDate() != null && mediaItem.getReleaseDate().length()>3) {
    		releaseDate = " (" + mediaItem.getReleaseDate().substring(0, 4) + ")";
    	} else {
    		releaseDate = " (N/A)";
		}
		if (mediaItem.isMovie()) {
			movieTitleLabel.setText(mediaItem.getTitle()+ releaseDate);  
		} else {
			tvTitleLabel.setText(mediaItem.getTitle(false) + ": ");
			episodeTitleLabel.setText(mediaItem.getTitle() + releaseDate);
		}
		genreLabel.setText( (mediaItem.getGenres().size()>1)? "Genres:" : "Genre:" );
		genreFlowPane.getChildren().clear();
		for (int i = 0; i < mediaItem.getGenres().size(); ++i) {
			genreFlowPane.getChildren().add(JFXCustomChips.getGenreChip(mediaItem.getGenres().get(i)));
		}
		genreLabel.setVisible(genreFlowPane.getChildren().size()>0);
		if (mediaItem.rating==-1) {
			rating.getStyleClass().remove("loaded");
			rating.setRating(mediaItem.getVoteAverage()/10*5);
		} else {
			rating.setRating(mediaItem.rating);
			if (!rating.getStyleClass().contains("loaded")) {
				rating.getStyleClass().add("loaded");
			}
		}
		
		tagsLabel.setText( (mediaItem.getKeywords().size()>1)? "Tags:" : "Tag:" );
		tagsFlowPane.getChildren().clear();
		for (int i = 0; i < mediaItem.getKeywords().size(); ++i) {
			tagsFlowPane.getChildren().add(JFXCustomChips.getTagChip(mediaItem.getKeywords().get(i).getName()));
		}
		tagsLabel.setVisible(tagsFlowPane.getChildren().size()>0);
		
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
		posterImageView.setImage(MediaSearchHandler.getItemPoster(mediaItem, 500).getImage());
		posterImageView.setEffect(new DropShadow());
	}
	
	@SuppressWarnings("unchecked")
	public void fillInfo() {		
		int dirCount = 0;
		int writCount = 0;
		ObservableList<Node> workingDirectorCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingWriterCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingActorCollection = FXCollections.observableArrayList();
		PersonCrew pCrew;
		List<Integer> writerIds = new ArrayList<>();
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
		actorFlowPane.getChildren().setAll(workingActorCollection);
		writerFlowPane.getChildren().setAll(workingWriterCollection);		
		dirLabel.setVisible(directorFlowPane.getChildren().size()>0);
		actLabel.setVisible(actorFlowPane.getChildren().size()>0);
		writLabel.setVisible(writerFlowPane.getChildren().size()>0);
		if (mediaItem.getVideos().size()>0) {
			videoLink = mediaItem.getVideos().get(0).getKey();
		} else {
			videoLink = null;
		}
		descLabel.setText("\t"+ mediaItem.getOverview());		
		
		//use runtime 
		String runtimeString = (mediaItem.isMovie())? "Runtime: " : "Episode Avg: ";
		runTimeLabel.setText(runtimeString + String.format("%d", mediaItem.getRuntime()/60) + ":" + String.format("%02d", mediaItem.getRuntime()%60));
		Platform.runLater(() -> directorFlowPane.requestLayout());
		Platform.runLater(() -> writerFlowPane.requestLayout());
		Platform.runLater(() -> actorFlowPane.requestLayout());		
	}
	
	@FXML public void playMedia() {
		openFile(mediaItem.getFullFilePath());
	}

	//create a m3u playlist of all path files, then play
	//adds episodes before after the last episode so it plays in a loop starting with your chosen episode
	@FXML public void playShow() {
		List<String> previousFilePaths = new ArrayList<>();
		List<String> filePaths = new ArrayList<>();
		String tempPath = "";
		for (int i = 1; i <= mediaItem.getNumSeasons(); ++i) {
			for (int j = 1; j < mediaItem.getEpisodes(i).size(); ++j) {
				tempPath = mediaItem.getFullFilePath(i, j);
				//skip empty paths
				if (!tempPath.isEmpty()) {
					if (i <= Integer.parseInt(seasonComboBox.getValue()) && j < Integer.parseInt(episodeComboBox.getValue())) {
						previousFilePaths.add(tempPath);
					} else {
						filePaths.add(tempPath);
					}
				}
			}
		}
		filePaths.addAll(previousFilePaths);
		File file = M3UBuilder.buildFile(filePaths);
		if (file != null) {
			openFile(file.getAbsolutePath());
		}
	}

	private void openFile(String path) {
		if (Desktop.isDesktopSupported()) {
			try {
				File file = new File(path);
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("The filepath " + path + " is invalid.");
			}
		} else {
			System.out.println("Unsupported OS, please post this bug and your OS at github");
		}
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
