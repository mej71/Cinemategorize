package application;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlaylistCell<T extends MediaPlaylist> extends FlowCell<T> {

    PlaylistCell(T item, ListFlowPane<PlaylistCell<T>, T> pane) {
        super(item, pane);
    }

    void setItem(T item) {
        this.item = item;
        updateItem();
    }

    @Override
    public void updateItem() {
        super.updateItem();
        if (item != null) {
            HBox hBox = new HBox();
            Label title = new Label(item.getName());
            Label subtitle = new Label(item.getItems().size() + " items");
            hBox.getChildren().addAll(title, subtitle);
            setGraphic(hBox);
        } else {
            setGraphic(null);
        }
    }

    public static <T extends MediaPlaylist> List<PlaylistCell<T>> createCells(List<T> items, ListFlowPane<PlaylistCell<T>, T> pane) {
        List<PlaylistCell<T>> cells = new ArrayList<>();
        for (T item : items) {
            cells.add(new PlaylistCell<>(item, pane));
        }
        return cells;
    }
}
