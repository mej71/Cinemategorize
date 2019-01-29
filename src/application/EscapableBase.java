package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXDialog;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EscapableBase implements Initializable {

	protected JFXDialog dLink;
	protected boolean preventEscape = false;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {}
	
	protected void setDialogLink(JFXDialog dLink) {
		this.dLink = dLink;
		Platform.runLater(this::determinePrimaryStage);
	}

	//check for esc 
	//have to use listener because scene is null for a bit
	private void determinePrimaryStage() {
		if (dLink.getScene() == null) {
			dLink.sceneProperty().addListener((observableScene, oldScene, newScene) -> addKeyListener(newScene));
		} else {
			addKeyListener(dLink.getScene());
		}
    }
	
	private EventHandler<KeyEvent> keyListener = (event -> {
	    if (event.getCode().equals(KeyCode.ESCAPE) && !preventEscape) {
	    	this.dLink.close();
	    }	    
	});
	
	private void addKeyListener(Scene scene) {
		if (scene != null) {
			scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyListener);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, keyListener);
        }
	}
}
