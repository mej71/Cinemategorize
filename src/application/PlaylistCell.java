package application;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlaylistCell<T extends MediaPlaylist> extends FlowCell<T> {

    static int prefCellHeight = 42;

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
            GridPane gridPane = new GridPane();
            final int numCols = 5;
            final int numRows = 1;
            for (int i = 0; i < numCols; i++) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setPercentWidth(100.0 / numCols);
                gridPane.getColumnConstraints().add(colConst);
            }
            for (int i = 0; i < numRows; i++) {

                RowConstraints rowConst = new RowConstraints();
                rowConst.setPercentHeight(100.0 / numRows);
                gridPane.getRowConstraints().add(rowConst);
            }
            Label title = new Label(item.getName());
            title.getStyleClass().add("playlist-cell-name");
            Label subtitle = new Label(item.getItems().size() + " items");
            subtitle.getStyleClass().add("playlist-cell-subtitle");
            gridPane.setPrefHeight(prefCellHeight);
            gridPane.setMaxHeight(prefCellHeight);
            gridPane.prefWidthProperty().bind(getPane().widthProperty());
            gridPane.maxWidthProperty().bind(getPane().widthProperty());
            gridPane.add(title, 0, 0, 3, 1);
            gridPane.add(subtitle, 3, 0, 1, 1);
            setGraphic(gridPane);
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
