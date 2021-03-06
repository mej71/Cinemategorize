package application.flowcells;

import application.ControllerMaster;
import application.mediainfo.MediaItem;
import application.MediaSearchHandler;
import application.SearchItem;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class SearchCell<T extends SearchItem> extends FlowCell<T> {

    private static final String NAME_CLASS = "cell-name-text";
    private static final String SUB_CLASS = "cell-sub-text";
    public static int prefCellHeight = 36;
    private static final int imageSize = 32;

    private SearchCell(T item){ super(item); }

    @Override
    public void updateItem(){
        super.updateItem();
        if (item != null && getPane() != null) {
            Label reference = new Label();
            HBox hbox = new HBox();
            hbox.setSpacing(7);
            Text title = new Text();
            Text subtitle = new Text();
            ImageView imageView = null;
            subtitle.getStyleClass().add(SUB_CLASS);
            title.getStyleClass().add(NAME_CLASS);
            subtitle.setText(item.getItemName());
            switch (item.searchType) {
                case TITLE:
                    title.setText(item.getItemName());
                    if (((MediaItem) item.getItem()).getReleaseDate() != null && ((MediaItem) item.getItem()).getReleaseDate().length() > 3) {
                        subtitle.setText(" (" + ((MediaItem) item.getItem()).getReleaseDate().substring(0, 4) + ")");
                    }
                    imageView = MediaSearchHandler.getItemPoster((MediaItem) item.getItem(), 185);
                    break;
                case TAG:
                    title.setText("With the tag: ");
                    break;
                case GENRE:
                    title.setText("In the genre: ");
                    break;
                case DIRECTOR:
                    title.setText("Directed by: ");
                    imageView = MediaSearchHandler.getProfilePicture((PersonCrew) item.getItem());
                    break;
                case ACTOR:
                    title.setText("Starring: ");
                    imageView = MediaSearchHandler.getProfilePicture((PersonCast) item.getItem());
                    break;
                case WRITER:
                    title.setText("Written by: ");
                    imageView = MediaSearchHandler.getProfilePicture((PersonCrew) item.getItem());
                    break;
                default:
                    break;
            }
            if (imageView != null) {
                imageView.setFitWidth(imageSize);
                imageView.setFitHeight(imageSize);
                hbox.getChildren().add(imageView);
            }
            reference.setGraphic(new TextFlow(title, subtitle));
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.getChildren().add(reference);
            hbox.setPrefHeight(prefCellHeight);
            hbox.setMaxHeight(prefCellHeight);
            hbox.prefWidthProperty().bind(getPane().widthProperty());
            setGraphic(hbox);
        }
    }

    @Override
    protected void runOnClick() {
        ControllerMaster.mainController.autoSelection = item;
        ControllerMaster.mainController.refreshSearch();
        ControllerMaster.mainController.tempStopSearchDelay = true;
        ControllerMaster.mainController.getSearchField().setText(item.getItemName());
        ControllerMaster.mainController.autoCompletePopup.hide();
        ControllerMaster.mainController.tempStopSearchDelay = false;
    }

    public static <T extends SearchItem> List<SearchCell<T>> createCells(List<T> items) {
        List<SearchCell<T>> cells = new ArrayList<>();
        for (T item : items) {
            cells.add(new SearchCell<>(item));
        }
        return cells;
    }
}
