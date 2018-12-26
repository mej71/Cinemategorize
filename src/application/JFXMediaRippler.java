package application;

import java.util.Timer;
import java.util.TimerTask;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.jfoenix.controls.JFXRippler;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import info.movito.themoviedbapi.model.people.PersonCrew;
import javafx.application.Platform;
import javafx.geometry.HPos;
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
	
	public static GridPane gridPane;
	public static PopOver pOver;
	public static Label titleLabel;
	public static Label descLabel;
	public static Label directorsLabel;
	public static Label actorsLabel;
	public static boolean hasEntered = false;
	public static final int taskMiliSeconds = 500;
	public static Timer timer;
	
	public static JFXMediaRippler tempRippler;
	
	
	
	public MediaItem linkedItem;
	public ImageView iView;
	public StackPane paneChild;
	
	public static void init() {
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
        titleLabel.getStyleClass().add("popup_descript");
        descLabel = new Label();
        descLabel.setMaxWidth(250);
        descLabel.setMaxHeight(-1);
        descLabel.setWrapText(true);
        descLabel.getStyleClass().add("popup_descript");
        directorsLabel = new Label();
        directorsLabel.setMaxWidth(250);
        directorsLabel.getStyleClass().add("popup_info");
        actorsLabel = new Label();
        actorsLabel.getStyleClass().add("popup_info");
        actorsLabel.setMaxWidth(250);
        actorsLabel.setWrapText(true);
        gridPane.add(titleLabel, 0, 0, 5, 1);
		GridPane.setHalignment(titleLabel, HPos.CENTER);
		gridPane.add(descLabel, 0, 1, 5, 7);
		gridPane.add(directorsLabel, 0, 8, 5, 1);
		gridPane.add(actorsLabel, 0, 9, 5, 2);
		
		//show popover for quick movie info when mouse is over media pane
		pOver = new PopOver(gridPane);
		pOver.setFadeInDuration(Duration.ONE);
		pOver.setFadeOutDuration(Duration.ZERO);
	}
	
	private static TimerTask getTimerTask() {
		return new TimerTask() {

			@Override
			public void run() {
				if (hasEntered && !popIsShowing()) {
					Platform.runLater(() -> {
						pOver.show(tempRippler);
					});
					this.cancel();
					hasEntered = false;
				}				
			}			
		};
	}
	
	public static void forceHidePopOver() {
		pOver.hide();
		if (hasEntered) {
			timer.cancel();
			hasEntered = false;
		}
	}
	
	public static boolean popIsShowing() {
		return pOver.isShowing();
	}
	
	public JFXMediaRippler(StackPane control, ImageView iv){
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
	
	public static JFXMediaRippler createBasicRippler(TilePane tilePane, MovieScrollPane scrollPane, HamburgerBackArrowBasicTransition burgerTask) {
		StackPane paneChild = new StackPane();
		ImageView iView = new ImageView();
		
		//user a smaller resolution for smaller scale.  looks better for smaller posters
		paneChild.getChildren().add(iView);
		iView.fitWidthProperty().bind(paneChild.widthProperty());
		iView.fitHeightProperty().bind(paneChild.heightProperty());
		
		
		JFXMediaRippler rippler = new JFXMediaRippler(paneChild, iView);
		rippler.setRipplerFill((Paint.valueOf("black")));
		rippler.setMaskType(JFXRippler.RipplerMask.RECT);
		rippler.setPosition(RipplerPos.FRONT);
		paneChild.setMaxWidth(139);
		paneChild.setMaxHeight(208);
		paneChild.resize(139, 208);
		paneChild.addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> { 
			if ( (burgerTask != null && burgerTask.getRate() > 0) || (scrollPane !=null && scrollPane.isScrolling) || pOver.isShowing()) {
				return;
			}
			MediaItem mi = rippler.linkedItem;
        	String lastDate = "";
        	if (!mi.isMovie()) {
        		lastDate = " - " + mi.getLastAirDate();
        		JFXMediaRippler.directorsLabel.setText(mi.getNumSeasons() + " seasons");
        	} else {
        		String directors = "";
	        	PersonCrew crew;
	        	for (int i = 0; i < mi.getCrew().size(); ++i) {
	        		crew = mi.getCrew().get(i);
	        		if (crew.getJob().equalsIgnoreCase("Director")) {
	        			if (directors.equals("")) {
	        				directors+=crew.getName();
	        			} else {
	        				directors+=", "+crew.getName();
	        			}
	        		}
	        	}
	        	JFXMediaRippler.directorsLabel.setText("Directed by: " + directors);
        	}
        	JFXMediaRippler.titleLabel.setText(mi.getTitle()+ " (" + mi.getReleaseDate().substring(0, 4) + lastDate + ")");
        	JFXMediaRippler.descLabel.setText(mi.getOverview());
        	
        	String cast = "";
        	for (int i = 0; i<4; ++i) {
        		if (i==mi.getCast().size()) {
        			break;
        		}
        		if (i==0) {
        			cast+=mi.getCast().get(i).getName();
        		} else {
        			cast+=", " + mi.getCast().get(i).getName();
        		}
        	}
        	JFXMediaRippler.actorsLabel.setText("Starring: " + cast);
			int columnsWide = 0;
			int listPos = -1;
			int lastX = (int)((JFXRippler)tilePane.getChildren().get(0)).getLayoutX();
			boolean foundMax = false;
			for (int i = 0; i < tilePane.getChildren().size(); ++i) {
				if (!foundMax) {
					if (((JFXMediaRippler)tilePane.getChildren().get(i)).getLayoutX()<lastX) {
						foundMax = true;
						if (listPos != -1) {
							break;
						}
					} else {
						++columnsWide;
						lastX = (int)((JFXRippler)tilePane.getChildren().get(i)).getLayoutX();
					}
				}
				
				if (listPos == -1) {
					if (((JFXMediaRippler)tilePane.getChildren().get(i)).linkedItem.getId()==rippler.linkedItem.getId()) {
						listPos = i;
						if (foundMax) {
							break;
						}
					}
				}
			}			
			int columnPos = listPos % (columnsWide);			
			if (columnPos==columnsWide-1 && columnsWide != 1) { //movies on the rightmost side should have the popover on the left to keep it on the screen
				pOver.getStyleClass().remove("pop_over_left");
				pOver.getStyleClass().add("pop_over_right");
				pOver.arrowLocationProperty().set(ArrowLocation.RIGHT_CENTER);
			} else {
				pOver.getStyleClass().remove("pop_over_right");
				pOver.getStyleClass().add("pop_over_left");
				pOver.arrowLocationProperty().set(ArrowLocation.LEFT_CENTER);
			}
			if (!hasEntered) {
				timer = new Timer();
				hasEntered = true;
				tempRippler = rippler;
				timer.schedule(getTimerTask(), taskMiliSeconds);
			}
		});
		paneChild.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
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
				Platform.runLater(() -> {
					ControllerMaster.mainController.showSelectionDialog(rippler.linkedItem);
				});
			}
		});
		return rippler;
	}
}