package application.flowcells;

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
	public BooleanBinding hasChanged = Bindings.createBooleanBinding(() -> changed.getValue(), changed);
	private int selectedIndex = -1;
	private List<T> cells;

	public ListFlowPane() {
		super();
		cells = new ArrayList<>();
	}

	public void addCell(T newCell) {
		newCell.setPane(this);
		cells.add(newCell);
		this.getChildren().add(newCell);
		newCell.setOnMouseClicked(e -> {
			selectCell(cells.indexOf(newCell));
			newCell.runOnClick();
			//don't consume, flow cells have their own properties
		});
		newCell.updateItem();
	}

	public void addCells(List<T> newCells) {
		for (T t: newCells) {
			addCell(t);
		}
	}

	public void clearCells() {
		if (selectedIndex > -1 && selectedIndex < cells.size()) {
			deselectCell(cells.get(selectedIndex));
		}
		cells.clear();
		this.getChildren().clear();
	}

	public void removeCell(T cell) {
		cells.remove(cell);
		this.getChildren().remove(cell);
	}

	public List<T> getCells() {
		return cells;
	}

	public T getSelectedCell() {
		if (selectedIndex > -1 && selectedIndex < cells.size()) {
			return cells.get(selectedIndex);
		}
		return null;
	}

	public R getSelectedItem() {
		if (selectedIndex > -1 && selectedIndex < cells.size()) {
			return cells.get(selectedIndex).getItem();
		}
		return null;
	}
	
	public void setChanged(boolean b) {
		changed.set(b);
	}
	
	//highlights cell, but doesn't trigger click.  Use for external selection to prevent null selections
	public void selectCell(int newIndex) {
		if (newIndex == selectedIndex) {
			return;
		}
		//deselect old cell
		if (selectedIndex > -1 && selectedIndex < cells.size()) {
			deselectCell(cells.get(selectedIndex));
		}
		if (newIndex > -1 && newIndex < cells.size()) {
			cells.get(newIndex).pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
		}
		selectedIndex = newIndex;
		setChanged(true);
	}

	private void deselectCell(T cell) {
		cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
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
	
	private void setupScrollPane(ScrollPane sp) {
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		JFXScrollPane.smoothScrolling(sp);
	}

}
