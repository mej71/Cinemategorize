package application;

import java.util.Timer;
import java.util.TimerTask;

import org.controlsfx.control.PopOver;

import com.jfoenix.controls.JFXListCell;

import info.movito.themoviedbapi.model.people.PersonCredit;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

public class CreditCell<T extends PersonCredit> extends JFXListCell<T> {
	
	
    private static GridPane gridPane;
    private static PopOver pOver;
    private static Label popTitle;
    private static Label popDesc;
    private static boolean hasEntered = false;
    private static final int taskMiliSeconds = 500;
	private static Timer timer;
	private T item;
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
        popDesc.setMaxWidth(450);
        popDesc.setMaxHeight(-1);
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
	
	public CreditCell() {
		super();
		if (gridPane == null) {
			init();
		}
		label = new Label();
		label.setMaxWidth(425);
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
		this.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
			if (ControllerMaster.userData.ownsMovie(item.getId()) || ControllerMaster.userData.ownsShow(item.getId()) ) {
				ControllerMaster.mainController.showSelectionDialog( ControllerMaster.userData.getMovieById(item.getId()) );
			}
		});
	}
	
	
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		this.item = item;
		setText(null);
		if (item!=null) {
			label.setText(getCellString(item));
			
			
			if (ControllerMaster.userData.ownsMovie(item.getId()) || ControllerMaster.userData.ownsShow(item.getId()) ) {
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("owned-media"), true);
				
			} else {
				this.pseudoClassStateChanged(PseudoClass.getPseudoClass("owned-media"), false);
			}
			
            setGraphic(label);
		} else {
			setGraphic(null);
		}
	}
}
