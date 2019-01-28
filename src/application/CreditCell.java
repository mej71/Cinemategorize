package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.controlsfx.control.PopOver;

import info.movito.themoviedbapi.model.people.PersonCredit;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

public class CreditCell<T extends PersonCredit> extends FlowCell<T> {
	
	
    private static GridPane gridPane;
    private static PopOver pOver;
    private static Label popTitle;
    private static Label popDesc;
    private static boolean hasEntered = false;
    private static final int taskMiliSeconds = 500;
	private static Timer timer;
	public static int prefCellHeight = 22;
	private Label label;
	
	private static CreditCell<?> tempCell;
	
	private static void init() {
		gridPane = new GridPane();
		final int numCols = 5;
        final int numRows = 8;
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
        popTitle = new Label();
        popTitle.setId("pop-title");
        popDesc = new Label();
        popDesc.setMaxWidth(250);
        popDesc.setMaxHeight(150);
        popDesc.setId("pop-descript");
        
        gridPane.add(popTitle, 0, 0, 5, 1);
		GridPane.setHalignment(popTitle, HPos.CENTER);
		gridPane.add(popDesc, 0, 1, 5, 7);
		
		//show popover for quick movie info when mouse is over media pane
		pOver = new PopOver(gridPane);
		pOver.setFadeInDuration(Duration.ONE);
		pOver.setFadeOutDuration(Duration.ZERO);
		
	}
	
	private static TimerTask getTimerTask() {
		return new TimerTask() {

			@Override
			public void run() {
				if (hasEntered) {
					Platform.runLater(() -> {
						pOver.show(tempCell);
					});
					this.cancel();
					hasEntered = false;
				}				
			}			
		};
	}
	
	private String getCellString(T item) {
    	String title = "";
    	String year = "";
    	if (item.getMovieTitle() != null && !item.getMovieTitle().isEmpty()) {
			title = item.getMovieTitle();
			year = item.getReleaseDate();
		} else {
			title = item.getSeriesName();	
			year = item.getFirstAirDate();
		}				
		if (year != null && year.length()>4) {
			year = " (" + year.substring(0, 4) + ")";
		} else {
			year = " (N/A)";
		}
		String role = item.getCharacter();
		if (role == null || role.isEmpty()) {
			role = "";
		} else {
			role = " as " + role;
		}
		return title + year + role;
    }
	
	public CreditCell(T item, ListFlowPane<CreditCell<T>, T> pane) {
		super(item, pane);
		if (gridPane == null) {
			init();
		}
		
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, (e) -> { 
			popTitle.setText(getCellString(item));
			if (!item.getOverview().isEmpty()) {
				popDesc.setText(item.getOverview());
			} else {
				popDesc.setText("Description not available");
			}
			if (!hasEntered) {
				timer = new Timer();
				hasEntered = true;
				tempCell = this;
				timer.schedule(getTimerTask(), taskMiliSeconds);
			}
			
		});
		this.addEventHandler(MouseEvent.MOUSE_EXITED, (e) -> {
			pOver.hide();
			if (hasEntered) {
				timer.cancel();
				hasEntered = false;
			}
			tempCell = null;
		});
	}
	
	
	
	@Override
	protected void updateItem() {
		super.updateItem();
		if (item != null) {
			HBox hbox = new HBox();
			hbox.setPrefHeight(prefCellHeight);
			hbox.setMaxHeight(prefCellHeight);
			hbox.minWidthProperty().bind(getPane().widthProperty());
			hbox.prefWidthProperty().bind(getPane().widthProperty());
			hbox.maxWidthProperty().bind(getPane().widthProperty());
			label = new Label();
			label.setText(getCellString(item));
			hbox.setAlignment(Pos.CENTER_LEFT);
			
			
			if (ControllerMaster.userData.ownsMovie(item.getId()) || ControllerMaster.userData.ownsShow(item.getId()) ) {
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("owned-media"), true);
				
			} else {
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("owned-media"), false);
			}
			hbox.getChildren().add(label);
            setGraphic(hbox);
		}
	}
	
	@Override
	protected Label getGraphic() {
		return super.getGraphic();
	}
	
	@Override
	protected void runOnClick() {
		super.runOnClick();
		if (item.getMediaType().equalsIgnoreCase("TV") &&  ControllerMaster.userData.ownsShow(item.getId())) {
			ControllerMaster.mainController.showSelectionDialog( ControllerMaster.userData.getTvById(item.getMediaId()) );
		}
		if (!item.getMediaType().equalsIgnoreCase("TV") && ControllerMaster.userData.ownsMovie(item.getId())) {
			ControllerMaster.mainController.showSelectionDialog( ControllerMaster.userData.getMovieById(item.getMediaId()));		
		}
	}
	
	public static <T extends PersonCredit> List<CreditCell<T>> createCells(List<T> credits, ListFlowPane<CreditCell<T>,T> pane) {
		List<CreditCell<T>> cells = new ArrayList<CreditCell<T>>();
		for (T credit : credits) {
			cells.add(new CreditCell<T>(credit, pane));
		}
		return null;
		
	}
}
