package application;
	
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {
	 @Override
	    public void init() throws Exception {
	        super.init();
	        System.out.println("Inside init() method! Perform necessary initializations here.");
	    }

	    @Override
	    public void start(Stage primaryStage) {
	        try {
	            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("CinemaMainController.fxml"));
	            Scene scene = new Scene(root, 1110, 720);
	            primaryStage.setTitle("Cinemategorize");
	            //add a listener that prevents the window from being resized smaller than the initial value
	            primaryStage.showingProperty().addListener((observable, oldValue, showing) -> {
	                if(showing) {
	                    primaryStage.setMinHeight(primaryStage.getHeight());
	                    primaryStage.setMinWidth(primaryStage.getWidth());
	                }
	            });
	            primaryStage.setScene(scene);
	            primaryStage.show();
	            //save all data when closing
	            primaryStage.setOnCloseRequest(arg0 -> ControllerMaster.userData.saveAll());
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	    

	    @Override
	    public void stop() throws Exception {
	        super.stop();
	        System.out.println("Inside stop() method! Destroy resources. Perform Cleanup.");
	    }
}
