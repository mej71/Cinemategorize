package application;

import application.flowcells.ListFlowPane;
import application.flowcells.SearchCell;
import com.jfoenix.controls.JFXPopup;
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

    public void setItems(List<SearchItem> items) {
        searchFlowPane.clearCells();
        searchFlowPane.addCells(SearchCell.createCells(items));
        searchFlowPane.setPrefHeight(searchFlowPane.getChildren().size() * SearchCell.prefCellHeight);
    }

    public boolean isEmpty(){
        return searchFlowPane.getChildren().isEmpty();
    }

    public void clearItems(){
        searchFlowPane.getChildren().clear();
    }

}
