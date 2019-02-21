package application.controls;

import application.ControllerMaster;
import application.SearchItem;
import application.SearchItem.SearchTypes;
import com.jfoenix.controls.JFXRippler;
import info.movito.themoviedbapi.model.Genre;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class JFXCustomChips {
	
	public static JFXRippler getGenreChip(Genre g) {


        Label label = new Label(g.getName());
        label.setWrapText(true);
        label.getStyleClass().add("genre-chip-label");
        HBox hBox = new HBox(label);
        hBox.getStyleClass().add("genre-chip-hbox");
        JFXRippler rippler = new JFXRippler(hBox);
        rippler.getStyleClass().add("jfx-fit-rippler");
        hBox.setOnMouseClicked(ev -> {
			ControllerMaster.mainController.autoSelection = new SearchItem(SearchTypes.GENRE, g);
			ControllerMaster.mainController.refreshSearch();
			ControllerMaster.mainController.tempStopSearchDelay = true;
			ControllerMaster.mainController.getSearchField().setText(g.getName());
			ControllerMaster.mainController.autoCompletePopup.hide();
			ControllerMaster.mainController.tempStopSearchDelay = false;
			ControllerMaster.closeDialogs();
			ev.consume();
		});
        return rippler;
	}
	
	public static JFXRippler getTagChip(String tag) {
        Label label = new Label(tag);
        label.setWrapText(true);
        label.getStyleClass().add("tag-chip-label");
        HBox hBox = new HBox(label);
        hBox.getStyleClass().add("tag-chip-hbox");
        JFXRippler rippler = new JFXRippler(hBox);
		rippler.getStyleClass().add("jfx-fit-rippler");
        hBox.setOnMouseClicked(ev -> {
			ControllerMaster.mainController.autoSelection = new SearchItem(SearchTypes.TAG, tag);
			ControllerMaster.mainController.refreshSearch();
			ControllerMaster.mainController.tempStopSearchDelay = true;
			ControllerMaster.mainController.getSearchField().setText(tag);
			ControllerMaster.mainController.autoCompletePopup.hide();
			ControllerMaster.mainController.tempStopSearchDelay = false;
			ControllerMaster.closeDialogs();
			ev.consume();
		});
        return rippler;
	}

}
