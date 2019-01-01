package application;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class ManualLookupController implements Initializable {
	
	@FXML private JFXListView<MediaItem> fileListView;
	@FXML private JFXListView<ResultsMediaItem> resultsListView;
	@FXML private JFXButton confirmButton;
	@FXML private JFXButton searchButton;
	@FXML private JFXTextField titleField;
	@FXML private JFXNumberTextField yearField;
	@FXML private JFXComboBox<MediaTypeOptions> mediaTypeComboBox;
	
	private JFXDialog dialogLink;
	private JFXDialog addMovieDialog;
	private boolean isSmoothScrolling = false;
	private LinkedHashMap<MediaItem, MediaResultsPage> mediaList;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
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
        
        //double click on a result to select it
        resultsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.PRIMARY &&
                   (event.getTarget() instanceof Text || event.getTarget() instanceof Label || 
                		   (event.getTarget() instanceof GridPane && (((GridPane) event.getTarget()).getChildren().size() > 0)))) {

                   //confirm choice     
                 }    
            }
        });
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

	public void openDialog(JFXDialog dLink, JFXDialog aLink) {
		if (!isSmoothScrolling) {
			JFXSmoothScroll.smoothScrollingListView(fileListView, 0.1);
			isSmoothScrolling = true;
		}
		resetValidations();
		addMovieDialog = aLink;
		dialogLink = dLink;
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
	
	@FXML
	public void confirmMediaItem() {
		MediaItem item = fileListView.getSelectionModel().getSelectedItem();
		if (item.isMovie()) {
			ControllerMaster.userData.addMovie((CustomMovieDb)item.getItem(), new File(item.fullFilePath) );
			for (MediaItem mi : ControllerMaster.userData.tempManualItems.keySet()) {
				if (mi.fullFilePath.equals(item.fullFilePath)) {
					ControllerMaster.userData.tempManualItems.remove(mi);
					break;
				}
			}
		} //else add tv when supported
		
		fileListView.getItems().remove(item);
		if (!ControllerMaster.mainController.searchField.getText().isEmpty() && ControllerMaster.mainController.autoEvent!=null ) {
			ControllerMaster.userData.refreshViewingList(ControllerMaster.mainController.autoEvent.getObject().getTargetIDs(), false);			
		} else {
			ControllerMaster.userData.refreshViewingList(null, true);
		}
		if (fileListView.getItems().size()==0) {
			dialogLink.close();
			if (addMovieDialog!=null) {
				addMovieDialog.close();
			}
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
