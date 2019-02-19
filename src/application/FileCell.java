package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class FileCell<T extends MediaItem> extends FlowCell<T> {
	
	static int prefCellHeight = 55;

	public MediaResultsPage mediaResultsPage;
	public List<ResultCell<ResultsMediaItem>> resultCells;
	
	FileCell(T item) { super(item); }
	
	@Override
	public void updateItem() {
		super.updateItem();
		if (item != null) {
			GridPane gridPane = new GridPane();
			gridPane.setPrefHeight(prefCellHeight);
			gridPane.setMaxHeight(prefCellHeight);
			gridPane.prefWidthProperty().bind(getPane().widthProperty());
			gridPane.maxWidthProperty().bind(getPane().widthProperty());
			Label folderLabel = new Label();
			Label fileLabel = new Label();
			folderLabel.setText(item.getFolder());
			fileLabel.setText(item.getFileName());
			gridPane.add(folderLabel, 0, 0);
			gridPane.setAlignment(Pos.CENTER_LEFT);
			gridPane.add(fileLabel, 0, 1);
			Tooltip.install(this, new Tooltip(item.getFullFilePath()));
			setGraphic(new HBox(gridPane));
		} 
	}
	
	public static <T extends MediaItem> List<FileCell<T>> createCells(Set<T> items) {
		List<FileCell<T>> cells = new ArrayList<>();
		for (T item : items) {
			cells.add(new FileCell<>(item));
		}
		return cells;		
	}

}
