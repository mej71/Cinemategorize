package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.events.JFXDialogEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

//Parent class for content views, implements loading bar
public class LoadingControllerBase implements Initializable {

	@FXML protected GridPane mainGrid;
	@FXML protected StackPane overlayPane;
    @FXML protected JFXSpinner progressSpinner;
	protected JFXDialog dLink;
	protected Task<Object> loadTask;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		overlayPane.prefHeightProperty().bind(mainGrid.heightProperty());
		overlayPane.prefWidthProperty().bind(mainGrid.widthProperty());		
	}
	
	protected void setDialogLink(JFXDialog dLink) {
		setDialogLink(dLink, true);
	}
	
	protected void showLoadingPane() {
		overlayPane.setDisable(false);
		overlayPane.setVisible(true);
		progressSpinner.setProgress(JFXSpinner.INDETERMINATE_PROGRESS);
	}
	
	protected void setDialogLink(JFXDialog dLink, boolean needsLoad) {
		this.dLink = dLink;
		if (needsLoad) {
			showLoadingPane();
		}
		loadTask = new Task<Object>() {

			@Override
			protected Object call() throws Exception {
				runTasks();
				succeeded();
				return null;
			}
		};
		loadTask.setOnSucceeded(e -> {
			successTasks();
		});
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
		if (dLink.getScene() == null) {
			dLink.sceneProperty().addListener((observableScene, oldScene, newScene) -> {             
				addKeyListener(newScene);
	        });
		} else {
			addKeyListener(dLink.getScene());
		}
    }
	
	private EventHandler<KeyEvent> keyListener = (event -> {
	    if (event.getCode().equals(KeyCode.ESCAPE)) {
	    	this.dLink.close();
	    }	    
	});
	
	private void addKeyListener(Scene scene) {
		if (scene != null) {
			scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyListener);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, keyListener);
        }
	}
	
	protected void runTasks() {}
	
	protected void successTasks() {
		overlayPane.setVisible(false);
		overlayPane.setDisable(true);
	}
}
