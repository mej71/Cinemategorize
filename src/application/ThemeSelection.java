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
		if (theme != null) {
			//remove last stylesheet so small modifications aren't overridden by it
			ControllerMaster.mainController.cinemaScene.getStylesheets().remove(theme.getClass().getClassLoader().getResource(ControllerMaster.userData.themeSelection.getFileName()).toExternalForm());
			ControllerMaster.userData.themeSelection = theme;
		}
		ControllerMaster.mainController.cinemaScene.getStylesheets().add(ControllerMaster.userData.getClass().getClassLoader().getResource(ControllerMaster.userData.themeSelection.getFileName()).toExternalForm());
	}
	
	private static final long serialVersionUID = 1L;
	private String themeName;
	private String fileName;
	
	public ThemeSelection(String tn, String fn) {
		setThemeName(tn);
		setFileName(fn);
	}

	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}