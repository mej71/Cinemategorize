package application;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class ResultCell<T extends ResultsMediaItem> extends JFXListCell<ResultsMediaItem> {
	
	private GridPane gridPane;
	private ImageView iView = new ImageView();
	private Label titleLabel = new Label();
	private Label descLabel = new Label();
	private Label directorLabel;
	private Label starringLabel;
	private JFXComboBox<Integer> seasonBox;
	private JFXComboBox<Integer> episodeBox;
	
	
	public ResultCell() {
		super();
		titleLabel.getStyleClass().add("header");
	}
	
	@Override
	protected void updateItem(ResultsMediaItem item, boolean empty) {
		super.updateItem(item, empty);
		setText("");
		if (!empty && item!=null) {
			gridPane = new GridPane();
			seasonBox = new JFXComboBox<Integer>();
			seasonBox.setPromptText("Season");
			seasonBox.setLabelFloat(true);
			episodeBox = new JFXComboBox<Integer>();
			episodeBox.setPromptText("Episode");
			episodeBox.setLabelFloat(true);
			final int numCols = 5;
	        final int numRows = 5;
	        for (int i = 0; i < numCols; i++) {
	            ColumnConstraints colConst = new ColumnConstraints();
	            if (i<2) {
	            	colConst.setPrefWidth(50);
	            	colConst.setMaxWidth(50);
	            } else {
	            	colConst.setPercentWidth(100.0 / numCols);
	            }
	            gridPane.getColumnConstraints().add(colConst);
	        }
	        for (int i = 0; i < numRows; i++) {
	        	
	            RowConstraints rowConst = new RowConstraints();
	            if (i==0) {
	            	rowConst.setPercentHeight(15);
	            } else {
	            	rowConst.setPercentHeight(85.0 / (numRows-1));
	            }
	            gridPane.getRowConstraints().add(rowConst);         
	        }
	        descLabel.setWrapText(true);
	        descLabel.prefWidthProperty().bind(gridPane.widthProperty().multiply(0.75));
	        descLabel.maxWidthProperty().bind(gridPane.widthProperty().multiply(0.75));
	        descLabel.setMaxHeight(70);
			gridPane.setMinWidth(0);
			gridPane.setPrefWidth(1);
			iView.setImage(MediaSearchHandler.getItemPoster(item, 185).getImage());
			iView.setFitHeight(139);
			iView.setFitWidth(93);
			gridPane.setPrefHeight(139);
			gridPane.setMaxHeight(139);
			titleLabel.setText(item.getTitle());
			episodeBox.setVisible(false);
			if (!item.isMovie()) {
				
				seasonBox.setItems(
						FXCollections.observableArrayList(IntStream.rangeClosed(1,item.getNumSeasons()).boxed().collect(Collectors.toList()))
				);
				
				seasonBox.valueProperty().addListener(new ChangeListener<Integer>() {

					@Override
					public void changed(ObservableValue<? extends Integer> obVal, Integer oldVal, Integer newVal) {
						episodeBox.setItems(
					    FXCollections.observableArrayList(IntStream.rangeClosed(1, item.getNumEpisodes(newVal)).boxed().collect(Collectors.toList())));
					}
					
				});
				
				//episode box shouldn't be visible unless items have been filled
				
				episodeBox.itemsProperty().addListener(new ChangeListener<ObservableList<Integer>>() {

					@Override
					public void changed(ObservableValue<? extends ObservableList<Integer>> observable,
							ObservableList<Integer> oldValue, ObservableList<Integer> newValue) {
						if (newValue != null && !newValue.isEmpty()) {
							episodeBox.setVisible(true);
						} else {
							episodeBox.setVisible(false);
						}					
					}
				
				});
				
				episodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {

					@Override
					public void changed(ObservableValue<? extends Integer> ov, Integer oldVal, Integer newVal) {
						if (newVal != null) {
							titleLabel.setText(item.getTitle() + ": " + item.getEpisode(seasonBox.getValue(), newVal).getName());
							descLabel.setText(item.getEpisode(seasonBox.getValue(), newVal).getOverview());
						} else {
							titleLabel.setText(item.getTitle());
							descLabel.setText(item.getOverview());
						}
						
					}
					
				});
				gridPane.add(seasonBox, 2, 4);
				gridPane.add(episodeBox, 3, 4);
				seasonBox.setVisible(true);
				if (item.getFirstAvailableEpisode() != null) {
					seasonBox.getSelectionModel().select(item.getFirstAvailableEpisode().getSeasonNumber()-1);
					episodeBox.getSelectionModel().select(item.getFirstAvailableEpisode().getEpisodeNumber());
				}
				
			} else {
				seasonBox.setVisible(false);
			}
			
			descLabel.setText(item.getOverview());
			gridPane.add(iView, 0, 0, 2, 5);
			gridPane.add(titleLabel, 2, 0, 3, 1);
			gridPane.add(descLabel, 2, 1, 3, 3);	
			GridPane.setValignment(descLabel, VPos.TOP);
			gridPane.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
				updateSelected(true);
			});
			setGraphic(gridPane);
		} else {
			setGraphic(null);
		}
	}

}
