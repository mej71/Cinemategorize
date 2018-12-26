package application;

import com.jfoenix.controls.JFXListCell;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class FileCell<T extends MediaItem> extends JFXListCell<MediaItem> {
	
	private VBox vbox;
	private Label folderLabel;
	private Label fileLabel;
	private MediaItem mi;
	
	public FileCell() {
		super();
	}
	
	@Override
	protected void updateItem(MediaItem item, boolean empty) {
		super.updateItem(item, empty);
		mi = item;
		setText("");
		if (!empty && item!=null) {
			folderLabel = new Label();
			fileLabel = new Label();
			vbox = new VBox();
			vbox.setMinWidth(0);
			vbox.setPrefWidth(1);
			folderLabel.setText(item.fileFolder);
			fileLabel.setText(item.fileName);
			vbox.getChildren().add(folderLabel);
			vbox.getChildren().add(fileLabel);
			setTooltip(new Tooltip(item.fullFilePath));
			vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
				updateSelected(true);
			});
			setGraphic(vbox);
		} else {
			setGraphic(null);
		}
	}
	
	public boolean matchesFilePath(String path) {
		return path.equals(mi.fullFilePath);
	}

}
