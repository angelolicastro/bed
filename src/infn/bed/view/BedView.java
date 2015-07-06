package infn.bed.view;

import infn.bed.component.ControlPanel;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import org.jlab.coda.jevio.EvioEvent;

import cnuphys.bCNU.component.InfoWindow;
import cnuphys.bCNU.component.TranslucentWindow;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.view.EventDisplayView;

/**
 * Defines a mouse listener and contains an instance of the ControlPanel object used by subclasses.
 * 
 * @author David Heddle
 * @author Andy Beiter
 * @author Angelo Licastro
 */
@SuppressWarnings("serial")
public abstract class BedView extends EventDisplayView {

	/**
	 * An instance of the ControlPanel object.
	 */
	protected ControlPanel _controlPanel;

	/**
	 * The hovering check threshold.
	 */
	private long hoveringCheckThreshold = -1;

	/**
	 * The hovering mouse event.
	 */
	private MouseEvent hoveringMouseEvent;

	/**
	 * The trajectory from the last hovering mouse event.
	 */
	private String _lastTrajectory;

	/**
	 * The constructor.
	 * 
	 * @param args A variable-length argument list.
	 */
	public BedView(Object... args) {
		super(args);
		createHeartbeat();
		prepareForHovering();
	}

	/**
	 * Checks if the hovering event exceeds the hovering check threshold and the minimum hovering trigger.
	 */
	private void ping() {
		long minimumHoveringTrigger = 1000;
		if (hoveringCheckThreshold > 0) {
			if ((System.currentTimeMillis() - hoveringCheckThreshold) > minimumHoveringTrigger) {
				createHoveringWindow(hoveringMouseEvent);
				hoveringCheckThreshold = -1;
			}
		}
	}

	/**
	 * Creates a heartbeat to check for hovering.
	 */
	private void createHeartbeat() {
		int delay = 1000;
		ActionListener taskPerformer = event -> ping();
		new Timer(delay, taskPerformer).start();
	}

	/**
	 * Sets up mouse listeners for hovering.
	 */
	private void prepareForHovering() {
		MouseMotionListener mouseMotionListener = new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent mouseEvent) {
				resetHovering();
			}

			@Override
			public void mouseMoved(MouseEvent mouseEvent) {
				closeHoverWindow();
				hoveringCheckThreshold = System.currentTimeMillis();
				hoveringMouseEvent = mouseEvent;
			}
		};

		MouseListener mouseListener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				resetHovering();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) {
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) {
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				resetHovering();
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent) {
				resetHovering();
			}

		};

		getContainer().getComponent().addMouseMotionListener(mouseMotionListener);
		getContainer().getComponent().addMouseListener(mouseListener);
	}

	/**
	 * Closes the hovering window.
	 */
	private void closeHoverWindow() {
		TranslucentWindow.closeInfoWindow();
		InfoWindow.closeInfoWindow();
	}

	/**
	 * Resets the hovering check threshold.
	 */
	private void resetHovering() {
		hoveringCheckThreshold = -1;
		closeHoverWindow();
	}

	/**
	 * Sets the visibility of the annotation layer.
	 * 
	 * @param isVisible The status of visibility. true if visible, false otherwise.
	 */
	public void showAnnotations(boolean isVisible) {
		if (getContainer().getAnnotationLayer() != null) {
			getContainer().getAnnotationLayer().setVisible(isVisible);
		}
	}

	/**
	 * Sets the visibility of the magnetic field layer.
	 * 
	 * @param isVisible The status of visibility. true if visible, false otherwise.
	 */
	public void showMagneticField(boolean isVisible) {
		if (getMagneticFieldLayer() != null) {
			getMagneticFieldLayer().setVisible(isVisible);
		}
	}

	/**
	 * Returns the sector that contains a point.
	 * 
	 * @param worldPoint A point in the corresponding world.
	 * @return The sector that contains the point, if any.
	 */
	public abstract int getSector(Point2D.Double worldPoint);

	/**
	 * A new event has arrived from jevio. This is called by the generic
	 * EventContol object. By the time we get here any detector specific parsing
	 * on this event should be done, provided you haven't put the detector
	 * specific parsing in a separate thread. This is the actual event not a
	 * copy so it should not be modified.
	 * 
	 * @param event A new JEVIO event.
	 */
	@Override
	public void newPhysicsEvent(final EvioEvent event) {
		super.newPhysicsEvent(event);
		if (!EventControl.getInstance().isAccumulating()) {
			getUserComponent().repaint();
		}
	}

	/**
	 * Creates a hovering window.
	 * 
	 * @param mouseEvent A hovering MouseEvent.
	 */
	private void createHoveringWindow(MouseEvent mouseEvent) {
		Point p = mouseEvent.getLocationOnScreen();
		p.x = p.x + 5;
		p.y = p.y + 4;
		if (_lastTrajectory != null) {
			if (TranslucentWindow.isTranslucencySupported()) {
				TranslucentWindow.info(_lastTrajectory, 0.6f, p);
			} else {
				InfoWindow.info(_lastTrajectory, p);
			}
		}
	}

}