package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.events.JFXDialogEvent;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

//Parent class for content views, implements loading bar
public class LoadingControllerBase extends EscapableBase implements Initializable {

	@FXML protected GridPane mainGrid;
	@FXML protected StackPane overlayPane;
    @FXML protected JFXSpinner progressSpinner;
	
	protected Task<Object> loadTask;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		overlayPane.prefHeightProperty().bind(mainGrid.heightProperty());
		overlayPane.prefWidthProperty().bind(mainGrid.widthProperty());		
	}
	
	protected void showLoadingPane() {
		overlayPane.setDisable(false);
		overlayPane.setVisible(true);
		progressSpinner.setProgress(JFXSpinner.INDETERMINATE_PROGRESS);
	}
	
	@Override
	protected void setDialogLink(JFXDialog d) {
		setDialogLink(d, true);
	}
	
	protected void setDialogLink(JFXDialog d, boolean needsLoad) {
		super.setDialogLink(d);
		if (needsLoad) {
			showLoadingPane();
		}
		this.dLink.setOnDialogOpened(new EventHandler<JFXDialogEvent>() {

			@Override
			public void handle(JFXDialogEvent event) {
				startTask();				
				event.consume();
			}
			
		});
	}
	
	protected void startTask() {
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
		loadTask.run();
	}
	
	protected void runTasks() {}
	
	protected void successTasks() {
		overlayPane.setVisible(false);
		overlayPane.setDisable(true);
	}
}
