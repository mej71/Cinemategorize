package application;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXScrollPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.net.URL;
import java.util.ResourceBundle;

public class PlaylistManagerController extends EscapableBase implements Initializable {

    @FXML private StackPane stackPane;
    @FXML private ListFlowPane<PlaylistCell<MediaPlaylist>, MediaPlaylist> playlistFlowPane;
    @FXML private ScrollPane playlistScrollPane;

    private MovieScrollPane movieScrollPane;
    private TilePane tilePane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playlistFlowPane.bindWidthToNode(playlistScrollPane);
        playlistFlowPane.hasChanged.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue && newValue != null && newValue) {
                playlistFlowPane.setChanged(false);
                updateRipplers();
            }
        });
        tilePane = new TilePane();
        tilePane.setPadding(new Insets(5, 5, 5, 5));
        tilePane.setVgap(15);
        tilePane.setHgap(10);
        ControllerMaster.tileAnimator.observe(tilePane.getChildren());

        movieScrollPane = new MovieScrollPane();
        movieScrollPane.setFitToWidth(true);
        movieScrollPane.setFitToHeight(true);
        movieScrollPane.setContent(tilePane);
        JFXSmoothScroll.smoothScrolling(movieScrollPane);
        movieScrollPane.getStyleClass().add("movie-scroll-pane");
        stackPane.getChildren().add(movieScrollPane);
    }

    void show(JFXDialog dialog) {
        super.setDialogLink(dialog);
        playlistFlowPane.getChildren().clear();
        playlistFlowPane.getChildren().addAll(PlaylistCell.createCells(ControllerMaster.userData.userPlaylists, playlistFlowPane));
        if (playlistFlowPane.getChildren().size() > 0) {
            playlistFlowPane.selectCell((PlaylistCell<MediaPlaylist>)playlistFlowPane.getChildren().get(0));
        } else {
            clearRipplers();
        }
        dLink.show();
    }

    public void updateRipplers() {
        clearRipplers();
        for (int i = 0; i < playlistFlowPane.selectedCell.item.getItems().size(); ++i) {
            tilePane.getChildren().add(ControllerMaster.mainController.addMediaTile(playlistFlowPane.selectedCell.item.getItems().get(i)));
        }
    }

    void clearRipplers() {
        tilePane.getChildren().clear();
    }

    @FXML public void createNewPlaylist() {

    }
}
