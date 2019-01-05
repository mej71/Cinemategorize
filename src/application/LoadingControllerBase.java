package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.events.JFXDialogEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

//Parent class for content views, implements loading bar
public class LoadingControllerBase implements Initializable {

	@FXML protected GridPane mainGrid;
	@FXML protected StackPane overlayPane;
    @FXML protected Label progressLabel;
    @FXML protected JFXProgressBar progressBar;
	protected JFXDialog dLink;
	protected Task<Object> loadTask;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		overlayPane.prefHeightProperty().bind(mainGrid.heightProperty());
		overlayPane.prefWidthProperty().bind(mainGrid.widthProperty());		
	}
	
	protected void setDialogLink(JFXDialog dLink) {
		this.dLink = dLink;
		overlayPane.setDisable(false);
		overlayPane.setVisible(true);
		progressBar.setProgress(JFXProgressBar.INDETERMINATE_PROGRESS);
		this.dLink.setOnDialogOpened(new EventHandler<JFXDialogEvent>() {

			@Override
			public void handle(JFXDialogEvent event) {
				loadTask.run();				
				event.consume();
			}
			
		});
		Platform.runLater(() -> determinePrimaryStage());
	}
	
	//check for esc 
	//have to use listener because scene is null for a bit
	private void determinePrimaryStage() {
		dLink.sceneProperty().addListener((observableScene, oldScene, newScene) -> {             
            if (oldScene == null && newScene != null) {
            	dLink.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        		    if (event.getCode().equals(KeyCode.ESCAPE)) {
        		    	this.dLink.close();
        		    }
        		});
            }
        });
    }

}
