package application;

import java.util.Timer;
import java.util.TimerTask;

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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Paint;
import javafx.util.Duration;

public class JFXMediaRippler extends JFXRippler { 
	
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
	
	private JFXMediaRippler(StackPane control, ImageView iv){
		super(control, RipplerMask.RECT, RipplerPos.FRONT);
		if (gridPane == null) {
			init();
		}
		control.setPickOnBounds(false);
		iView = iv;
	} 
	
	public void setItem(MediaItem item) {
		linkedItem = item;
		if ( (linkedItem.isMovie() && ControllerMaster.userData.ownsMovie(linkedItem.getId())) || 
				(!linkedItem.isMovie() && ControllerMaster.userData.ownsShow(linkedItem.getId()))) {
			getControl().getStyleClass().add("selectable");
		} else {
			getControl().getStyleClass().removeAll("selectable");
		}
		iView.setImage(MediaSearchHandler.getItemPoster(linkedItem).getImage());
	}
	
	public StackPane getPane() {
		return (StackPane) getControl();
	}
	
	public static JFXMediaRippler createBasicRippler(TilePane tilePane, MovieScrollPane scrollPane) {
		StackPane paneChild = new StackPane();
		ImageView iView = new ImageView();
		
		//user a smaller resolution for smaller scale.  looks better for smaller posters
		paneChild.getChildren().add(iView);
		paneChild.setPickOnBounds(false);
		iView.fitWidthProperty().bind(paneChild.widthProperty());
		iView.fitHeightProperty().bind(paneChild.heightProperty());
		
		
		JFXMediaRippler rippler = new JFXMediaRippler(paneChild, iView);
		rippler.getStyleClass().add("jfx-media-rippler");
		paneChild.setMaxWidth(139);
		paneChild.setMaxHeight(208);
		paneChild.resize(139, 208);
		paneChild.addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> { 
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
	        	PersonCrew crew;
	        	for (int i = 0; i < mi.getCrew().size(); ++i) {
	        		crew = mi.getCrew().get(i);
	        		if (crew.getJob().equalsIgnoreCase("Director")) {
	        			if (directors.toString().equals("")) {
	        				directors.append(crew.getName());
	        			} else {
	        				directors.append(", ").append(crew.getName());
	        			}
	        		}
	        	}
	        	JFXMediaRippler.directorsLabel.setText("Directed by: " + directors);
        	}
        	String releaseDate = "";
        	if (mi.getReleaseDate() != null && mi.getReleaseDate().length()>3) {
        		releaseDate = mi.getReleaseDate().substring(0, 4);
        	}
        	JFXMediaRippler.titleLabel.setText(mi.getTitle()+ " (" + releaseDate + lastDate + ")");
        	JFXMediaRippler.descLabel.setText(mi.getOverview());
        	
        	StringBuilder cast = new StringBuilder();
        	for (int i = 0; i<4; ++i) {
        		if (i==mi.getCast().size()) {
        			break;
        		}
        		if (i==0) {
        			cast.append(mi.getCast().get(i).getName());
        		} else {
        			cast.append(", ").append(mi.getCast().get(i).getName());
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
		paneChild.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
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
		paneChild.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {

			if ( (rippler.linkedItem.isMovie() && ControllerMaster.userData.ownsMovie(rippler.linkedItem.getId())) || 
					(!rippler.linkedItem.isMovie() && ControllerMaster.userData.ownsShow(rippler.linkedItem.getId())) ) {
				if (pOver.isShowing()) {
					pOver.hide();
				}
				if (hasEntered) {
					timer.cancel();
					hasEntered = false;
				}
				Platform.runLater(() -> ControllerMaster.mainController.showSelectionDialog(rippler.linkedItem));
			}
			e.consume();
		});
		return rippler;
	}
}