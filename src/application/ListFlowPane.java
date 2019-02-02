package application;

import java.util.ArrayList;
import java.util.List;

import com.jfoenix.controls.JFXScrollPane;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.FlowPane;

public class ListFlowPane<T extends FlowCell<R>, R> extends FlowPane{
	private SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
	private List<R> allItems = new ArrayList<>();
	public BooleanBinding hasChanged = Bindings.createBooleanBinding(() -> changed.getValue(), changed);	
	public T selectedCell = null;
	
	public List<R> getItems() {
		return allItems;
	}
	
	public void addItem(R r) {
		if (!allItems.contains(r)) {
			allItems.add(r);
		}
	}
	
	public void setChanged(boolean b) {
		changed.set(b);
	}
	
	//highlights cell, but doesn't trigger click.  Use for external selection to prevent null selections
	public void selectCell(T cell) {
		if (selectedCell == cell) {
			return;
		}
		if (selectedCell != null) {
    		selectedCell.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
    	}
		selectedCell = cell;
		if (selectedCell != null) {
			selectedCell.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
		}
		setChanged(true);
	}
	
	//binding should be done in initialization
	public void bindWidthToNode(Control c) {
		this.minWidthProperty().bind(c.widthProperty());
		this.prefWidthProperty().bind(c.widthProperty());
		this.maxWidthProperty().bind(c.widthProperty());
		if (c instanceof ScrollPane) {
			setupScrollPane((ScrollPane) c);
		}
	}
	
	public void setupScrollPane(ScrollPane sp) {
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		JFXScrollPane.smoothScrolling(sp);
	}

}
