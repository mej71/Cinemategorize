package application;

import application.SearchItem.SearchTypes;
import info.movito.themoviedbapi.model.Genre;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class JFXCustomChips {
	
	public static HBox getGenreChip(Genre g) {
        Label label = new Label(g.getName());
        label.setWrapText(true);
        label.getStyleClass().add("genre-chip-label");
        HBox root = new HBox(label);
        root.getStyleClass().add("genre-chip-hbox");
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent ev) {
				ControllerMaster.mainController.searchField.setText(g.getName());
				ControllerMaster.mainController.autoCompletePopup.hide();
				ControllerMaster.mainController.autoEvent = new MovieAutoCompleteEvent<SearchItem>(MovieAutoCompleteEvent.SELECTION, new SearchItem(SearchTypes.GENRE, g));
				ControllerMaster.mainController.refreshSearch();
				ControllerMaster.mainController.closeDialogs();
				ev.consume();
			}
        	
        });
        return root;
	}
	
	public static HBox getTagChip(String tag) {
        Label label = new Label(tag);
        label.setWrapText(true);
        label.getStyleClass().add("tag-chip-label");
        HBox root = new HBox(label);
        root.getStyleClass().add("tag-chip-hbox");
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent ev) {
				ControllerMaster.mainController.searchField.setText(tag);
				ControllerMaster.mainController.autoCompletePopup.hide();
				ControllerMaster.mainController.autoEvent = new MovieAutoCompleteEvent<SearchItem>(MovieAutoCompleteEvent.SELECTION, new SearchItem(SearchTypes.TAG, tag));
				ControllerMaster.mainController.refreshSearch();
				ControllerMaster.mainController.closeDialogs();
				ev.consume();
			}
        	
        });
        return root;
	}

}
