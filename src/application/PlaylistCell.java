package application;

import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public final class PlaylistCell<String> extends ListCell<String> {

	static final int posterSize = 32;
	static final int hGap = 5;
	static final int maxItems = 5;
	
	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty && item != null) {
			GridPane gp = new GridPane();
			Label title = new Label(item.toString());
			gp.add(title, 0, 0, 2, 1);
			FlowPane posterPane = new FlowPane();
			posterPane.setHgap(hGap);
			posterPane.setMaxWidth( (maxItems * (posterSize + hGap)) + hGap);
			posterPane.setPrefWidth( (maxItems * (posterSize + hGap)) + hGap);
			List<MediaItem> media = ControllerMaster.userData.userPlaylists.getPlaylist(item.toString());
			ImageView iView;
			for (int i = 0; (i < maxItems && i < media.size()); ++i) {
				iView = MediaSearchHandler.getItemPoster(media.get(i), 185);
				iView.setFitHeight(posterSize);
				iView.setFitWidth(posterSize);
				posterPane.getChildren().add(iView);
			}
			gp.add(posterPane, 0, 1, 2, 1);
			setGraphic(gp);
		} else {
			setGraphic(null);
		}
		
	}
}
