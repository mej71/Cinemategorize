package application.controls;

import application.mediainfo.MediaItem;
import application.MediaSearchHandler;
import application.PersonViewController;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;

import info.movito.themoviedbapi.model.people.Person;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

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
		HBox paneChild = new HBox();	
		paneChild.setAlignment(Pos.CENTER_LEFT);
		paneChild.setSpacing(5);
		ImageView iView = new ImageView();
		iView.setFitWidth(45);
		iView.setFitHeight(70);
		paneChild.getChildren().add(iView);
		Label reference = new Label();
		reference.getStyleClass().add("person_label");
		paneChild.getChildren().add(reference);
		paneChild.getStyleClass().add("person_pane");
		JFXPersonRippler<?> rippler = new JFXPersonRippler<>(paneChild, reference, iView);
		rippler.getStyleClass().add("jfx-fit-rippler");
		return rippler;
	}
	
	private JFXPersonRippler(Node control, Label label, ImageView iView){
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
	}
	
	public void updateImage() {
		if (person != null) {
			Platform.runLater(() -> {
				//split into first/middle name + last name
				String text;
				String name = person.getName();
				if (person.getName().split("\\w+").length > 1) {
					text = name.substring(0, name.lastIndexOf(' ')) + "\n" + name.substring(name.lastIndexOf(' ') + 1);
				} else {
					text = person.getName();
				}
				nameLabel.setText(text);
				personImageView.setImage(MediaSearchHandler.getProfilePicture(person).getImage());
			});
		}
	}
}
