package application;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;

public class FlowCell<T> extends Control {
	
	private static final String DEFAULT_STYLE_CLASS = "flow-cell";
	
	protected T item;
	protected ListFlowPane pane;
	protected EventHandler<MouseEvent> clickHandler = event -> runOnClick();
	
	public FlowCell(T item, ListFlowPane pane) {
		super();
		pane.addItem(item);
		getStyleClass().add(DEFAULT_STYLE_CLASS);
		this.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
		this.item = item;
		this.pane = pane;
		updateItem();
	}
	
	@Override
	public Skin<?> createDefaultSkin() {
		return new FlowCellSkin(this);
	}
	
	public T getItem() {
		return item;
	}
	
	public ListFlowPane getPane() {
		return pane;
	}
	
	public <R extends Node> void setGraphic(R graphic) {
		this.getChildren().clear();
		this.getChildren().add(graphic);
	}
	
	//must be overriden in child classes
	protected <R extends Node> R getGraphic() {
		if (this.getChildren().isEmpty()) {
			return null;
		}
		return (R)this.getChildren().get(0);
	}
	
	public void setText(String text) {
		this.getChildren().clear();
		this.getChildren().add(new Label(text));
	}

	//update item
	//super to automatically refresh selection handler
	protected void updateItem() {
		removeSelectionHandler();
		addSelectionHandler();
	}
	
	protected void addSelectionHandler() {
		this.setOnMouseClicked(clickHandler);
	}
	
	protected void removeSelectionHandler() {
		this.removeEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
	}
	
	protected void runOnClick() {
		//must select season & episode for tv
    	getPane().selectCell(this);
	}
	
}
