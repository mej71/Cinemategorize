package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXListView;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class SidePanelController implements Initializable {
	
	@FXML private GridPane mainGrid;
	public JFXListView<Label> mainMenuListView;
    private JFXDrawer drawerMenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	mainMenuListView = new JFXListView<Label>();
    	Label addMovieButton = new Label("Add Movie");
    	Label settingsButton = new Label("Settings");
    	Label aboutButton = new Label("About");
    	Label helpButton = new Label("Help");
    	mainMenuListView.getItems().add(addMovieButton);
    	mainMenuListView.getItems().add(settingsButton);
    	mainMenuListView.getItems().add(aboutButton);
    	mainMenuListView.getItems().add(helpButton);
    	mainMenuListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
    		
			@Override
			public void handle(MouseEvent event) {
				if (mainMenuListView.getSelectionModel().getSelectedItem() == null) {
					return;
				}
				switch(mainMenuListView.getSelectionModel().getSelectedItem().getText()) {
				case "Add Movie":
					addMovie();
					break;
				case "Settings":
					showSettings();
					break;
				case "About":
					showAbout();
					break;
				case "Help":
					showHelp();
					break;
				}					
			}
	    });
    	mainGrid.add(mainMenuListView, 0, 1);
    }    
    
    
    public void setDrawer(JFXDrawer d) {
    	drawerMenu = d;
    	drawerMenu.setLayoutY(360);
		drawerMenu.setLayoutX(-100);
		drawerMenu.setDefaultDrawerSize(300);
    }
    
    private void addMovie() {
    	ControllerMaster.mainController.toggleDrawer(false);
    	ControllerMaster.mainController.showAddMovieDialog();
    }
    
    private void showSettings() {
    	ControllerMaster.mainController.toggleDrawer(false);
    }
    
    private void showAbout() {
    	ControllerMaster.mainController.toggleDrawer(false);
    }
    
    private void showHelp() {
    	ControllerMaster.mainController.toggleDrawer(false);
    }
}
