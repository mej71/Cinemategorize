package application;

import java.net.URL;
import java.util.ResourceBundle;

import application.controls.EscapableBase;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXToggleButton;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.util.StringConverter;

public class SettingsController extends EscapableBase implements Initializable {

	@FXML private JFXToggleButton autoLookupToggle;
	@FXML private JFXComboBox<ThemeSelection> themeComboBox;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		themeComboBox.setItems(FXCollections.observableArrayList(ThemeSelection.themes));
		
		themeComboBox.valueProperty().addListener((ov, oldVal, newVal) -> {
			if (newVal != null) {
				ThemeSelection.updateTheme(newVal);
			}
		});
		
		themeComboBox.setConverter(new StringConverter<ThemeSelection>() {

			@Override
			public String toString(ThemeSelection object) {
				return object.getThemeName();
			}

			@Override
			public ThemeSelection fromString(String string) {
				return null;
			}
			
		});
		
		autoLookupToggle.selectedProperty().addListener((observable, oldVal, newVal) -> ControllerMaster.userData.setUseAutoLookup(newVal));
		
	}

	public void show(JFXDialog d) {
		super.setDialogLink(d);
		autoLookupToggle.setSelected(ControllerMaster.userData.isUseAutoLookup());
		//load user selection, if values have changed then use default theme
		if (themeComboBox.getItems().contains(ControllerMaster.userData.getThemeSelection())) {
			themeComboBox.getSelectionModel().select(ControllerMaster.userData.getThemeSelection());
		} else {
			themeComboBox.getSelectionModel().select(0);
		}
		dLink.show();		
		
	}

}
