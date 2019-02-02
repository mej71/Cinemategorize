package application;

import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public final class PlaylistComboCell<T extends MediaPlaylist> extends ComboMovieCell<T> {
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty && item != null) {
			GridPane gp = new GridPane();
			Label title = new Label(item.toString());
			gp.add(title, 0, 0, 2, 1);
			FlowPane posterPane = new FlowPane();
			posterPane.setHgap(hGap);
			posterPane.setMaxWidth( (maxItems * (posterSize + hGap)) + hGap);
			posterPane.setPrefWidth( (maxItems * (posterSize + hGap)) + hGap);
			List<MediaItem> media = item.getItems();
			fillImages(media, posterPane);
			gp.add(posterPane, 0, 1, 2, 1);
			setGraphic(gp);
		} else {
			setGraphic(null);
			setText(null);
		}
		
	}
}
