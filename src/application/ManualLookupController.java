package application;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.validation.RequiredFieldValidator;

import info.movito.themoviedbapi.model.tv.TvEpisode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;


public class ManualLookupController implements Initializable {
	
	@FXML private JFXButton confirmButton;
	@FXML private JFXButton searchButton;
	@FXML private JFXTextField titleField;
	@FXML private JFXNumberTextField yearField;
	@FXML private JFXComboBox<MediaTypeOptions> mediaTypeComboBox;
	@FXML private ScrollPane fileScrollPane;
	@FXML private ListFlowPane<FileCell<MediaItem>, MediaItem> fileFlowPane;
	@FXML private ScrollPane resultsScrollPane;
	@FXML private ListFlowPane<ResultCell<ResultsMediaItem>, ResultsMediaItem> resultsFlowPane;
	
	
	private JFXDialog dialogLink;
	private LinkedHashMap<MediaItem, MediaResultsPage> mediaList;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		mediaTypeComboBox.setItems( FXCollections.observableArrayList( MediaTypeOptions.values()));
		mediaTypeComboBox.setValue(MediaTypeOptions.MOVIE);
		//init columns info
		
		fileFlowPane.hasChanged.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue != newValue && newValue != null && newValue) {
					fileFlowPane.setChanged(false);
		    		resultsFlowPane.getChildren().clear();
		    		MediaItem item = fileFlowPane.selectedCell.getItem();
			    	if (mediaList.get(item) != null) {
			    		resultsFlowPane.getChildren().addAll(ResultCell.createCells(mediaList.get(item).getResults(), resultsFlowPane));
			    		resultsFlowPane.setPrefHeight(resultsFlowPane.getChildren().size() * ResultCell.prefCellHeight);
			    	}
		    	}				
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
        titleField.textProperty().addListener((o, oldVal, newVal) -> {
        	titleField.validate();
        });
       
        yearField.setMaxChars(4);
        fileFlowPane.bindWidthToNode(fileScrollPane);
        resultsFlowPane.bindWidthToNode(resultsScrollPane);
	}
	
	public void setData(LinkedHashMap<MediaItem, MediaResultsPage> mList) {
		mediaList = mList;
		fileFlowPane.getChildren().clear();
		fileFlowPane.getChildren().addAll(FileCell.createCells(mediaList.keySet(), fileFlowPane));
		fileFlowPane.setPrefHeight(fileFlowPane.getChildren().size() * ResultCell.prefCellHeight);
		fileFlowPane.selectCell((FileCell<MediaItem>)fileFlowPane.getChildren().get(0));
	}
	
	public void resetValidations() {
		titleField.resetValidation();
	}

	public void openDialog(JFXDialog dLink) {
		resetValidations();
		dialogLink = dLink;
		dialogLink.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {

			@Override
			public void handle(JFXDialogEvent event) {
				if (ControllerMaster.userData.numMediaItems() > 0) {
					ControllerMaster.mainController.addMediaWindow.close();
				}
			}
			
		});
		dialogLink.show();  
	}
	
	enum MediaTypeOptions {
		MOVIE("Movie"),
		TV_SHOW("TV Series");
		
		private final String toString;
		
		private MediaTypeOptions(String toString) {
			this.toString = toString;
		}
		
		@Override
		public String toString() {
			return this.toString;
		}
	}
	
	public void confirmMediaItem() {
		JFXDialogLayout confirmLayout = new JFXDialogLayout();
		confirmLayout.setBody(new Label("Are you sure this is the right choice?"));
		JFXDialog confirmDialog = new JFXDialog();
		confirmDialog.setDialogContainer(ControllerMaster.mainController.getBackgroundStackPane());
		confirmDialog.setContent(confirmLayout);
		confirmDialog.setTransitionType(DialogTransition.CENTER);
		JFXButton confirmButton = new JFXButton("Confirm");
		JFXButton cancelButton = new JFXButton("Cancel");
		confirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				confirmDialog.close();
				addMediaItem();
				
			}        	
        });
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				confirmDialog.close();
				
			}        	
        });
		confirmLayout.setActions(confirmButton, cancelButton);
		confirmDialog.show();
	}
	
	@FXML
	public void addMediaItem() {

		MediaItem fileItem = fileFlowPane.selectedCell.getItem();
		ResultsMediaItem resultItem = resultsFlowPane.selectedCell.getItem();
		//set choice to proper media result
		if (resultItem.isMovie()) {
			fileItem.setMovie(resultItem.cMovie);
		} else {
			fileItem.setTvShow(resultItem.tvShow);
		}
		
		if (fileItem.isMovie()) {
			ControllerMaster.userData.addMovie( fileItem.cMovie, new File(fileItem.fullFilePath) );
		} else {
			TvEpisode episode = MediaSearchHandler.getEpisodeInfo(fileItem.getId(), resultItem.getTempSeasonNum(), 
					resultItem.getTempEpisodeNum());
			ControllerMaster.userData.addTvShow(fileItem.tvShow, episode, new File(fileItem.fullFilePath));
		}
		
		//cleanup
		for (MediaItem mi : ControllerMaster.userData.tempManualItems.keySet()) {
			if (mi.fullFilePath.equals(fileItem.fullFilePath)) {
				ControllerMaster.userData.tempManualItems.remove(mi);
				break;
			}
		}
		fileFlowPane.getChildren().remove(fileFlowPane.selectedCell);
		
		//refresh master view with new file
		if (!ControllerMaster.mainController.searchField.getText().isEmpty() && ControllerMaster.mainController.autoEvent!=null ) {
			ControllerMaster.userData.refreshViewingList(ControllerMaster.mainController.autoEvent.getObject().getTargetIDs(), false);			
		} else {
			ControllerMaster.userData.refreshViewingList(null, true);
		}
		
		//close manual dialog if empty
		if (fileFlowPane.getChildren().size()==0) {
			dialogLink.close();
		} else {
			fileFlowPane.selectedCell = (FileCell<MediaItem>)fileFlowPane.getChildren().get(0);
			fileFlowPane.setChanged(true);
		}
	}
	
	@FXML
	public void searchSuggestion() {
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
		resultsFlowPane.getChildren().clear();
		resultsFlowPane.getChildren().addAll(ResultCell.createCells(mediaList.get(fileFlowPane.selectedCell.getItem()).getResults(), resultsFlowPane));
		resultsFlowPane.setPrefHeight(resultsFlowPane.getChildren().size() * ResultCell.prefCellHeight);
	}
}
