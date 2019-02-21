package application;

import application.mediainfo.MediaItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaylist implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    public boolean canDelete = true;
    private List<MediaItem> items;

    public MediaPlaylist(String name, MediaItem mi) {
        this.name = name;
        items = new ArrayList<>();
        items.add(mi);
    }

    public MediaPlaylist(String name) {
        this.name = name;
        items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void removeMedia(MediaItem mi) {
        // Remove the current element from the iterator and the list.
        items.removeIf(mik -> mik.equals(mi));
        refreshPlaylistInMain();
    }

    public void addItem(MediaItem mi) {
        items.add(mi);
        refreshPlaylistInMain();
    }

    private void refreshPlaylistInMain() {
        MediaPlaylist curList = ControllerMaster.mainController.getPlaylistCombo().getValue();
        ControllerMaster.mainController.updatePlaylistCombo();
        if (ControllerMaster.userData.hasPlaylist(curList) && curList.getItems().size()>0) {
            ControllerMaster.mainController.getPlaylistCombo().setValue(curList);
        } else {
            ControllerMaster.mainController.getPlaylistCombo().setValue(null);
        }
        ControllerMaster.mainController.refreshSearch();
    }

    public List<MediaItem> getItems() {
        return items;
    }

    @Override public String toString() {
        return getName();
    }

}
