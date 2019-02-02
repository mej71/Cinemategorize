package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MediaPlaylist implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name = "";
    private boolean isFavorite = false;
    private List<MediaItem> items = new ArrayList<>();

    public MediaPlaylist(String name, MediaItem mi) {
        this.name = name;
        items.add(mi);
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
    }

    List<MediaItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return getName();
    }

}
