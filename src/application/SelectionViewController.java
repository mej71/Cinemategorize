package application;

import com.jfoenix.controls.*;
import info.movito.themoviedbapi.model.ContentRating;
import info.movito.themoviedbapi.model.ReleaseDate;
import info.movito.themoviedbapi.model.ReleaseInfo;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.controlsfx.control.Rating;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private JFXRippler optionsRippler;
    @FXML private StackPane imageStackPane;
    @FXML private Label starRatingLabel;
    //other variables
    private MediaItem mediaItem;
    private String videoLink;
	private JFXDialog personViewDialog;
	private List<JFXPersonRippler> actorTiles = new ArrayList<>();
	private List<JFXPersonRippler> directorTiles = new ArrayList<>();
	private List<JFXPersonRippler> writerTiles = new ArrayList<>();
	private JFXListView<SelectionOptions.SelectionOptionTitles> optionList;
	private boolean observing = false;
    
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
				rating.setRating(Math.max(0.0, rating.getRating()));
				rating.setRating(Math.min(5.0, rating.getRating()));
				mediaItem.rating = rating.getRating();
				if (!rating.getStyleClass().contains("loaded")) {
					rating.getStyleClass().add("loaded");
				}
				starRatingLabel.setText("My Rating: " + (Math.round(rating.getRating() * 100.0) / 100.0));
			}
		});
		seasonComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && !newValue.equals(oldValue)) {
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
			if (newValue != null && !newValue.equals(oldValue)) {
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
		posterImageView.fitHeightProperty().bind(imageStackPane.heightProperty());
		posterImageView.fitWidthProperty().bind(imageStackPane.widthProperty());
		descLabel.wrappingWidthProperty().bind(infoScrollPane.widthProperty().subtract(20));
		playAllEpisodesButton.managedProperty().bindBidirectional(playAllEpisodesButton.visibleProperty());
		optionList = new JFXListView<>();
        optionList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		JFXPopup optionsPopup = new JFXPopup(optionList);
		optionsRippler.setOnMouseClicked(e -> {
            //hack to make selection clear (bug in Javafx)
		    List<SelectionOptions.SelectionOptionTitles> options = FXCollections.observableArrayList(optionList.getItems());
            optionList.getItems().clear();
            optionList.getItems().setAll(options);
            optionsPopup.show(optionsRippler, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT);
		});
		optionList.setOnMouseClicked( e -> {
		    if (e.getTarget() != null) {
		        if (e.getTarget() instanceof JFXListCell) {
		            JFXListCell<SelectionOptions.SelectionOptionTitles> target = (JFXListCell)e.getTarget();
                    switch (target.getItem()) {
                        case ADDTOPLAYLIST:
                            System.out.println("a");
                            break;
                        case CHANGELOCATION:
							FileChooser chooser = new FileChooser();
							chooser.getExtensionFilters().add(AddMediaDialogController.extFilter);
							chooser.setTitle("Choose replacement file");
							File file = chooser.showOpenDialog(dLink.getScene().getWindow());
							if (file != null) {
								for (int j = 0; j < AddMediaDialogController.extFilter.getExtensions().size(); ++j) {
									if (file.getName().endsWith(AddMediaDialogController.extFilter.getExtensions().get(j).substring(1))) {
										if (mediaItem.isMovie()) {
											mediaItem.setFilePathInfo(new FilePathInfo(file.getAbsolutePath(), file.getName(), file.getParentFile().getName()));
										} else {
											mediaItem.tvShow.setFilePathInfo(new FilePathInfo(file.getAbsolutePath(), file.getName(), file.getParentFile().getName()), getSelectedSeason(), getSelectedEpisode());
										}
									}
								}
							}
							break;
                        case MANUALEDIT:
							MediaResultsPage mRes;
							if (mediaItem.isMovie()) {
								mRes = new MediaResultsPage(MediaSearchHandler.getMovieResults(mediaItem.getTitle(), 0));
							} else {
								mRes = new MediaResultsPage(MediaSearchHandler.getTvResults(mediaItem.getTitle(false)));
							}
							MediaItem tempItem = new MediaItem(mediaItem.tvShow, mediaItem.cMovie, mediaItem.getFullFilePath(), mediaItem.getFileName(), mediaItem.getFolder());
							ControllerMaster.userData.tempManualItems.put(tempItem, mRes);
                        	ControllerMaster.mainController.showManualLookupDialog(ControllerMaster.userData.tempManualItems, mediaItem.getId(), getSelectedSeason(), getSelectedEpisode());
                            break;
                        case REMOVEEPISODE:
							confirmDelete("Are you sure you want to delete " + mediaItem.getTitle() + "?\nYou cannot undo this action",true);
                            break;
                        case REMOVESHOW: //remove show and movie do the same thing, but different names for context
                        case REMOVEMOVIE:
							confirmDelete("Are you sure you want to delete " + mediaItem.getTitle(false) + "?\nYou cannot undo this action", false);
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
	}

	public void confirmDelete(String message, boolean episodeOnly) {
		JFXDialogLayout confirmLayout = new JFXDialogLayout();
		confirmLayout.setBody(new Label(message));
		JFXDialog confirmDialog = new JFXDialog();
		confirmDialog.setDialogContainer(ControllerMaster.mainController.getBackgroundStackPane());
		confirmDialog.setContent(confirmLayout);
		confirmDialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
		JFXButton confirmButton = new JFXButton("Confirm");
		confirmButton.getStyleClass().add("delete-button");
		JFXButton cancelButton = new JFXButton("Cancel");
		confirmButton.setOnAction(event -> {
			confirmDialog.close();
			boolean refreshDialog = false;
			if (episodeOnly) {
				refreshDialog = ControllerMaster.userData.removeTvEpisode(mediaItem, getSelectedSeason(), getSelectedEpisode());
			} else {
				ControllerMaster.userData.removeMedia(mediaItem);
			}
			//refresh selection view on change or close if removed entirely
			if (refreshDialog) { //
				updateComboBoxes();
			} else {
				dLink.close();
			}
		});
		cancelButton.setOnAction(event -> confirmDialog.close());
		confirmLayout.setActions(confirmButton, cancelButton);
		confirmDialog.show();
	}

	private int getSelectedSeason() {
		return (mediaItem.isTvShow())? Integer.parseInt(seasonComboBox.getValue()) : 0;
	}

	private int getSelectedEpisode() {
		return (mediaItem.isTvShow())? Integer.parseInt(episodeComboBox.getValue()) : 0;
	}

	void updateComboBoxes() {
		seasonComboBox.getItems().clear();
		for (Integer i : mediaItem.tvShow.getOwnedSeasonNumbers()) {
			seasonComboBox.getItems().add(i.toString());
		}
		seasonComboBox.getSelectionModel().select(String.valueOf(mediaItem.tvShow.lastViewedSeason));
		episodeComboBox.getSelectionModel().select(String.valueOf(mediaItem.tvShow.lastViewedEpisode));
	}

	void showMediaItem(JFXDialog d, MediaItem mi) {
		super.setDialogLink(d, !mi.hasLoaded());
		mediaItem = mi;
		tvTitleGridPane.setVisible(!mediaItem.isMovie());
		movieTitleGridPane.setVisible(mediaItem.isMovie());
		playAllEpisodesButton.setVisible(!mediaItem.isMovie());
        optionList.getItems().setAll( (mediaItem.isMovie())? SelectionOptions.getMovieOptions() : SelectionOptions.getTvOptions());
		if (!mediaItem.isMovie() ) {
			updateComboBoxes();
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
		observeNodes();
		fillControlls();
		mediaItem.setLoaded();
	}

	@Override
	protected void closeTasks() {
		super.closeTasks();
		unobserveNodes();
		System.gc();
	}

	private void observeNodes() {
		if (observing) {
			unobserveNodes();
		}
		ControllerMaster.tileAnimator.observe(genreFlowPane.getChildren());
		ControllerMaster.tileAnimator.observe(directorFlowPane.getChildren());
		ControllerMaster.tileAnimator.observe(actorFlowPane.getChildren());
		ControllerMaster.tileAnimator.observe(writerFlowPane.getChildren());
		ControllerMaster.tileAnimator.observe(tagsFlowPane.getChildren());
		observing = true;
	}

	private void unobserveNodes() {
		ControllerMaster.tileAnimator.unobserve(genreFlowPane.getChildren());
		ControllerMaster.tileAnimator.unobserve(directorFlowPane.getChildren());
		ControllerMaster.tileAnimator.unobserve(actorFlowPane.getChildren());
		ControllerMaster.tileAnimator.unobserve(writerFlowPane.getChildren());
		ControllerMaster.tileAnimator.unobserve(tagsFlowPane.getChildren());
		observing = false;
	}

	private void fillControlls() {
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
	private void fillMainInfo() {
		infoScrollPane.setVvalue(0);
		String releaseDate;
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
			starRatingLabel.setText("TMDB Avg Rating: " + (Math.round(rating.getRating() * 100.0) / 100.0));
		} else {
			rating.setRating(mediaItem.rating);
			if (!rating.getStyleClass().contains("loaded")) {
				rating.getStyleClass().add("loaded");
			}
			starRatingLabel.setText("My Rating: " + (Math.round(rating.getRating() * 100.0) / 100.0));
		}

		List<Keyword> keywords = mediaItem.getKeywords();
		tagsLabel.setText( (keywords.size()>1)? "Tags:" : "Tag:" );
		tagsFlowPane.getChildren().clear();
		for (int i = 0; i < keywords.size(); ++i) {
			tagsFlowPane.getChildren().add(JFXCustomChips.getTagChip(keywords.get(i).getName()));
		}
		tagsLabel.setVisible(tagsFlowPane.getChildren().size()>0);
		
		//movie ratings are stored in releases, tv in content rating
		boolean found = false;
		if (mediaItem.isMovie()) {
			List<ReleaseInfo> releases = mediaItem.getReleases();
			List<ReleaseDate> releaseDates;
			for (int i = 0; i < releases.size(); ++i) {
				if (releases.get(i).getCountry().equalsIgnoreCase("US")) {
					releaseDates = releases.get(i).getReleaseDates();
					for (int j = 0; j < releaseDates.size(); ++j) {
						if (releaseDates.get(j).getCertification()!=null && !releaseDates.get(j).getCertification().isEmpty()) {
							ratingLabel.setText(releaseDates.get(j).getCertification());
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
			List<ContentRating> contentRatings = mediaItem.getContentRating();
			ContentRating contentRating;
			for (int i = 0; i < contentRatings.size(); ++i) {
				contentRating = contentRatings.get(i);
				if (contentRating.getLocale().equalsIgnoreCase("US")) {
					found = true;
					ratingLabel.setText(contentRating.getRating());
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
	private void fillInfo() {
		int dirCount = 0;
		int writCount = 0;
		ObservableList<Node> workingDirectorCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingWriterCollection = FXCollections.observableArrayList();
		ObservableList<Node> workingActorCollection = FXCollections.observableArrayList();
		PersonCrew pCrew;
		List<Integer> writerIds = new ArrayList<>();
		List<PersonCrew> crew = mediaItem.getCrew();
		for (int i = 0; i < crew.size(); ++i) {
			pCrew = crew.get(i);
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

		List<PersonCast> cast = mediaItem.getCast();
		PersonCast pCast;
		for (int i = 0; i < cast.size() && i<13; ++i) {
			pCast = cast.get(i);
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
		Platform.runLater(() -> {
			directorFlowPane.requestLayout();
			writerFlowPane.requestLayout();
			actorFlowPane.requestLayout();
		});
	}
	
	@FXML public void playMedia() {
		openFile(mediaItem.getFullFilePath());
	}

	//create a m3u playlist of all path files, then play
	//adds episodes before after the last episode so it plays in a loop starting with your chosen episode
	@FXML public void playShow() {
		List<String> previousFilePaths = new ArrayList<>();
		List<String> filePaths = new ArrayList<>();
		String tempPath;
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
