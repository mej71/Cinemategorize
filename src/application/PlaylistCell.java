package application;

import javafx.scene.layout.HBox;

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

        } else {
            setGraphic(null);
        }
    }
}
