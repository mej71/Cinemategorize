package application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jfoenix.controls.JFXComboBox;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;

public class ResultCell<T extends ResultsMediaItem> extends FlowCell<T>{
	
	public static int prefCellHeight = 139;
	private GridPane gridPane;
	private ImageView iView;
	private Label titleLabel;
	private Label descLabel;
	//private Label directorLabel;
	//private Label starringLabel;
	private JFXComboBox<Integer> seasonBox;
	private JFXComboBox<Integer> episodeBox;
	
	
	public ResultCell(T item, ListFlowPane<ResultCell<T>, T> pane) {
		super(item, pane);
	}
	
	public void setItem(T item) {
		this.item = item;
		updateItem();
	}
	
	@Override
	public void updateItem() {
		super.updateItem();
		if (getItem() != null) {
			gridPane = new GridPane();
			iView = new ImageView();
			titleLabel = new Label();
			titleLabel.getStyleClass().add("header");
			descLabel = new Label();
			seasonBox = new JFXComboBox<Integer>();
			episodeBox = new JFXComboBox<Integer>();
			seasonBox.setPromptText("Season");
			seasonBox.setLabelFloat(true);
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
			iView.setImage(MediaSearchHandler.getItemPoster(getItem(), 185).getImage());
			iView.setFitHeight(prefCellHeight);
			iView.setFitWidth(93);
			gridPane.setPrefHeight(prefCellHeight);
			gridPane.setMaxHeight(prefCellHeight);
			gridPane.prefWidthProperty().bind(getPane().widthProperty());
			gridPane.maxWidthProperty().bind(getPane().widthProperty());
			titleLabel.setText(getItem().getTitle());
			episodeBox.setVisible(false);
			if (getItem().isTvShow()) {
				seasonBox.setItems(
						FXCollections.observableArrayList(IntStream.rangeClosed(1,getItem().getNumSeasons()).boxed().collect(Collectors.toList()))
				);
				seasonBox.valueProperty().addListener(new ChangeListener<Integer>() {

					@Override
					public void changed(ObservableValue<? extends Integer> obVal, Integer oldVal, Integer newVal) {
						episodeBox.setItems(
					    FXCollections.observableArrayList(IntStream.rangeClosed(1, getItem().getNumEpisodes(newVal)).boxed().collect(Collectors.toList())));
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
							titleLabel.setText(getItem().getTitle() + ": " + getItem().getEpisode(seasonBox.getValue(), newVal).getName());
							descLabel.setText(getItem().getEpisode(seasonBox.getValue(), newVal).getOverview());
						} else {
							titleLabel.setText(getItem().getTitle());
							descLabel.setText(getItem().getOverview());
						}
						
					}
					
				});
				gridPane.add(seasonBox, 2, 4);
				gridPane.add(episodeBox, 3, 4);
				seasonBox.setVisible(true);
				if (getItem().getFirstAvailableEpisode() != null) {
					seasonBox.getSelectionModel().select(getItem().getFirstAvailableEpisode().getSeasonNumber()-1);
					episodeBox.getSelectionModel().select(getItem().getFirstAvailableEpisode().getEpisodeNumber());
				}
			} else {
				seasonBox.setVisible(false);
			}
			descLabel.setText(getItem().getOverview());
			gridPane.add(iView, 0, 0, 2, 5);
			gridPane.add(titleLabel, 2, 0, 3, 1);
			gridPane.add(descLabel, 2, 1, 3, 3);	
			GridPane.setValignment(descLabel, VPos.TOP);
			setGraphic(new HBox(gridPane));
		}
	}
	
	//must be overriden in child classes
	@Override
	protected HBox getGraphic() {
		return super.getGraphic();
	}
	
	
	@Override 
	protected void runOnClick() {
		super.runOnClick();
		if (getItem().isTvShow()) {
    		if (seasonBox.getValue() != null && seasonBox.getValue() > 0 &&
    				episodeBox.getValue() != null && episodeBox.getValue() > 0 ) {
    			getItem().setTvEp(seasonBox.getValue(), episodeBox.getValue());
    		} else {
    			return;
    		}
    	}  
    	ControllerMaster.manualController.confirmMediaItem();
	}
	

	public static <T extends ResultsMediaItem> List<ResultCell<T>> createCells(List<T> results, ListFlowPane<ResultCell<T>,T> pane) {
		List<ResultCell<T>> cells = new ArrayList<ResultCell<T>>();
		for (T result : results) {
			cells.add(new ResultCell<T>(result, pane));
		}
		return cells;
	}


}
