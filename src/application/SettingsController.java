package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXToggleButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
		
		themeComboBox.valueProperty().addListener(new ChangeListener<ThemeSelection>() {

			@Override
			public void changed(ObservableValue<? extends ThemeSelection> ov, ThemeSelection oldVal, ThemeSelection newVal) {
				if (newVal != null) {
					ThemeSelection.updateTheme(newVal);
				}
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
		
		autoLookupToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldVal, Boolean newVal) {
				if (newVal != null) {
					ControllerMaster.userData.useAutoLookup = newVal;
				}				
			}
			
		});
		
	}

	public void show(JFXDialog d) {
		super.setDialogLink(d);
		autoLookupToggle.setSelected(ControllerMaster.userData.useAutoLookup);
		//load user selection, if values have changed then use default theme
		if (themeComboBox.getItems().contains(ControllerMaster.userData.themeSelection)) {
			themeComboBox.getSelectionModel().select(ControllerMaster.userData.themeSelection);
		} else {
			themeComboBox.getSelectionModel().select(0);
		}
		dLink.show();		
		
	}

}
