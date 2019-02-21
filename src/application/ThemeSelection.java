package application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThemeSelection implements Serializable {
		
	public static List<ThemeSelection> themes = new ArrayList<ThemeSelection>() {{
		add(new ThemeSelection("Perfect Blue", "perfect_blue.css"));
	}};

	//set new theme and refresh css
	//just refresh if theme is null (used for inital loading)
	public static void updateTheme(ThemeSelection theme) {
		ControllerMaster.userData.setTheme(theme);
	}
	
	private static final long serialVersionUID = 1L;
	private String themeName;
	private String fileName;
	
	private ThemeSelection(String tn, String fn) {
		setThemeName(tn);
		setFileName(fn);
	}

	public String getThemeName() {
		return themeName;
	}

	private void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getFileName() {
		return fileName;
	}

	private void setFileName(String fileName) {
		this.fileName = fileName;
	}
}