package application;

import java.util.List;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public final class PlaylistCell<String> extends ComboMovieCell<String> {
	
	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty && item != null) {
			if (item.equals(CinemaController.manageName)) {
				setGraphic(new Label(item.toString()));
				return;
			}
			GridPane gp = new GridPane();
			Label title = new Label(item.toString());
			gp.add(title, 0, 0, 2, 1);
			FlowPane posterPane = new FlowPane();
			posterPane.setHgap(hGap);
			posterPane.setMaxWidth( (maxItems * (posterSize + hGap)) + hGap);
			posterPane.setPrefWidth( (maxItems * (posterSize + hGap)) + hGap);
			List<MediaItem> media = ControllerMaster.userData.userPlaylists.getPlaylist(item.toString());
			fillImages(media, posterPane);
			gp.add(posterPane, 0, 1, 2, 1);
			setGraphic(gp);
		} else {
			setGraphic(null);
		}
		
	}
}
