package application;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.net.URL;
import java.util.ResourceBundle;

public class PlaylistManagerController extends EscapableBase implements Initializable {

    @FXML private StackPane stackPane;
    @FXML private ListFlowPane<PlaylistCell<MediaPlaylist>, MediaPlaylist> playlistFlowPane;
    @FXML private ScrollPane playlistScrollPane;
    @FXML private Label noPlaylistsLabel;
    @FXML private Label emptyPlaylistLabel;
    @FXML private JFXTextField playlistNameField;
    @FXML private JFXButton addPlaylistButton;

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
        addPlaylistButton.setDisable(true);
        playlistNameField.textProperty().addListener(observable -> {
            addPlaylistButton.setDisable(playlistNameField.getText().isEmpty() || playlistNameField.getText().equals(""));
        });
    }

    void show(JFXDialog dialog) {
        super.setDialogLink(dialog);
        updatePlaylists();
        dLink.show();
    }

    void updatePlaylists() {
        updatePlaylists(false);
    }

    void updatePlaylists(boolean selectNew) {
        clearRipplers();
        playlistFlowPane.getChildren().clear();
        playlistFlowPane.addCells(PlaylistCell.createCells(ControllerMaster.userData.userPlaylists));
        if (playlistFlowPane.getChildren().size() > 0) {
            noPlaylistsLabel.setVisible(false);
            playlistFlowPane.setPrefHeight(playlistFlowPane.getChildren().size() * PlaylistCell.prefCellHeight);
            if (selectNew) { //select the newest added
                playlistFlowPane.selectCell(playlistFlowPane.getChildren().size()-1);
                playlistScrollPane.setVvalue(1);
            } else {
                playlistFlowPane.selectCell(0);
                playlistScrollPane.setVvalue(0);
            }
        } else {
            noPlaylistsLabel.setVisible(true);
        }
    }

    public void updateRipplers() {
        clearRipplers();
        for (int i = 0; i < playlistFlowPane.getSelectedItem().getItems().size(); ++i) {
            tilePane.getChildren().add(ControllerMaster.mainController.addMediaTile(playlistFlowPane.getSelectedItem().getItems().get(i)));
        }
    }

    void clearRipplers() {
        tilePane.getChildren().clear();
    }

    @FXML public void createNewPlaylist() {
        ControllerMaster.userData.userPlaylists.add(new MediaPlaylist(playlistNameField.getText()));
        updatePlaylists(true);
        playlistNameField.clear();
    }
}
