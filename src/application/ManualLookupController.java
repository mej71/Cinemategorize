package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.validation.RequiredFieldValidator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
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
	@FXML private JFXNumberTextField seasonField;
	@FXML private JFXNumberTextField episodeField;
	@FXML private JFXComboBox<MediaTypeOptions> mediaTypeComboBox;
	
	private JFXDialog dialogLink;
	private JFXDialog addMovieDialog;
	private boolean isSmoothScrolling = false;
	private LinkedHashMap<MediaItem, MediaResultsPage> mediaList;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		mediaTypeComboBox.setItems( FXCollections.observableArrayList( MediaTypeOptions.values()));
		mediaTypeComboBox.setValue(MediaTypeOptions.MOVIE);
		comboChange(new ActionEvent());
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
		RequiredFieldValidator seasonValidator = new RequiredFieldValidator();
		RequiredFieldValidator epValidator = new RequiredFieldValidator();
		titleValidator.setMessage("Title Required");
        seasonValidator.setMessage("Input Required");
        epValidator.setMessage("Input Required");
        URL url = this.getClass().getResource("/images/error.png");
		BufferedImage image;
		try {
			image = ImageIO.read(url);
			WritableImage i = SwingFXUtils.toFXImage(image, null);
			titleValidator.setIcon(new ImageView(i));
			seasonValidator.setIcon(new ImageView(i));
			epValidator.setIcon(new ImageView(i));
		} catch (IOException e) {
			e.printStackTrace();
		}	
		seasonField.setMaxChars(2);
        seasonField.getValidators().add(seasonValidator);
        seasonField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal && mediaTypeComboBox.getValue() != MediaTypeOptions.MOVIE) {
            	seasonField.validate();
            }
        });
        seasonField.textProperty().addListener((o, oldVal, newVal) -> {
        	seasonField.validate();
        });
        episodeField.setMaxChars(2);
        episodeField.getValidators().add(epValidator);
        episodeField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal && mediaTypeComboBox.getValue() != MediaTypeOptions.MOVIE) {
            	episodeField.validate();
            }
        });
        episodeField.textProperty().addListener((o, oldVal, newVal) -> {
        	episodeField.validate();
        });
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
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 &&
                   (event.getTarget() instanceof Text || ((GridPane) event.getTarget()).getChildren().size() > 0)) {

                   //your code here        
                 }    
            }
        });
	}
	
	@FXML
	private void comboChange(ActionEvent event) {
		boolean isMovie = mediaTypeComboBox.getValue()==MediaTypeOptions.MOVIE;
		seasonField.setVisible(!isMovie);
		episodeField.setVisible(!isMovie);
		
	}
	
	public void setData(LinkedHashMap<MediaItem, MediaResultsPage> mList) {
		mediaList = mList;
		fileListView.getItems().clear();
		fileListView.getItems().addAll(mediaList.keySet());
	}

	public void openDialog(JFXDialog dLink, JFXDialog aLink) {
		if (!isSmoothScrolling) {
			JFXSmoothScroll.smoothScrollingListView(fileListView, 0.1);
			isSmoothScrolling = true;
		}
		episodeField.resetValidation();
		seasonField.resetValidation();
		titleField.resetValidation();
		addMovieDialog = aLink;
		dialogLink = dLink;
		dialogLink.show();
		dialogLink.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
			@Override
			public void handle(JFXDialogEvent arg0) {
				
				
			}
		});   
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
		if (mediaTypeComboBox.getValue()==MediaTypeOptions.MOVIE) {
			String movieName = titleField.getText();
			if (movieName!=null && !movieName.isEmpty()) {
				int movieYear = 0;
				if (!yearField.getText().isEmpty()) {
					movieYear = Integer.parseInt(yearField.getText());
				}
				CustomMovieDb cm = MediaSearchHandler.getMovieInfo(titleField.getText(), movieYear);
			}			
		}//else tv show
		
	}
}
