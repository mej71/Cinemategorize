package application;

import java.util.List;

import application.mediainfo.MediaItem;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

//dummy class for image functions in combo cells
class ComboMovieCell<T> extends ListCell<T>{
	
	static final int posterSize = 32;
	static final int hGap = 5;
	static final int maxItems = 5;
	
	
	void fillImages(List<MediaItem> media, FlowPane pane) {
		ImageView iView;
		for (int i = 0; (i < maxItems && i < media.size()); ++i) {
			iView = MediaSearchHandler.getItemPoster(media.get(i), 185);
			iView.setFitHeight(posterSize);
			iView.setFitWidth(posterSize);
			pane.getChildren().add(iView);
		}
	}

}
