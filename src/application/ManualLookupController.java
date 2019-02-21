package application;

import application.flowcells.FileCell;
import application.flowcells.ListFlowPane;
import application.flowcells.ResultCell;
import application.controls.LoadingControllerBase;
import application.mediainfo.MediaItem;
import application.mediainfo.MediaResultsPage;
import application.mediainfo.ResultsMediaItem;
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
				if (fileFlowPane.getSelectedCell() != null) {
					resultsFlowPane.clearCells();
					resultsFlowPane.addCells(fileFlowPane.getSelectedCell().resultCells);
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
		fileFlowPane.clearCells();
		FileCell cell;
		for (MediaItem mi : mList.keySet()) {
			cell = new FileCell(mi);
			cell.mediaResultsPage = mList.get(mi);
			cell.resultCells = ResultCell.createCells(cell.mediaResultsPage.getResults());
			fileFlowPane.addCell(cell);
		}
		fileFlowPane.setPrefHeight(fileFlowPane.getChildren().size() * FileCell.prefCellHeight);
		fileFlowPane.selectCell(0);
	}
	
	@Override 
	protected void runTasks() {
		super.runTasks();
		resetValidations();
	}

	public void openDialog(JFXDialog dLink, int oi, int seasonNum, int epNum) {
		setDialogLink(dLink, false);
		oldId = oi;
		oldSeason = seasonNum;
		oldEpisode = epNum;
		dLink.setOnDialogClosed(event -> {
			if (ControllerMaster.userData.numMediaItems() > 0) {
				ControllerMaster.closeAddMediaWindow();
			}
			//remove manual edits
			if (oldId != 0) {
				MediaItem mi;
				if (oldSeason != 0) { //
					mi = ControllerMaster.userData.getTvById(oldId);
				} else {
					mi = ControllerMaster.userData.getMovieById(oldId);
				}
				ControllerMaster.userData.removeTempManualItem(mi);
			}
		});
		dLink.show();
	}
	
	private void resetValidations() {
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
    	if (resultsFlowPane.getSelectedItem().getReleaseDate(false) != null && resultsFlowPane.getSelectedItem().getReleaseDate(false).length()>3) {
    		releaseDate = " (" + resultsFlowPane.getSelectedItem().getReleaseDate(false).substring(0, 4) + ")";
    	} else {
    		releaseDate += " (N/A)";
		}
		String text = "The file\n" + fileFlowPane.getSelectedItem().getFullFilePath() + "\nis " +
				resultsFlowPane.getSelectedItem().getTitle(false) + releaseDate;
		if (!resultsFlowPane.getSelectedItem().isMovie()) {
			text += ": Season " + resultsFlowPane.getSelectedCell().getSeason()  + " Episode " + resultsFlowPane.getSelectedCell().getEpisode();
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
		if (resultsFlowPane.getSelectedItem().getReleaseDate(false) != null && resultsFlowPane.getSelectedItem().getReleaseDate(false).length()>3) {
			releaseDate = " (" + resultsFlowPane.getSelectedItem().getReleaseDate(false).substring(0, 4) + ")";
		} else {
			releaseDate += " (N/A)";
		}
		String text = "The file item " +
				resultsFlowPane.getSelectedItem().getTitle(false) + releaseDate;
		if (!resultsFlowPane.getSelectedItem().isMovie()) {
			text += ": Season " + resultsFlowPane.getSelectedCell().getSeason()  + " Episode " + resultsFlowPane.getSelectedCell().getEpisode();
		}
		confirmLayout.setBody(new Label(text + " is already owned.\nPlease select a different item or change that one first"));
		JFXDialog confirmDialog = new JFXDialog();
		confirmDialog.setDialogContainer(ControllerMaster.mainController.getBackgroundStackPane());
		confirmDialog.setContent(confirmLayout);
		confirmDialog.setTransitionType(DialogTransition.CENTER);
		JFXButton okayButton = new JFXButton("Okay");
		okayButton.setOnAction(event -> confirmDialog.close());
		confirmLayout.setActions(okayButton);
		confirmDialog.show();
	}
	
	@FXML
	private void addMediaItem() {
		MediaItem fileItem = fileFlowPane.getSelectedItem();
		ResultsMediaItem resultItem = resultsFlowPane.getSelectedItem();

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
		ControllerMaster.userData.removeTempManualItem(fileItem.getTempFilePath());

		fileFlowPane.removeCell(fileFlowPane.getSelectedCell());
		fileFlowPane.setPrefHeight(fileFlowPane.getChildren().size() * FileCell.prefCellHeight);
		
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
				ControllerMaster.showSelectionDialog(mi);
			}
		} else {
			fileFlowPane.selectCell(0);
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
		fileFlowPane.getSelectedCell().mediaResultsPage.setResults(mRes);
		fileFlowPane.getSelectedCell().resultCells = ResultCell.createCells(mRes.getResults());
		Platform.runLater(() -> {
			resultsFlowPane.clearCells();
			resultsFlowPane.addCells(fileFlowPane.getSelectedCell().resultCells);
			resultsFlowPane.setPrefHeight(resultsFlowPane.getChildren().size() * ResultCell.prefCellHeight);
			noResultsLabel.setVisible(resultsFlowPane.getChildren().size() == 0);
		});
		
	}
}
