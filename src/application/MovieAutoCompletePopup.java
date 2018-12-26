package application;

import com.jfoenix.controls.JFXAutoCompletePopup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.stage.Window;

@SuppressWarnings("rawtypes")
public class MovieAutoCompletePopup extends JFXAutoCompletePopup{

	private final ObjectProperty<EventHandler<MovieAutoCompleteEvent>> selectionHandler = new SimpleObjectProperty<>();
	
	public MovieAutoCompletePopup() {
		super();
	}
	
	
	@Override
    protected Skin<?> createDefaultSkin() {
        return new MovieAutoCompletePopupSkin(this);
	}
	
	public void show(Node node){
        if(!isShowing()){
            if(node.getScene() == null || node.getScene().getWindow() == null)
                throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
            Window parent = node.getScene().getWindow();
            this.show(parent, parent.getX() + node.localToScene(0, 0).getX() +
                              node.getScene().getX(),
                parent.getY() + node.localToScene(0, 0).getY() +
                node.getScene().getY() + ((Region)node).getHeight());
            ((MovieAutoCompletePopupSkin)getSkin()).animate();
        }
	}
	
	public EventHandler<MovieAutoCompleteEvent> getSelectionHandler() {
        return selectionHandler.get();
    }

	public void setMovieSelectionHandler(EventHandler<MovieAutoCompleteEvent> selectionHandler){
        this.selectionHandler.set(selectionHandler);
    }
}
