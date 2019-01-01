package application;

import java.util.function.Function;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class JFXSmoothScroll  {	
	
	private static ScrollBar getScrollbarComponent(ListView<?> control, Orientation orientation) {
	    Node n = control.lookup(".scroll-bar");
	    if (n instanceof ScrollBar) {
	        final ScrollBar bar = (ScrollBar) n;
	        if (bar.getOrientation().equals(orientation)) {
	            return bar;
	        }
	    }

	    return null;
	}	
	
	public static void smoothScrollingListView(ListView<?> listView, double speed) {
		smoothScrollingListView(listView, speed, Orientation.VERTICAL, bounds -> bounds.getHeight());
	}
	
	public static void smoothHScrollingListView(ListView<?> listView, double speed) {
		smoothScrollingListView(listView, speed, Orientation.HORIZONTAL, bounds -> bounds.getHeight());
	}
	
	public static void smoothScrolling(MovieScrollPane scrollPane) {
        customScrolling(scrollPane, scrollPane.vvalueProperty(), bounds -> bounds.getHeight());
	}		
	
	public static void smoothHScrolling(MovieScrollPane scrollPane) {
	        customScrolling(scrollPane, scrollPane.hvalueProperty(), bounds -> bounds.getWidth());
	}

	private static void customScrolling(MovieScrollPane scrollPane, DoubleProperty scrollDriection, Function<Bounds, Double> sizeFunc) {
        final double[] frictions = {0.99, 0.1, 0.05, 0.04, 0.03, 0.02, 0.01, 0.04, 0.01, 0.008, 0.008, 0.008, 0.008, 0.0006, 0.0005, 0.00003, 0.00001};
        final double[] pushes = {1};
        final double[] derivatives = new double[frictions.length];
        Timeline timeline = new Timeline();
        final EventHandler<MouseEvent> dragHandler = event -> timeline.stop();
        final EventHandler<ScrollEvent> scrollHandler = event -> {
            if (event.getEventType() == ScrollEvent.SCROLL) {
                int direction = event.getDeltaY() > 0 ? -1 : 1;
                for (int i = 0; i < pushes.length; i++) {
                    derivatives[i] += direction * pushes[i];
                }
                if (timeline.getStatus() == Animation.Status.STOPPED) {
                    timeline.play();
                }
                //hide popover when scrolling
                
                if (JFXMediaRippler.popIsShowing()) {
                	
					JFXMediaRippler.forceHidePopOver();
				}
                event.consume();
            }
        };
        addHandlers(scrollPane.getContent(), dragHandler, scrollHandler);
        
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3), (event) -> {
            for (int i = 0; i < derivatives.length; i++) {
                derivatives[i] *= frictions[i];
            }
            for (int i = 1; i < derivatives.length; i++) {
                derivatives[i] += derivatives[i - 1];
            }
            double dy = derivatives[derivatives.length - 1];
            double size = sizeFunc.apply(scrollPane.getContent().getLayoutBounds());
            scrollDriection.set(Math.min(Math.max(scrollDriection.get() + dy / size, 0), 1));
            if (Math.abs(dy) < 1 || scrollPane.getVvalue()==scrollPane.getVmax() || scrollPane.getVvalue()==scrollPane.getVmin()) {
            	scrollPane.isScrolling = false;
            	if (Math.abs(dy) < 0.0001) {
                    timeline.stop();
                } 
            } else {
            	scrollPane.isScrolling = true;
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
	}
	
	private  static void smoothScrollingListView(ListView<?> listView, double speed, Orientation orientation, Function<Bounds, Double> sizeFunc) {
		ScrollBar scrollBar = getScrollbarComponent(listView, orientation);
		if (scrollBar == null) {
			return;
		}
		final double[] frictions = {0.99, 0.1, 0.05, 0.04, 0.03, 0.02, 0.01, 0.04, 0.01, 0.008, 0.008, 0.008, 0.008, 0.0006, 0.0005, 0.00003, 0.00001};
        final double[] pushes = {speed};
        final double[] derivatives = new double[frictions.length];
        final double[] lastVPos = {0};
        Timeline timeline = new Timeline();
        final EventHandler<MouseEvent> dragHandler = event -> timeline.stop();
        final EventHandler<ScrollEvent> scrollHandler = event -> {
            if (event.getEventType() == ScrollEvent.SCROLL) {
            	scrollBar.valueProperty().set(lastVPos[0]);
            	double direction = event.getDeltaY() > 0 ? -1 : 1;
                for (int i = 0; i < pushes.length; i++) {
                    derivatives[i] += direction * pushes[i];
                }
                if (timeline.getStatus() == Animation.Status.STOPPED) {
                    timeline.play();
                }
                event.consume();
            }
            
        };
        addHandlers(scrollBar, dragHandler, scrollHandler);
        
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3), (event) -> {
			for (int i = 0; i < derivatives.length; i++) {
			    derivatives[i] *= frictions[i];
			}
			for (int i = 1; i < derivatives.length; i++) {
			    derivatives[i] += derivatives[i - 1];
			}
			double dy = derivatives[derivatives.length - 1];
			double size = sizeFunc.apply(scrollBar.getLayoutBounds());
			scrollBar.valueProperty().set(Math.min(Math.max(scrollBar.getValue() + dy / size, 0), 1));
			lastVPos[0] = scrollBar.getValue();
			if (Math.abs(dy) < 1) {
				if (Math.abs(dy) < 0.01) {
			        timeline.stop();
			    } 
			} 
		}));
		timeline.setCycleCount(Animation.INDEFINITE);	    
	}
	
	private static void addHandlers(Node node, EventHandler<MouseEvent> dragHandler, EventHandler<ScrollEvent> scrollHandler) {
		if (node.getParent() != null) {
			node.getParent().addEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
			node.getParent().addEventHandler(ScrollEvent.ANY, scrollHandler);
        }
		node.parentProperty().addListener((o,oldVal, newVal)->{
            if (oldVal != null) {
                oldVal.removeEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
                oldVal.removeEventHandler(ScrollEvent.ANY, scrollHandler);
            }
            if (newVal != null) {
                newVal.addEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
                newVal.addEventHandler(ScrollEvent.ANY, scrollHandler);
            }
        });
	}
	 
}
