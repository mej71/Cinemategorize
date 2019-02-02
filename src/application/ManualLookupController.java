package application;

import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.validation.RequiredFieldValidator;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;


public class ManualLookupController extends LoadingControllerBase implements Initializable {

	@FXML private JFXTextField titleField;
	@FXML private JFXNumberTextField yearField;
	@FXML private JFXComboBox<MediaTypeOptions> mediaTypeComboBox;
	@FXML private ScrollPane fileScrollPane;
	@FXML private ListFlowPane<FileCell<MediaItem>, MediaItem> fileFlowPane;
	@FXML private ScrollPane resultsScrollPane;
	@FXML private ListFlowPane<ResultCell<ResultsMediaItem>, ResultsMediaItem> resultsFlowPane;
	@FXML private Label noResultsLabel;
	
	private LinkedHashMap<MediaItem, MediaResultsPage> mediaList;
	private int oldId = 0;
	private int oldSeason = 0;
	private int oldEpisode = 0;

    @Override
	public void initialize(URL url, ResourceBundle rb) {
		mediaTypeComboBox.setItems( FXCollections.observableArrayList( MediaTypeOptions.values()));
		mediaTypeComboBox.setValue(MediaTypeOptions.MOVIE);
		
		fileFlowPane.hasChanged.addListener((observable, oldValue, newValue) -> {
			if (oldValue != newValue && newValue != null && newValue) {
				fileFlowPane.setChanged(false);
				resultsFlowPane.getChildren().clear();
				MediaItem item = fileFlowPane.selectedCell.getItem();
				if (mediaList.get(item) != null) {
					resultsFlowPane.getChildren().addAll(ResultCell.createCells(mediaList.get(item).getResults(), resultsFlowPane));
					resultsFlowPane.setPrefHeight(resultsFlowPane.getChildren().size() * ResultCell.prefCellHeight);
				}

				noResultsLabel.setVisible(resultsFlowPane.getChildren().size() == 0);
			}
		});
		
		RequiredFieldValidator titleValidator = new RequiredFieldValidator();
		titleValidator.setMessage("Title Required");
      
        titleField.getValidators().add(titleValidator);
        titleField.focusedProperty().addListener((o, oldVal, newVal) -> {
        	if (!newVal) {
        		titleField.validate();
        	}
        });
        titleField.textProperty().addListener((o, oldVal, newVal) -> titleField.validate());
       
        yearField.setMaxChars(4);
        fileFlowPane.bindWidthToNode(fileScrollPane);
        resultsFlowPane.bindWidthToNode(resultsScrollPane);
        noResultsLabel.setVisible(false);
	}

	public void setData(LinkedHashMap<MediaItem, MediaResultsPage> mList) {
		mediaList = mList;
		fileFlowPane.getChildren().clear();
		fileFlowPane.getChildren().addAll(FileCell.createCells(mediaList.keySet(), fileFlowPane));
		fileFlowPane.setPrefHeight(fileFlowPane.getChildren().size() * ResultCell.prefCellHeight);
		fileFlowPane.selectCell((FileCell<MediaItem>)fileFlowPane.getChildren().get(0));
	}
	
	@Override 
	protected void runTasks() {
		super.runTasks();
		resetValidations();
	}

	void openDialog(JFXDialog dLink) {
    	openDialog(dLink, 0, 0, 0);
	}

	public void openDialog(JFXDialog dLink, int oi, int seasonNum, int epNum) {
		setDialogLink(dLink, false);
		oldId = oi;
		oldSeason = seasonNum;
		oldEpisode = epNum;
		dLink.setOnDialogClosed(event -> {
			if (ControllerMaster.userData.numMediaItems() > 0) {
				ControllerMaster.mainController.addMediaWindow.close();
			}
			//remove manual edits
			if (oldId != 0) {
				MediaItem mi;
				if (oldSeason != 0) { //
					mi = ControllerMaster.userData.getTvById(oldId);
				} else {
					mi = ControllerMaster.userData.getMovieById(oldId);
				}
				boolean miIsMovie = mi.isMovie();
				int miId = mi.getId();
				for (MediaItem mik : ControllerMaster.userData.tempManualItems.keySet()) {
					if (mi.equals(mik)) {
						ControllerMaster.userData.tempManualItems.remove(mik);
						break;
					}
				}

			}
		});
		dLink.show();
	}
	
	public void resetValidations() {
		titleField.resetValidation();
	}
	
	enum MediaTypeOptions {
		MOVIE("Movie"),
		TV_SHOW("TV Series");
		
		private final String toString;
		
		MediaTypeOptions(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return this.toString;
		}
	}
	
	public void confirmMediaItem() {
		JFXDialogLayout confirmLayout = new JFXDialogLayout();
		String releaseDate = "";
    	if (resultsFlowPane.selectedCell.getItem().getReleaseDate(false) != null && resultsFlowPane.selectedCell.getItem().getReleaseDate(false).length()>3) {
    		releaseDate = " (" + resultsFlowPane.selectedCell.getItem().getReleaseDate(false).substring(0, 4) + ")";
    	} else {
    		releaseDate += " (N/A)";
		}
		String text = "The file\n" + fileFlowPane.selectedCell.getItem().getFullFilePath() + "\nis " + 
				resultsFlowPane.selectedCell.getItem().getTitle(false) + releaseDate;
		if (!resultsFlowPane.selectedCell.getItem().isMovie()) {
			text += ": Season " + resultsFlowPane.selectedCell.getSeason()  + " Episode " + resultsFlowPane.selectedCell.getEpisode();
		} 
		confirmLayout.setBody(new Label(text + "?"));
		JFXDialog confirmDialog = new JFXDialog();
		confirmDialog.setDialogContainer(ControllerMaster.mainController.getBackgroundStackPane());
		confirmDialog.setContent(confirmLayout);
		confirmDialog.setTransitionType(DialogTransition.CENTER);
		JFXButton confirmButton = new JFXButton("Confirm");
		JFXButton cancelButton = new JFXButton("Cancel");
		confirmButton.setOnAction(event -> {
			confirmDialog.close();
			addMediaItem();

		});
		cancelButton.setOnAction(event -> confirmDialog.close());
		confirmLayout.setActions(confirmButton, cancelButton);
		confirmDialog.show();
	}

	public void informAlreadyOwns() {
		JFXDialogLayout confirmLayout = new JFXDialogLayout();
		String releaseDate = "";
		if (resultsFlowPane.selectedCell.getItem().getReleaseDate(false) != null && resultsFlowPane.selectedCell.getItem().getReleaseDate(false).length()>3) {
			releaseDate = " (" + resultsFlowPane.selectedCell.getItem().getReleaseDate(false).substring(0, 4) + ")";
		} else {
			releaseDate += " (N/A)";
		}
		String text = "The file item " +
				resultsFlowPane.selectedCell.getItem().getTitle(false) + releaseDate;
		if (!resultsFlowPane.selectedCell.getItem().isMovie()) {
			text += ": Season " + resultsFlowPane.selectedCell.getSeason()  + " Episode " + resultsFlowPane.selectedCell.getEpisode();
		}
		confirmLayout.setBody(new Label(text + " is already owned.\nPlease select a different item or change that one first"));
		JFXDialog confirmDialog = new JFXDialog();
		confirmDialog.setDialogContainer(ControllerMaster.mainController.getBackgroundStackPane());
		confirmDialog.setContent(confirmLayout);
		confirmDialog.setTransitionType(DialogTransition.CENTER);
		JFXButton okayButton = new JFXButton("Okay");
		okayButton.setOnAction(event -> {
			confirmDialog.close();
		});
		confirmLayout.setActions(okayButton);
		confirmDialog.show();
	}
	
	@FXML
	public void addMediaItem() {
		MediaItem fileItem = fileFlowPane.selectedCell.getItem();
		ResultsMediaItem resultItem = resultsFlowPane.selectedCell.getItem();

		//if editing manually, remove old item
		MediaItem mi;
		if (oldId != 0) {
			if (oldSeason != 0) { //
				mi = ControllerMaster.userData.getTvById(oldId);
			} else {
				mi = ControllerMaster.userData.getMovieById(oldId);
			}
			if (mi.isMovie()) {
				ControllerMaster.userData.removeMedia(mi);
			} else {
				ControllerMaster.userData.removeTvEpisode(mi, oldSeason, oldEpisode);
			}
		}
		//set choice to proper media result
		if (resultItem.isMovie()) {
			fileItem.setMovie(resultItem.cMovie);
		} else {
			fileItem.setTvShow(resultItem.tvShow);
		}

		if (fileItem.isMovie()) {
			UserDataHelper.addMovie( fileItem.cMovie, new File(fileItem.getTempFilePath()) );
		} else {
			TvEpisode episode = MediaSearchHandler.getEpisodeInfo(fileItem.getId(), resultItem.getTempSeasonNum(), 
					resultItem.getTempEpisodeNum());
			UserDataHelper.addTvShow(fileItem.tvShow, episode.getSeasonNumber(), episode.getEpisodeNumber(), new File(fileItem.getTempFilePath()));
		}
		
		//cleanup
		for (MediaItem mik : ControllerMaster.userData.tempManualItems.keySet()) {
			if (mik.getTempFilePath().equals(fileItem.getTempFilePath())) {
				ControllerMaster.userData.tempManualItems.remove(mik);
				break;
			}
		}
		fileFlowPane.getChildren().remove(fileFlowPane.selectedCell);
		
		//refresh master view with new file
		ControllerMaster.mainController.refreshSearch();
		
		//close manual dialog if empty
		if (fileFlowPane.getChildren().size()==0) {
			dLink.close();
			//force refresh after manual update
			if (oldId != 0) {
				if (!fileItem.isMovie()) { //
					mi = ControllerMaster.userData.getTvById(fileItem.getId());
				} else {
					mi = ControllerMaster.userData.getMovieById(fileItem.getId());
				}
				ControllerMaster.mainController.showSelectionDialog(mi);
			}
		} else {
			fileFlowPane.selectedCell = (FileCell<MediaItem>)fileFlowPane.getChildren().get(0);
			fileFlowPane.setChanged(true);
		}
	}
	
	@FXML public void searchSuggestion() {
		showLoadingPane();
        Task<Object> searchTask = new Task<Object>() {

            @Override
            protected Object call()  {
                searchSuggestionLookup();
                succeeded();
                return null;
            }
        };
		searchTask.setOnSucceeded(e -> successTasks());
		searchTask.run();
	}
	
	private void searchSuggestionLookup() {
		MediaResultsPage mRes = null;
		String title = titleField.getText();
		int year = (yearField.getText().isEmpty())? 0 : Integer.parseInt(yearField.getText());
		if ( title.isEmpty() ) {
			resetValidations();
			return;
		}
		if (mediaTypeComboBox.getValue()==MediaTypeOptions.MOVIE) {
			mRes = new MediaResultsPage(MediaSearchHandler.getMovieResults(title, year));	
		} else if (mediaTypeComboBox.getValue()==MediaTypeOptions.TV_SHOW) { //tv show
			mRes = new MediaResultsPage(MediaSearchHandler.getTvResults(title));
		}
		mediaList.get(fileFlowPane.selectedCell.getItem()).setResults(mRes);
		Platform.runLater(() -> {
			resultsFlowPane.getChildren().clear();
			resultsFlowPane.getChildren().addAll(ResultCell.createCells(mediaList.get(fileFlowPane.selectedCell.getItem()).getResults(), resultsFlowPane));
			resultsFlowPane.setPrefHeight(resultsFlowPane.getChildren().size() * ResultCell.prefCellHeight);
			noResultsLabel.setVisible(resultsFlowPane.getChildren().size() == 0);
		});
		
	}
}
