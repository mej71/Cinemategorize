package application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Duration;

public class TileAnimator implements ChangeListener<Number>, ListChangeListener<Node> {

  private Map<Node, TranslateTransition> nodeHTransitions = new HashMap<>();
  private Map<Node, TranslateTransition> nodeVTransitions = new HashMap<>();
  private final int transitionSpeed = 150;
  
  public void observe(ObservableList<Node> nodes) {
    for (Node node : nodes) {
      this.observe(node);
    }
    nodes.addListener(this);
  }
  
  public void observe(Node n) {
	n.layoutXProperty().addListener(this);
	n.layoutYProperty().addListener(this);
  }

  public void unobserve(ObservableList<Node> nodes) {
	for (Node node : nodes) {
      this.unobserve(node);
	}
	nodes.removeListener(this);    
  }

  public void unobserve(Node n) {
    n.layoutXProperty().removeListener(this);
    n.layoutYProperty().removeListener(this);
  }

  @Override
  public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
    final double delta = newValue.doubleValue() - oldValue.doubleValue();
    //ignore very slight changes in scale.  transition looks bad
    if (delta < 50) {
    	return;
    }
    final DoubleProperty doubleProperty = (DoubleProperty) ov;
    final Node node = (Node) doubleProperty.getBean();

    
    TranslateTransition t;
    switch (doubleProperty.getName()) {
      case  "layoutX":
        t = nodeHTransitions.get(node);
        if (t == null) {
          t = new TranslateTransition(Duration.millis(transitionSpeed), node);
          t.setToX(0);
          nodeHTransitions.put(node, t);
        }
        t.setFromX(node.getTranslateX() - delta);
        node.setTranslateX(node.getTranslateX() - delta);
        break;

      default: // "layoutY"
        t = nodeVTransitions.get(node);
        if (t == null) {
          t = new TranslateTransition(Duration.millis(transitionSpeed), node);
          t.setToY(0);
          nodeVTransitions.put(node, t);
        }
        t.setFromY(node.getTranslateY() - delta);
        node.setTranslateY(node.getTranslateY() - delta);
    }
    t.play();
  }

  @Override
  public void onChanged(Change<? extends Node> change) {
    while (change.next()) {
      if (change.wasAdded()) {
        for (Node node : (List<Node>) change.getAddedSubList()) {
          this.observe(node);
        }
      } else if (change.wasRemoved()) {
        for (Node node : (List<Node>) change.getRemoved()) {
          this.unobserve(node);
        }
      }
    }
  }
}