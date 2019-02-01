package application;

import application.SearchItem.SearchTypes;
import com.jfoenix.controls.JFXRippler;
import info.movito.themoviedbapi.model.Genre;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class JFXCustomChips {
	
	static JFXRippler getGenreChip(Genre g) {


        Label label = new Label(g.getName());
        label.setWrapText(true);
        label.getStyleClass().add("genre-chip-label");
        HBox hBox = new HBox(label);
        hBox.getStyleClass().add("genre-chip-hbox");
        JFXRippler rippler = new JFXRippler(hBox);
        rippler.getStyleClass().add("jfx-fit-rippler");
        hBox.setOnMouseClicked(ev -> {
			ControllerMaster.mainController.searchField.setText(g.getName());
			ControllerMaster.mainController.autoCompletePopup.hide();
			ControllerMaster.mainController.autoEvent = new MovieAutoCompleteEvent<>(MovieAutoCompleteEvent.SELECTION, new SearchItem(SearchTypes.GENRE, g));
			ControllerMaster.mainController.refreshSearch();
			ControllerMaster.mainController.closeDialogs();
			ev.consume();
		});
        return rippler;
	}
	
	static JFXRippler getTagChip(String tag) {
        Label label = new Label(tag);
        label.setWrapText(true);
        label.getStyleClass().add("tag-chip-label");
        HBox hBox = new HBox(label);
        hBox.getStyleClass().add("tag-chip-hbox");
        JFXRippler rippler = new JFXRippler(hBox);
        hBox.setOnMouseClicked(ev -> {
			ControllerMaster.mainController.searchField.setText(tag);
			ControllerMaster.mainController.autoCompletePopup.hide();
			ControllerMaster.mainController.autoEvent = new MovieAutoCompleteEvent<>(MovieAutoCompleteEvent.SELECTION, new SearchItem(SearchTypes.TAG, tag));
			ControllerMaster.mainController.refreshSearch();
			ControllerMaster.mainController.closeDialogs();
			ev.consume();
		});
        return rippler;
	}

}
