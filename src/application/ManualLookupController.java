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
import com.jfoenix.controls.JFXListView;
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


public class ManualLookupController implements Initializable {
	
	@FXML private JFXListView<MediaItem> fileListView;
	@FXML private JFXListView<ResultsMediaItem> resultsListView;
	@FXML private JFXButton confirmButton;
	@FXML private JFXButton searchButton;
	@FXML private JFXTextField titleField;
	@FXML private JFXNumberTextField yearField;
	@FXML private JFXComboBox<MediaTypeOptions> mediaTypeComboBox;
	
	private JFXDialog dialogLink;
	private boolean isSmoothScrolling = false;
	private LinkedHashMap<MediaItem, MediaResultsPage> mediaList;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		mediaTypeComboBox.setItems( FXCollections.observableArrayList( MediaTypeOptions.values()));
		mediaTypeComboBox.setValue(MediaTypeOptions.MOVIE);
		//init columns info
		fileListView.setCellFactory(param -> new FileCell<MediaItem>());
		resultsListView.setCellFactory(param -> new ResultCell<ResultsMediaItem>());
		fileListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MediaItem>() {

		    @Override
		    public void changed(ObservableValue<? extends MediaItem> observable, MediaItem oldValue, MediaItem newValue) {
		    	if (oldValue != newValue && newValue != null) {
			    	resultsListView.getItems().clear();
			    	if (mediaList.get(newValue) != null) {
			    		resultsListView.getItems().setAll( mediaList.get(newValue).getResults());
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
	}
	
	public void setData(LinkedHashMap<MediaItem, MediaResultsPage> mList) {
		mediaList = mList;
		fileListView.getItems().clear();
		fileListView.getItems().addAll(mediaList.keySet());
		fileListView.getSelectionModel().select(0);
	}
	
	public void resetValidations() {
		titleField.resetValidation();
	}

	public void openDialog(JFXDialog dLink) {
		if (!isSmoothScrolling) {
			JFXSmoothScroll.smoothScrollingListView(fileListView, 0.1);
			isSmoothScrolling = true;
		}
		resetValidations();
		dialogLink = dLink;
		dialogLink.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {

			@Override
			public void handle(JFXDialogEvent event) {
				if (ControllerMaster.userData.numMediaItems() > 0) {
					ControllerMaster.mainController.addMediaWindow.close();;
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

		//set choice to proper media result
		if (resultsListView.getSelectionModel().getSelectedItem().isMovie()) {
			fileListView.getSelectionModel().getSelectedItem().setMovie(resultsListView.getSelectionModel().getSelectedItem().cMovie);
		} else {
			fileListView.getSelectionModel().getSelectedItem().setTvShow(resultsListView.getSelectionModel().getSelectedItem().tvShow);
		}
		
		//add file to master list
		MediaItem item = fileListView.getSelectionModel().getSelectedItem();
		if (item.isMovie()) {
			ControllerMaster.userData.addMovie( item.cMovie, new File(item.fullFilePath) );
		} else {
			TvEpisode episode = MediaSearchHandler.getEpisodeInfo(item.getId(), resultsListView.getSelectionModel().getSelectedItem().getTempSeasonNum(), 
					resultsListView.getSelectionModel().getSelectedItem().getTempEpisodeNum());
			ControllerMaster.userData.addTvShow(item.tvShow, episode, new File(item.fullFilePath));
		}
		
		//cleanup
		for (MediaItem mi : ControllerMaster.userData.tempManualItems.keySet()) {
			if (mi.fullFilePath.equals(item.fullFilePath)) {
				ControllerMaster.userData.tempManualItems.remove(mi);
				break;
			}
		}
		fileListView.getItems().remove(fileListView.getSelectionModel().getSelectedItem());
		
		//refresh master view with new file
		if (!ControllerMaster.mainController.searchField.getText().isEmpty() && ControllerMaster.mainController.autoEvent!=null ) {
			ControllerMaster.userData.refreshViewingList(ControllerMaster.mainController.autoEvent.getObject().getTargetIDs(), false);			
		} else {
			ControllerMaster.userData.refreshViewingList(null, true);
		}
		
		//close manual dialog if empty
		if (fileListView.getItems().size()==0) {
			dialogLink.close();
		} else {
			fileListView.getSelectionModel().select(0);
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
		mediaList.get(fileListView.getSelectionModel().getSelectedItem()).setResults(mRes);
		resultsListView.getItems().clear();
		resultsListView.getItems().setAll( mediaList.get(fileListView.getSelectionModel().getSelectedItem()).getResults());		
	}
}
