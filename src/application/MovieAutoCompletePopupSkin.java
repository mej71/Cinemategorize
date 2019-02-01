package application;

import com.jfoenix.controls.JFXAutoCompletePopup;

import com.jfoenix.controls.JFXListView;
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
	public static final int prefCellHeight = 90;	

	MovieAutoCompletePopup control;
	JFXListView<SearchItem> suggestionList;
	final StackPane pane = new StackPane();
	Scale scale;
	Timeline showTransition;
	boolean isSmoothScrolling = false;

	@SuppressWarnings("unchecked")
	public MovieAutoCompletePopupSkin(MovieAutoCompletePopup control) {
		this.control = control;
		suggestionList = new JFXListView<>();
		suggestionList.getItems().setAll((ObservableList<SearchItem>) control.getFilteredSuggestions());
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
		suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
		suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
		suggestionList.minWidthProperty().bind(control.minWidthProperty());
		registerEventListener();
		suggestionList.setCellFactory(lv -> new ListCell<SearchItem>() {
			static final String NAME_CLASS = "cell-name-text";
			static final String SUB_CLASS = "cell-sub-text";

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
					Label reference = new Label();
					HBox hbox = new HBox();
					hbox.setSpacing(5);
					Text title = new Text();
					Text subtitle = new Text();
					ImageView imageView = null;
					subtitle.getStyleClass().add(SUB_CLASS);
					title.getStyleClass().add(NAME_CLASS);
					subtitle.setText(c.getItemName());
					switch (c.searchType) {
					case TITLE:
						title.setText(c.getItemName());
						if (((MediaItem) c.getItem()).getReleaseDate() != null && ((MediaItem) c.getItem()).getReleaseDate().length()>3) {
							subtitle.setText(" (" + ((MediaItem) c.getItem()).getReleaseDate().substring(0, 4) + ")");
						}
						imageView = MediaSearchHandler.getItemPoster((MediaItem)c.getItem(), 185);
						break;
					case TAG:
						title.setText("With the tag: ");
						break;
					case GENRE:
						title.setText("In the genre: ");
						break;
					case DIRECTOR:
						title.setText("Directed by: ");
						imageView = MediaSearchHandler.getProfilePicture((PersonCrew)c.getItem());
						break;
					case ACTOR:
						title.setText("Starring: ");
						imageView = MediaSearchHandler.getProfilePicture((PersonCast)c.getItem());
						break;
					case WRITER:
						title.setText("Written by: ");
						imageView = MediaSearchHandler.getProfilePicture((PersonCrew)c.getItem());
						break;
					default:
						break;
					}
					if (imageView != null) {
						imageView.setFitWidth(imageHeight);
						imageView.setFitHeight(imageHeight);
						hbox.getChildren().add(imageView);
					}
					reference.setGraphic(new TextFlow(title, subtitle));
					hbox.setAlignment(Pos.CENTER_LEFT);
					hbox.getChildren().add(reference);
					hbox.setPrefHeight(prefCellHeight);
					hbox.setMaxHeight(prefCellHeight);
					hbox.setMinWidth(0);
					hbox.setPrefWidth(1);
					setGraphic(hbox);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
		JFXMediaRippler.forceHidePopOver();
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
		suggestionList.setPrefHeight(Math.min(suggestionList.getItems().size(), getSkinnable().getCellLimit())
				* suggestionList.getFixedCellSize());
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
			control.getSelectionHandler().handle(new MovieAutoCompleteEvent<>(MovieAutoCompleteEvent.SELECTION,
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
