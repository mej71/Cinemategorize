package application.controls;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import application.*;
import application.mediainfo.MediaItem;
import info.movito.themoviedbapi.model.people.PersonCast;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.*;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.jfoenix.controls.JFXRippler;

import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class JFXMediaRippler extends JFXRippler { 

	public final static int baseHeight = 208;
	private final static int baseWidth = 139;
	public final static int bottomHeight = 38;

	private static GridPane gridPane;
	private static PopOver pOver;
	private static Label titleLabel;
	private static Label descLabel;
	private static Label directorsLabel;
	private static Label actorsLabel;
	private static boolean hasEntered = false;
	private static final int taskMiliSeconds = 500;
	private static Timer timer;
	
	private static StackPane tempPane;
	private static ImageView tempIView;
	private static final EventHandler<MouseEvent> handler = MouseEvent::consume;
	
	
	
	public MediaItem linkedItem;
	private final ImageView iView;
	private final ImageView heartIcon;
	private final ImageView playIcon;
	private final Label nameLabel;

	private float unfocusedMultiplier = 1.0f;
	private float focusedMultiplier = 1.1f;
	
	private static void init() {
		gridPane = new GridPane();
		final int numCols = 5;
        final int numRows = 11;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / numCols);
            gridPane.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / numRows);
            gridPane.getRowConstraints().add(rowConst);         
        }
        titleLabel = new Label();
        titleLabel.setId("pop-title");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(250);
        descLabel = new Label();
        descLabel.setMaxWidth(250);
        descLabel.setMaxHeight(150);
        descLabel.setId("pop-descript");
        directorsLabel = new Label();
        directorsLabel.setMaxWidth(250);
        directorsLabel.setId("pop-info");
        actorsLabel = new Label();
        actorsLabel.setId("pop-info");
        actorsLabel.setMaxWidth(250);
        gridPane.add(titleLabel, 0, 0, numCols, 1);
		gridPane.add(descLabel, 0, 1, numCols, 7);
		gridPane.add(directorsLabel, 0, 8, numCols, 1);
		gridPane.add(actorsLabel, 0, 9, numCols, 2);
		gridPane.addEventFilter(MouseEvent.ANY, handler);
		
		//show popover for quick movie info when mouse is over media pane
		pOver = new PopOver(gridPane);
		pOver.setFadeInDuration(Duration.ONE);
		pOver.setFadeOutDuration(Duration.ZERO);
		pOver.arrowLocationProperty().set(ArrowLocation.LEFT_CENTER);
	}
	
	private static TimerTask getTimerTask() {
		return new TimerTask() {

			@Override
			public void run() {
				if (hasEntered && !popIsShowing()) {
					if (tempPane != null) {
						Platform.runLater(() -> {
							pOver.setAnchorX(-300);
							if (!ControllerMaster.mainController.autoCompletePopup.isShowing()) {
								pOver.show(tempPane);
							}
						});
					}
					this.cancel();
					hasEntered = false;
				}				
			}			
		};
	}
	
	public static void forceHidePopOver() {
		if (pOver == null) {
			return;
		}
		pOver.hide();
		if (hasEntered) {
			timer.cancel();
			hasEntered = false;
		}
	}
	
	public static boolean popIsShowing() {
		return pOver.isShowing();
	}
	
	private JFXMediaRippler(StackPane control, ImageView iv, ImageView heartIcon, ImageView playIcon, Label nameLabel){
		super(control, RipplerMask.RECT, RipplerPos.FRONT);
		if (gridPane == null) {
			init();
		}
		control.setPickOnBounds(false);
		iView = iv;
		this.heartIcon = heartIcon;
		this.playIcon = playIcon;
		this.nameLabel = nameLabel;
		this.setFocusTraversable(true);
		this.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != oldValue) {
				updateScale();
			}
		});
	}

	@Override
	protected void initControlListeners() {
		// if the control got resized the overlay rect must be rest
		control.layoutBoundsProperty().addListener(observable -> resetRippler());
		if(getChildren().contains(control))
			control.boundsInParentProperty().addListener(observable -> resetRippler());
		control.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
			if (event.getTarget().equals(iView)) {
				if (event.isPrimaryButtonDown()) {
					createRipple(event.getX(), event.getY());
				}
			} else if (event.getTarget().equals(heartIcon)) {
				if (ControllerMaster.userData.favoritesContains(linkedItem)) {
					ControllerMaster.userData.getFavoritesList().removeMedia(linkedItem);
				} else {
					ControllerMaster.userData.getFavoritesList().addItem(linkedItem);
				}
				updateFavoriteIcon();
			} else if (event.getTarget().equals(playIcon)) {
				if (linkedItem.isMovie()) {
					PlayMedia.playMedia(linkedItem);
				} else {
					PlayMedia.playSeason(linkedItem, linkedItem.tvShow.lastViewedSeason, linkedItem.tvShow.lastViewedEpisode, true);
				}
			}
			event.consume();
		});
		// create fade out transition for the ripple
		control.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
			releaseRipple();
			if (event.getTarget().equals(iView)) {
				boolean isMovie = linkedItem.isMovie();
				int mId = linkedItem.getId();
				if ((isMovie && ControllerMaster.userData.ownsMovie(mId)) ||
						(!isMovie && ControllerMaster.userData.ownsShow(mId))) {
					if (pOver.isShowing()) {
						pOver.hide();
					}
					if (hasEntered) {
						timer.cancel();
						hasEntered = false;
					}
					Platform.runLater(() -> ControllerMaster.showSelectionDialog(linkedItem));
				}
			}
		});
	}

	public void setItem(MediaItem item) {
		linkedItem = item;
		boolean isMovie = linkedItem.isMovie();
		int mId = linkedItem.getId();
		if ( (isMovie && ControllerMaster.userData.ownsMovie(mId)) ||
				(!isMovie && ControllerMaster.userData.ownsShow(mId))) {
			getControl().getStyleClass().add("selectable");
		} else {
			getControl().getStyleClass().removeAll("selectable");
		}
		iView.setImage(MediaSearchHandler.getItemPoster(linkedItem).getImage());
		updateScale();
		boolean owned = (isMovie && ControllerMaster.userData.ownsMovie(mId)) ||
				(!isMovie && ControllerMaster.userData.ownsShow(mId));
		if (owned) {
			updateFavoriteIcon();
			playIcon.getStyleClass().add("play-icon");
			URL url = MediaSearchHandler.class.getClassLoader().getResource("play.png");
			try {
				playIcon.setImage(SwingFXUtils.toFXImage(ImageIO.read(Objects.requireNonNull(url)), null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		heartIcon.setVisible(owned);
		playIcon.setVisible(owned);
		nameLabel.setText(linkedItem.getTitle(false));

	}

	private void updateFavoriteIcon() {
		String heartName = (ControllerMaster.userData.favoritesContains(linkedItem))? "heart.png" : "empty_heart.png";
		URL url = MediaSearchHandler.class.getClassLoader().getResource(heartName);
		try {
			heartIcon.getStyleClass().remove("heart-icon");
			heartIcon.setImage(SwingFXUtils.toFXImage(ImageIO.read(Objects.requireNonNull(url)), null));
			heartIcon.getStyleClass().add("heart-icon");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateScale() {
		updateScale(ControllerMaster.userData.getScaleFactor());
	}

	public void updateScale(double scaleFactor) {
		StackPane pane = getPane();
		float multiplier = (this.isFocused())? focusedMultiplier :unfocusedMultiplier;
		pane.setMaxWidth(JFXMediaRippler.baseWidth * scaleFactor);
		pane.setMaxHeight(JFXMediaRippler.baseHeight * scaleFactor * multiplier + JFXMediaRippler.bottomHeight);
		pane.resize(JFXMediaRippler.baseWidth * scaleFactor, JFXMediaRippler.baseHeight * scaleFactor * multiplier + JFXMediaRippler.bottomHeight);
		nameLabel.maxWidthProperty().bind(pane.widthProperty().subtract(playIcon.getBoundsInLocal().getMaxX() - playIcon.getBoundsInLocal().getMinX()));
	}

	public StackPane getPane() {
		return (StackPane) getControl();
	}
	
	public static JFXMediaRippler createBasicRippler(MovieScrollPane scrollPane) {
		StackPane paneChild = new StackPane();
		paneChild.minWidthProperty().bind(paneChild.maxWidthProperty());
		paneChild.minHeightProperty().bind(paneChild.maxHeightProperty());
		paneChild.setPickOnBounds(false);

		ImageView iView = new ImageView();
		iView.fitWidthProperty().bind(paneChild.widthProperty());
		iView.fitHeightProperty().bind(paneChild.heightProperty().subtract(bottomHeight));
		StackPane.setAlignment(iView, Pos.TOP_CENTER);

		ImageView heartIcon = new ImageView();
		StackPane.setAlignment(heartIcon, Pos.CENTER_RIGHT);

		ImageView playIcon = new ImageView();
		StackPane.setAlignment(playIcon, Pos.CENTER_RIGHT);

		VBox overlayPane = new VBox();
		overlayPane.minHeightProperty().bind(paneChild.heightProperty().subtract((bottomHeight*2)));
		overlayPane.prefHeightProperty().bind(paneChild.heightProperty().subtract((bottomHeight*2)));
		overlayPane.maxHeightProperty().bind(paneChild.heightProperty().subtract((bottomHeight*2)));
		overlayPane.prefWidthProperty().bind(paneChild.widthProperty());
		overlayPane.maxWidthProperty().bind(paneChild.widthProperty());
		overlayPane.setPickOnBounds(false);
		overlayPane.setAlignment(Pos.BOTTOM_RIGHT);

		StackPane bottomPane = new StackPane();
		bottomPane.maxWidthProperty().bind(paneChild.maxWidthProperty());
		bottomPane.prefWidthProperty().bind(paneChild.prefWidthProperty());
		bottomPane.setPrefHeight(bottomHeight);
		bottomPane.setMaxHeight(bottomHeight);
		bottomPane.resize(baseWidth, bottomHeight);
		StackPane.setAlignment(bottomPane, Pos.BOTTOM_CENTER);
		bottomPane.getStyleClass().add("media-rippler-bottom");

		Label name = new Label();
		name.prefWidthProperty().bind(name.maxWidthProperty());
		name.getStyleClass().add("rippler-name");
		StackPane.setAlignment(name, Pos.CENTER_LEFT);

		bottomPane.getChildren().addAll(name, playIcon);
		overlayPane.getChildren().add(heartIcon);
		paneChild.getChildren().addAll(iView, bottomPane, overlayPane);

		JFXMediaRippler rippler = new JFXMediaRippler(paneChild, iView, heartIcon, playIcon, name);
		rippler.maxWidthProperty().bind(paneChild.widthProperty());
		rippler.maxHeightProperty().bind(paneChild.heightProperty());
		rippler.updateScale();

        iView.addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> {
			if ( (scrollPane !=null && scrollPane.isScrolling) || pOver.isShowing()) {
				return;
			}
			MediaItem mi = rippler.linkedItem;
        	String lastDate = "";
        	if (mi.isTvShow()) {
        		lastDate = " - ";
        		if (mi.getLastAirDate() != null && mi.getLastAirDate().length()>3) {
        			lastDate += mi.getLastAirDate().substring(0, 4);
            	} else {
            		lastDate += "(N/A)";
            	}
        		JFXMediaRippler.directorsLabel.setText(mi.getNumSeasons() + " seasons");
        	} else {
        		StringBuilder directors = new StringBuilder();
        		List<PersonCrew> crew = mi.getCrew(false);
        		PersonCrew crewMember;
	        	for (int i = 0; i < crew.size(); ++i) {
					crewMember = crew.get(i);
	        		if (crewMember.getJob().equalsIgnoreCase("Director")) {
	        			if (directors.toString().equals("")) {
	        				directors.append(crewMember.getName());
	        			} else {
	        				directors.append(", ").append(crewMember.getName());
	        			}
	        		}
	        	}
	        	JFXMediaRippler.directorsLabel.setText("Directed by: " + directors);
        	}
        	String releaseDate = "";
        	if (mi.getReleaseDate(false) != null && mi.getReleaseDate(false).length()>3) {
        		releaseDate = mi.getReleaseDate(false).substring(0, 4);
        	}
        	JFXMediaRippler.titleLabel.setText(mi.getTitle(false)+ " (" + releaseDate + lastDate + ")");
        	JFXMediaRippler.descLabel.setText(mi.getOverview(false));
        	
        	StringBuilder cast = new StringBuilder();
        	List<PersonCast> pCast = mi.getCast(false);
        	for (int i = 0; i<4; ++i) {
        		if (i==pCast.size()) {
        			break;
        		}
        		if (i==0) {
        			cast.append(pCast.get(i).getName());
        		} else {
        			cast.append(", ").append(pCast.get(i).getName());
        		}
        	}
        	JFXMediaRippler.actorsLabel.setText("Starring: " + cast);		
			if (!hasEntered) {
				timer = new Timer();
				hasEntered = true;
				tempPane = paneChild;
				timer.schedule(getTimerTask(), taskMiliSeconds);
			}
		});
        iView.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
			if (e.getPickResult().getIntersectedNode() == iView) {
				tempIView = iView;
				tempIView.addEventHandler(MouseEvent.MOUSE_EXITED, (ev) -> {
					if (ev.getPickResult().getIntersectedNode() == iView) {
						return;
					}
					if (pOver.isShowing()) {
						pOver.hide();
					}
					if (hasEntered) {
						timer.cancel();
						hasEntered = false;
					}
					
				});
				return;
			} else {
				tempIView = null;
			}
			if (pOver.isShowing()) {
				pOver.hide();
			}
			if (hasEntered) {
				timer.cancel();
				hasEntered = false;
			}
			
		});
		return rippler;
	}
}