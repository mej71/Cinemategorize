package application;

import com.jfoenix.controls.JFXPopup;
import com.jfoenix.skins.JFXPopupSkin;
import javafx.scene.control.ScrollPane;

import java.util.List;

public class SearchPopup extends JFXPopup {

    private ScrollPane searchScrollPane;
    private ListFlowPane<SearchCell<SearchItem>, SearchItem> searchFlowPane;
    private int maxItems = 10;

    public SearchPopup(){
        super();
        searchScrollPane = new ScrollPane();
        searchFlowPane = new ListFlowPane<>();
        searchScrollPane.setContent(searchFlowPane);
        searchScrollPane.setFitToWidth(true);
        searchScrollPane.prefWidthProperty().bind(this.prefWidthProperty());
        searchScrollPane.setMaxHeight(maxItems * SearchCell.prefCellHeight);
        searchScrollPane.prefHeightProperty().bind(this.prefHeightProperty());
        searchFlowPane.bindWidthToNode(searchScrollPane);
        this.setPopupContent(searchScrollPane);
    }

    void setItems(List<SearchItem> items) {
        searchFlowPane.getChildren().clear();
        searchFlowPane.getChildren().addAll(SearchCell.createCells(items, searchFlowPane));
        searchFlowPane.setPrefHeight(searchFlowPane.getChildren().size() * SearchCell.prefCellHeight);
    }

    boolean isEmpty(){
        return searchFlowPane.getChildren().isEmpty();
    }

    void clearItems(){
        searchFlowPane.getChildren().clear();
    }

}
