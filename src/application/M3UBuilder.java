package application;

import java.io.*;
import java.util.List;

class M3UBuilder {

    public static File buildFile(List<String> items) {
        if (items.isEmpty()) {
            return null;
        }
        new File("temp").mkdirs();
        File file = new File("temp/playlist.m3u");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            for (int i = 0; i < items.size(); ++i) {
                if (i > 0 ) {
                    writer.newLine();
                }
                writer.write(items.get(i));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
