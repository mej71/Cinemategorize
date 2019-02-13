package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MediaPlaylist implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name = "";
    boolean canDelete = true;
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

    String getName() {
        return name;
    }

    void removeMedia(MediaItem mi) {
        for (Iterator<MediaItem> iterator = items.iterator(); iterator.hasNext();) {
            MediaItem mik = iterator.next();
            if (mik.equals(mi)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }
        refreshPlaylistInMain();
    }

    void addItem(MediaItem mi) {
        items.add(mi);
        refreshPlaylistInMain();
    }

    private void refreshPlaylistInMain() {
        MediaPlaylist curList = ControllerMaster.mainController.playlistCombo.getValue();
        ControllerMaster.mainController.updatePlaylistCombo();
        if (ControllerMaster.userData.userPlaylists.contains(curList) && curList.getItems().size()>0) {
            ControllerMaster.mainController.playlistCombo.setValue(curList);
        } else {
            ControllerMaster.mainController.playlistCombo.setValue(null);
        }
        ControllerMaster.mainController.refreshSearch();
    }

    List<MediaItem> getItems() {
        return items;
    }

    @Override public String toString() {
        return getName();
    }

}
