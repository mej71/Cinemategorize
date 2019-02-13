package application;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class AddMediaDialogController extends EscapableBase implements Initializable {

	static ExtensionFilter extFilter = new ExtensionFilter("Video files", ControllerMaster.mainController.supportedFileTypes);

	@FXML private JFXDialogLayout dialogLayout;
	@FXML private Label welcomeLabel;
	@FXML private JFXButton chooseFileButton;
	@FXML private JFXButton chooseFolderButton;
	@FXML private Label orLabel;
	@FXML private Label cancelLabel;
	@FXML private Label addLabel;
	@FXML private Label progressLabel;
	@FXML private Label searchingLabel;
	@FXML private JFXProgressBar progressBar;

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
		super.setDialogLink(d);
		dLink.setOverlayClose(!initial);
		preventEscape = initial;
		updateLayout();
		
		dLink.show();
	}
	
	//Filechooser and DirectoryChooser are forcibly sseparatein Java for some reason, so making two different methods
	//Let's user choose a file, and updates progress bar while doing so
	@FXML
	public void chooseFile() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(extFilter);
	    chooser.setTitle("Choose file(s)");
	    List<File> list = chooser.showOpenMultipleDialog(dLink.getScene().getWindow());	
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
		            			if (UserDataHelper.addMovieOrTvShow(file)) {
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
	    dLink.setOverlayClose(false);
	    preventEscape = true;
	    new Thread(lookupTask).start();
	    lookupTask.setOnSucceeded(t -> {
			//update media list in case items were found
			if (ControllerMaster.userData.numMediaItems() > 0) {
				preventEscape = false;
				ControllerMaster.mainController.refreshSearch();
			} 	else {
				uiMode = UIMode.ERRORED;
			}
			updateLayout();
			//if unknown items were found, open manual lookup, if not and some items are owned, close
			if (ControllerMaster.userData.tempManualItems.size() > 0) {
				ControllerMaster.mainController.showManualLookupDialog(ControllerMaster.userData.tempManualItems);
			} else {
				if (ControllerMaster.userData.numMediaItems() > 0) {
					preventEscape = false;
					dLink.close();
				}
			}
		});
	}
	
	//same as chooseFile but for folders
	@FXML
	public void chooseFolder() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose directory (also searches through all subdirectories)");
		File dirFile = chooser.showDialog(dLink.getScene().getWindow());
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
					List<File> skippedResults = new ArrayList<>();
					List<File> fileResults = new ArrayList<>();
					findMovieFiles(dirFile, fileResults, skippedResults);
					skippedFiles += skippedResults.size();
		            for (File file : fileResults) {
		            	if (ControllerMaster.userData.hasPath(file.getPath())) { //skip files already saved
		            		++skippedFiles;
		            		continue;
		            	}
						if (UserDataHelper.addMovieOrTvShow(file)) {
							++addedFiles;
						} else {
							++failedFiles;
						}
		            	this.updateProgress(((double)skippedFiles+failedFiles+addedFiles)/(double) (fileResults.size()+skippedResults.size()), (fileResults.size()+skippedResults.size()));
            			this.updateMessage(skippedFiles+failedFiles+addedFiles + " out of " + (fileResults.size()+skippedResults.size()) + " completed");
		            }
		        } 
				succeeded();
				return true;
	    	}
		};
		setupTaskHandlers();
	}

	//recursive method to find all movies even in subdirectories
	private void findMovieFiles(File rootFile, List<File> foundFiles, List<File> skippedResults) {
		if (rootFile == null || foundFiles == null) {
			return;
		}
		if(rootFile.isDirectory()) {
			for(File file : rootFile.listFiles()) {
				findMovieFiles(file, foundFiles, skippedResults);
			}
		} else if(rootFile.isFile()) {
			boolean added = false;
			for (int i = 0; i < extFilter.getExtensions().size(); ++i) {
				if (rootFile.getName().endsWith(extFilter.getExtensions().get(i).substring(1))) { //substring to get rid of the *
					foundFiles.add(rootFile);
					added = true;
					break;
				}
			}
			if (!added) {
				skippedResults.add(rootFile);
			}
		}
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
