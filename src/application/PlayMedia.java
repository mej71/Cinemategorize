package application;

import application.mediainfo.MediaItem;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayMedia {

    public static void playMedia(MediaItem mi) {
        openFile(mi.getFullFilePath());
    }

    public static void playSeason(MediaItem mi, int seasonNum, int startEp){
        PlayMedia.playSeason(mi, seasonNum, startEp, false);
    }

    public static void playSeason(MediaItem mi, int curSeason, int curEp, boolean allSeasons){
        List<String> previousFilePaths = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        String tempPath;
        int startSeason = (allSeasons)? 1 : curSeason;
        int endSeason = (allSeasons) ? mi.getNumSeasons() : curSeason;
        for (int i = startSeason; i <= endSeason; ++i) {
            for (int j = 1; j < mi.getEpisodes(i).size(); ++j) {
                tempPath = mi.getFullFilePath(i, j);
                //skip empty paths
                if (!tempPath.isEmpty()) {
                    if (i <= curSeason && j < curEp) {
                        previousFilePaths.add(tempPath);
                    } else {
                        filePaths.add(tempPath);
                    }
                }
            }
        }
        filePaths.addAll(previousFilePaths);
        File file = M3UBuilder.buildFile(filePaths);
        if (file != null) {
            openFile(file.getAbsolutePath());
        }
    }

    public static void playPlaylist(MediaItem startItem, MediaPlaylist playlist){

    }

    private static void openFile(String path) {
        if (Desktop.isDesktopSupported()) {
            try {
                File file = new File(path);
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("The filepath " + path + " is invalid.");
            }
        } else {
            System.out.println("Unsupported OS, please post this bug and your OS at github");
        }
    }

}
