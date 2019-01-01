package application;

import com.jfoenix.controls.JFXAutoCompletePopup;

import application.SearchItem.SearchTypes;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

//A modified version of JFXAutoCompletePopupSkin that allows me to assign properties to list items
@SuppressWarnings("rawtypes")
public class MovieAutoCompletePopupSkin implements Skin<JFXAutoCompletePopup> {
	
	//not style-able, for some reason
	private final int imageHeight = 32;

	MovieAutoCompletePopup control;
	ListView<SearchItem> suggestionList;
	final StackPane pane = new StackPane();
	Scale scale;
	Timeline showTransition;
	boolean isSmoothScrolling = false;

	@SuppressWarnings("unchecked")
	public MovieAutoCompletePopupSkin(MovieAutoCompletePopup control) {
		this.control = control;
		suggestionList = new ListView<SearchItem>((ObservableList<SearchItem>) control.getFilteredSuggestions());
		suggestionList.setFixedCellSize(control.getFixedCellSize());
		control.fixedCellSizeProperty()
				.addListener(observable -> suggestionList.setFixedCellSize(control.getFixedCellSize()));
		suggestionList.getItems().addListener((InvalidationListener) observable -> updateListHeight());
		suggestionList.getStyleClass().add("autocomplete-list");
		control.suggestionsCellFactoryProperty().addListener((o, oldVal, newVal) -> {
			if (newVal != null) {
				suggestionList
						.setCellFactory((javafx.util.Callback<ListView<SearchItem>, ListCell<SearchItem>>) newVal);
			}
		});
		if (control.getSuggestionsCellFactory() != null) {
			suggestionList.setCellFactory(control.getSuggestionsCellFactory());
		}
		pane.getChildren().add(new Group(suggestionList));
		pane.getStyleClass().add("autocomplete-container");
		suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
		suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
		suggestionList.minWidthProperty().bind(control.minWidthProperty());
		registerEventListener();
		suggestionList.setCellFactory(lv -> new ListCell<SearchItem>() {
			static final String NAME_CLASS = "cell-name-text";
			static final String YEAR_CLASS = "cell-year-text";

			@Override
			protected void updateItem(SearchItem c, boolean empty) {
				super.updateItem(c, empty);
				if (empty) {
					setText(null);
					setStyle("");
					setGraphic(null);
					setHeight(90);
					getStyleClass().add(NAME_CLASS);
				} else {
					if (c.searchType == SearchTypes.TITLE) {
						GridPane gPane = new GridPane();
						gPane.setHgap(5);
						Text mediaName = new Text(c.getItemName());
						Text year = new Text(" (" + ((MediaItem) c.getItem()).getReleaseDate().substring(0, 4) + ")");
						year.getStyleClass().add(YEAR_CLASS);
						mediaName.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(mediaName, year));
						ImageView imageView = MediaSearchHandler.getItemPoster((MediaItem)c.getItem(), 185);
						imageView.getStyleClass().add("autocomplete-imageview");
						imageView.setFitWidth(imageHeight);
						imageView.setFitHeight(imageHeight);
						gPane.add(imageView, 0, 0, 1, 3);
						gPane.add(reference, 1, 0, 3, 3);
						reference.setAlignment(Pos.CENTER);
						setGraphic(gPane);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					} else if (c.searchType == SearchTypes.TAG) {
						HBox hbox = new HBox();
						Text withText = new Text("With the tag: ");
						withText.getStyleClass().add(YEAR_CLASS);
						Text tag = new Text(c.getItemName());
						tag.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(withText, tag));						
						hbox.getChildren().add(reference);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					} else if (c.searchType == SearchTypes.GENRE) {
						HBox hbox = new HBox();
						Text withText = new Text("In the genre: ");
						withText.getStyleClass().add(YEAR_CLASS);
						Text genre = new Text(c.getItemName());
						genre.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(withText, genre));
						hbox.getChildren().add(reference);
						hbox.setAlignment(Pos.CENTER_LEFT);
						setGraphic(hbox);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					} else if (c.searchType == SearchTypes.DIRECTOR) {
						GridPane gPane = new GridPane();
						gPane.setHgap(5);
						Text dirText = new Text("Directed by: ");
						dirText.getStyleClass().add(YEAR_CLASS);
						Text director = new Text(c.getItemName());
						director.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(dirText, director));
						ImageView imageView = MediaSearchHandler.getProfilePicture((PersonCrew)c.getItem());
						imageView.setFitWidth(imageHeight);
						imageView.setFitHeight(imageHeight);
						gPane.add(imageView, 3, 0, 1, 3);
						gPane.add(reference, 0, 0, 3, 3);
						reference.setAlignment(Pos.CENTER);
						setGraphic(gPane);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					} else if (c.searchType==SearchTypes.ACTOR) {
						GridPane gPane = new GridPane();
						gPane.setHgap(5);
						Text dirText = new Text("Starring: ");
						dirText.getStyleClass().add(YEAR_CLASS);
						Text actorName = new Text(c.getItemName());
						actorName.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(dirText, actorName));						
						ImageView imageView = MediaSearchHandler.getProfilePicture((PersonCast)c.getItem());
						imageView.setFitWidth(imageHeight);
						imageView.setFitHeight(imageHeight);
						gPane.add(imageView, 3, 0, 1, 3);
						gPane.add(reference, 0, 0, 3, 3);
						reference.setAlignment(Pos.CENTER);
						setGraphic(gPane);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					} else if (c.searchType==SearchTypes.WRITER) {
						GridPane gPane = new GridPane();
						gPane.setHgap(5);
						Text dirText = new Text("Written by: ");
						dirText.getStyleClass().add(YEAR_CLASS);
						Text writerName = new Text(c.getItemName());
						writerName.getStyleClass().add(NAME_CLASS);
						Label reference = new Label(null, new TextFlow(dirText, writerName));
						ImageView imageView = MediaSearchHandler.getProfilePicture((PersonCrew)c.getItem());
						imageView.setFitWidth(imageHeight);
						imageView.setFitHeight(imageHeight);
						gPane.add(imageView, 3, 0, 1, 3);
						gPane.add(reference, 0, 0, 3, 3);
						reference.setAlignment(Pos.CENTER);
						setGraphic(gPane);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					}
				}				
			}
			
		});
	}
	

	private void registerEventListener() {
		suggestionList.setOnMouseClicked(me -> {
			if (me.getButton() == MouseButton.PRIMARY) {
				selectItem();
				getSkinnable().hide();
			}
		});
		control.showingProperty().addListener((o, oldVal, newVal) -> {
			if (newVal) {
				animate();
			}
		});

		suggestionList.setOnKeyPressed(event -> {
			switch (event.getCode()) {
			case ENTER:
				selectItem();
				getSkinnable().hide();
				break;
			case ESCAPE:
				getSkinnable().hide();
				break;
			default:
				break;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void animate() {
		updateListHeight();
		if (showTransition == null || showTransition.getStatus().equals(Status.STOPPED)) {
			if (scale == null) {
				scale = new Scale(1, 0);
				pane.getTransforms().add(scale);
			}
			scale.setY(0);
			suggestionList.setOpacity(0);
			scale.setPivotX(pane.getLayoutBounds().getWidth() / 2);
			showTransition = new Timeline(
					new KeyFrame(Duration.millis(120), new KeyValue(scale.yProperty(), 1, Interpolator.EASE_BOTH)));
			showTransition.setOnFinished((finish) -> {
				Group vf = (Group) suggestionList.lookup(".sheet");
				ParallelTransition trans = new ParallelTransition();
				for (int i = 0; i < vf.getChildren().size(); i++) {
					ListCell<String> cell = (ListCell<String>) vf.getChildren().get(i);
					int index = cell.getIndex();
					if (index > -1) {
						cell.setOpacity(0);
						cell.setTranslateY(-suggestionList.getFixedCellSize() / 8);
						Timeline f = new Timeline(new KeyFrame(Duration.millis(120), end -> {
							cell.setOpacity(1);
							cell.setTranslateY(0);
						}, new KeyValue(cell.opacityProperty(), 1, Interpolator.EASE_BOTH),
								new KeyValue(cell.translateYProperty(), 0, Interpolator.EASE_BOTH)));
						f.setDelay(Duration.millis(index * 20));
						trans.getChildren().add(f);
					}
				}
				suggestionList.setOpacity(1);
				trans.play();
			});
			showTransition.play();
		}
	}

	private void updateListHeight() {
		final double height = Math.min(suggestionList.getItems().size(), getSkinnable().getCellLimit())
				* suggestionList.getFixedCellSize();
		suggestionList.setPrefHeight(height + suggestionList.getFixedCellSize() / 2);
		if (!isSmoothScrolling) {
			JFXSmoothScroll.smoothScrollingListView(suggestionList, 0.5);
			isSmoothScrolling = true;
		}
	}

	private void selectItem() {
		SearchItem item = suggestionList.getSelectionModel().getSelectedItem();
		if (item == null) {
			try {
				suggestionList.getSelectionModel().select(0);
				item = suggestionList.getSelectionModel().getSelectedItem();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (item != null) {
			control.getSelectionHandler().handle(new MovieAutoCompleteEvent<SearchItem>(MovieAutoCompleteEvent.SELECTION,
					suggestionList.getSelectionModel().getSelectedItem()));
		}
	}

	@Override
	public Node getNode() {
		return pane;
	}

	@Override
	public JFXAutoCompletePopup getSkinnable() {
		return control;
	}

	@Override
	public void dispose() {

		this.control = null;
		if (showTransition != null) {
			showTransition.stop();
			showTransition.getKeyFrames().clear();
			showTransition = null;
		}
	}

}
