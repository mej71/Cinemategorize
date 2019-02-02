package application;

import javafx.collections.FXCollections;

import java.util.List;

public class MainOptions {

    static List<MainOptionTitles> getOptions() {
        return FXCollections.observableArrayList(MainOptionTitles.values());
    }

    public enum MainOptionTitles {

        ADDMOVIE("Add Movie"),
        MANAGEPLAYLISTS("Manage Playlists"),
        SETTINGS("Settings"),
        ABOUT("About");

        private final String toString;

        MainOptionTitles(String toString) {
            this.toString = toString;
        }

        @Override
        public String toString() {
            return this.toString;
        }
    }
}
