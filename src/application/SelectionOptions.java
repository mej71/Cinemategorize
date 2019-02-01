package application;

import java.util.ArrayList;
import java.util.List;

public class SelectionOptions {

    public static List<SelectionOptionTitles> getTvOptions() {
        List<SelectionOptionTitles> options = new ArrayList<>(getBaseOptions());
        options.add(SelectionOptionTitles.REMOVEEPISODE);
        options.add(SelectionOptionTitles.REMOVESHOW);
        return options;
    }

    public static List<SelectionOptionTitles> getMovieOptions() {
        List<SelectionOptionTitles> options = new ArrayList<>(getBaseOptions());
        options.add(SelectionOptionTitles.REMOVEMOVIE);
        return options;
    }

    public static List<SelectionOptionTitles> getBaseOptions() {
        List<SelectionOptionTitles> options = new ArrayList<>();
        options.add(SelectionOptionTitles.ADDTOPLAYLIST);
        options.add(SelectionOptionTitles.CHANGELOCATION);
        options.add(SelectionOptionTitles.MANUALEDIT);
        return options;
    }


    public enum SelectionOptionTitles {

        ADDTOPLAYLIST("Add to Playlist"),
        CHANGELOCATION("Change File Location"),
        MANUALEDIT("Manual Lookup"),
        REMOVEEPISODE("Remove Episode"),
        REMOVESHOW("Remove Show"),
        REMOVEMOVIE("Remove Movie");

        private final String toString;

        SelectionOptionTitles(String toString) {
            this.toString = toString;
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }
}
