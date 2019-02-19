package application;

import com.jfoenix.controls.JFXRippler;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;

public class FlowCell<T> extends Control {
	
	private static final String DEFAULT_STYLE_CLASS = "flow-cell";
	private ListFlowPane pane;
	protected T item;
	protected EventHandler<MouseEvent> clickHandler = event -> runOnClick();
	
	public FlowCell(T item) {
		super();
		getStyleClass().add(DEFAULT_STYLE_CLASS);
		this.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), false);
		this.item = item;
	}

	public void setPane(ListFlowPane pane) {
		this.pane = pane;
	}

	public ListFlowPane getPane() {
		return pane;
	}
	
	@Override
	public Skin<?> createDefaultSkin() {
		return new FlowCellSkin(this);
	}
	
	public T getItem() {
		return item;
	}
	
	protected <R extends Node> void setGraphic(R graphic) {
		this.getChildren().clear();
		this.getChildren().add(new JFXRippler(graphic));
	}

	private JFXRippler getGraphic() {
		if (this.getChildren().isEmpty()) {
			return null;
		}
		return (JFXRippler)this.getChildren().get(0);
	}
	
	public void setText(String text) {
		this.getChildren().clear();
		this.getChildren().add(new Label(text));
	}

	//update item
	//super to automatically refresh selection handler
	protected void updateItem() {}

	protected void runOnClick() {}
	
}
