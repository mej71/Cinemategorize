package application;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;

import info.movito.themoviedbapi.model.people.Person;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

public class JFXPersonRippler<T extends Person> extends JFXRippler { 
	
	private static PersonViewController pvController;
	private static JFXDialog personViewDialog;
	
	
	private Label nameLabel;
	private ImageView personImageView;
	private T person = null;
	private MediaItem mediaItem;
	
	public static void setStaticVariables(PersonViewController pvc, JFXDialog pvd) {
		pvController = pvc;
		personViewDialog = pvd;
	}
	
	public static void closeWindow() {
		personViewDialog.close();
	}
	
	public static JFXPersonRippler<?> createBasicRippler() {
		StackPane paneChild = new StackPane();	
		paneChild.setPrefWidth(150);
		ImageView iView = new ImageView();
		paneChild.getChildren().add(iView);
		iView.setFitWidth(47);
		iView.setFitHeight(70);
		
		Label reference = new Label();
		reference.setMaxWidth(90);
		reference.getStyleClass().add("person_label");
		paneChild.getChildren().add(reference);
		StackPane.setAlignment(reference, Pos.BOTTOM_CENTER);
		StackPane.setAlignment(iView, Pos.TOP_LEFT);
		paneChild.getStyleClass().add("person_pane");
		JFXPersonRippler<?> rippler = new JFXPersonRippler(paneChild, reference, iView);
		rippler.setRipplerFill((Paint.valueOf("black")));
		rippler.setMaskType(JFXRippler.RipplerMask.FIT);
		rippler.setPosition(RipplerPos.FRONT);
		return rippler;
	}
	
	public JFXPersonRippler(Node control, Label label, ImageView iView){
		super(control, RipplerMask.RECT, RipplerPos.FRONT);
		control.getStyleClass().add("selectable");
		control.setPickOnBounds(false);
		nameLabel = label;
		personImageView = iView;
		setOnMouseClicked(e -> {
	        if (e.getButton() == MouseButton.PRIMARY){
	        	pvController.showPerson(personViewDialog, person, mediaItem);
	        }
	    });
	} 
	
	public void setPerson(T person, MediaItem mediaItem) {
		this.person = person;
		this.mediaItem = mediaItem;
		Platform.runLater(() -> {
			nameLabel.setText(person.getName());
			personImageView.setImage(MediaSearchHandler.getProfilePicture(person).getImage());
		});
	}
}
