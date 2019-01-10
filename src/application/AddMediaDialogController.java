package application;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AddMediaDialogController implements Initializable {
	
	@FXML private JFXDialogLayout dialogLayout;
	@FXML private GridPane dialogGrid;
	@FXML private Label welcomeLabel;
	@FXML private JFXButton chooseFileButton;
	@FXML private JFXButton chooseFolderButton;
	@FXML private Label orLabel;
	@FXML private Label cancelLabel;
	@FXML private Label directoryEmptyLabel;
	@FXML private Label addLabel;
	@FXML private Label progressLabel;
	@FXML private Label searchingLabel;
	@FXML private JFXProgressBar progressBar;
	
	private JFXDialog dialogLink;
	private ExtensionFilter extFilter;
	private Task<?> lookupTask;
	private UIMode uiMode;
	
	enum UIMode {
			INITIAL,
			SEARCHING,
			DEFAULT,
			ERRORED
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {}
	 
	public JFXDialogLayout getLayout() {
		 return dialogLayout;
	}
	 
	public void openDialogMenu(JFXDialog d, boolean initial) {
		uiMode = initial? UIMode.INITIAL: UIMode.DEFAULT;
		dialogLink = d;
		dialogLink.setOverlayClose(!initial);
		updateLayout();
		dialogLink.setCacheContainer(true);
		dialogLink.show(); 

		extFilter = new FileChooser.ExtensionFilter("Video files", ControllerMaster.mainController.supportedFileTypes);
	}

	
	//Filechooser and DirectoryChooser are forceably seperate in Java for some reason, so making two different methods
	//Let's user choose a file, and updates progress bar while doing so
	@FXML
	public void chooseFile() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(extFilter);
	    chooser.setTitle("Choose file(s)");
	    List<File> list = chooser.showOpenMultipleDialog(dialogLink.getScene().getWindow());	
	    lookupTask = new Task<Object>() {
	    	
	    	@Override
	    	public Object call() {
	    	    boolean added = false;
		        if (list != null && !list.isEmpty()) {
		        	int addedFiles = 0;
		        	int failedFiles = 0;
		        	int skippedFiles = 0;
		        	uiMode = UIMode.SEARCHING;
					updateLayout();
					File file;
		            for (int i = 0; i < list.size(); ++i) {
		            	file = list.get(i);
		            	if (ControllerMaster.userData.hasPath(file.getPath())) { //skip files already saved
		            		++skippedFiles;
		            		continue;
		            	}
		            	for (int j = 0; j < extFilter.getExtensions().size(); ++j) {
		            		if (file.getName().endsWith(extFilter.getExtensions().get(j).substring(1))) { //substring to get rid of the *
		            			if (ControllerMaster.userData.addMovieOrTvShow(file)) {
		            				++addedFiles;
		            			} else {
		            				++failedFiles;
		            			}
		            			added = true;
		            			break;
		            		} 
		            	}
		            	if (!added) {
		            		++skippedFiles;
		            	}
		            	added = false;
		            	this.updateProgress(((double)skippedFiles+failedFiles+addedFiles)/(double)list.size(), list.size());
            			this.updateMessage(skippedFiles+failedFiles+addedFiles + " out of " + list.size() + " completed");
		            }
		        } 
		        succeeded();
				return null;
	    	}
	    };
	    setupTaskHandlers();
	}
	
	//bind progress bar and label, start the task, and handle the logic after trying
	public void setupTaskHandlers() {
		progressBar.progressProperty().bind(lookupTask.workDoneProperty());
	    progressLabel.textProperty().bind(lookupTask.messageProperty());
	    dialogLink.setOverlayClose(false);
	    new Thread(lookupTask).start();
	    lookupTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	    	@Override
	    	public void handle(WorkerStateEvent t) {
	    		//update media list in case items were found
	    		if (ControllerMaster.userData.numMediaItems() > 0) {
	    			if (!ControllerMaster.mainController.searchField.getText().isEmpty() && ControllerMaster.mainController.autoEvent!=null ) {
	    				ControllerMaster.userData.refreshViewingList(ControllerMaster.mainController.autoEvent.getObject().getTargetIDs(), false);			
	    			} else {
	    				ControllerMaster.userData.refreshViewingList(null, true);
	    			}
	    		} 	else {
	    			uiMode = UIMode.ERRORED;
	    		}
	    		updateLayout();
	    		//if unknown items were found, open manual lookup, if not and some items are owned, close
	    		if (ControllerMaster.userData.tempManualItems.size() > 0) {
	    			ControllerMaster.mainController.showManualLookupDialog(ControllerMaster.userData.tempManualItems);
		    	} else {
		    		if (ControllerMaster.userData.numMediaItems() > 0) {
		    			dialogLink.close();
		    			return;
		    		}
		    	}
	    	}
	    });
	}
	
	//same as chooseFile but for folders
	@FXML
	public void chooseFolder() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose directory");
		File dirFile = chooser.showDialog(dialogLink.getScene().getWindow());
		new Thread(lookupTask).start();
		lookupTask = new Task<Object>() {
	    	
	    	@Override
	    	public Object call() {
				if (dirFile != null ) {
					uiMode = UIMode.SEARCHING;
					updateLayout();
					int addedFiles = 0;
					int skippedFiles = 0;
					int failedFiles = 0;
					boolean added = false;
		            for (File file : dirFile.listFiles()) {
		            	if (ControllerMaster.userData.hasPath(file.getPath())) { //skip files already saved
		            		++skippedFiles;
		            		continue;
		            	}
		            	for (int i = 0; i < extFilter.getExtensions().size(); ++i) {            		
		            		if (file.getName().endsWith(extFilter.getExtensions().get(i).substring(1))) { //substring to get rid of the *
		            			if (ControllerMaster.userData.addMovieOrTvShow(file)) {
		            				++addedFiles;
		            			} else {
		            				++failedFiles;
		            			}
		            			added = true;
		            			break;
		            		} 
		            	}
		            	if (!added) {
		            		++skippedFiles;
		            	}
		            	added = false;
		            	this.updateProgress(((double)skippedFiles+failedFiles+addedFiles)/(double)dirFile.listFiles().length, dirFile.listFiles().length);
            			this.updateMessage(skippedFiles+failedFiles+addedFiles + " out of " + dirFile.listFiles().length + " completed");
		            }
		        } 
				succeeded();
				return true;
	    	}
		};
		setupTaskHandlers();
	}
	
	//minimalistic layout handler based on the enum UIMode
	public void updateLayout() {
		welcomeLabel.setVisible(uiMode ==  UIMode.INITIAL);
		addLabel.setVisible(uiMode ==  UIMode.DEFAULT);
		cancelLabel.setVisible(uiMode == UIMode.ERRORED);
		chooseFileButton.setVisible(uiMode != UIMode.SEARCHING);
		orLabel.setVisible(uiMode != UIMode.SEARCHING);
		chooseFolderButton.setVisible(uiMode != UIMode.SEARCHING);
		progressBar.setVisible(uiMode == UIMode.SEARCHING);
		progressLabel.setVisible(uiMode == UIMode.SEARCHING);
		searchingLabel.setVisible(uiMode == UIMode.SEARCHING);
	}

}
