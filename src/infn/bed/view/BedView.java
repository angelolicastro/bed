package infn.bed.view;

import infn.bed.component.ControlPanel;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Timer;

import cnuphys.bCNU.component.InfoWindow;
import cnuphys.bCNU.component.TranslucentWindow;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.view.EventDisplayView;

import org.jlab.coda.jevio.EvioEvent;

/**
 * This abstract class defines a mouse listener and holds the control panel used
 * by subclasses. It represents the windows within the frame of the program.
 * 
 * @author David Heddle
 * @author Andy Beiter
 */
@SuppressWarnings("serial")
public abstract class BedView extends EventDisplayView {

	/**
	 * The control panel that displays information on the side.
	 */
	protected ControlPanel _controlPanel;

	/**
	 * Trigger time for hover checks
	 */
	private long minHoverTrigger = 1000; // ms

	/**
	 * Number used to start counting for a hover
	 */
	private long hoverStartCheck = -1;

	/**
	 * Field to hold the mouse event
	 */
	private MouseEvent hoverMouseEvent;

	/**
	 * Last trajectory hovering response
	 */
	protected String _lastTrajStr;

	/**
	 * Creates instance and mouse listener
	 * 
	 * @param keyVals
	 *            variable length argument list
	 */
	public BedView(Object... keyVals) {
		super(keyVals);
		createHeartbeat();
		prepareForHovering();
	}

	/**
	 * Checks if hovered for trigger time
	 */
	protected void ping() {
		// check for over
		if (hoverStartCheck > 0) {
			if ((System.currentTimeMillis() - hoverStartCheck) > minHoverTrigger) {
				hovering(hoverMouseEvent);
				hoverStartCheck = -1;
			}
		}
	}

	/**
	 * Creates heartbeat to check for hovering
	 */
	protected void createHeartbeat() {
		int delay = 1000;
		ActionListener taskPerformer = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ping();
			}
		};
		new Timer(delay, taskPerformer).start();
	}

	/**
	 * Sets up mouse listeners for hovering.
	 */
	private void prepareForHovering() {
		MouseMotionListener mml = new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent me) {
				resetHovering();
			}

			@Override
			public void mouseMoved(MouseEvent me) {
				closeHoverWindow();
				hoverStartCheck = System.currentTimeMillis();
				hoverMouseEvent = me;
			}
		};

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				resetHovering();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				resetHovering();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				resetHovering();
			}

		};

		getContainer().getComponent().addMouseMotionListener(mml);
		getContainer().getComponent().addMouseListener(ml);
	}

	/**
	 * Called to close the hover window.
	 */
	private void closeHoverWindow() {
		TranslucentWindow.closeInfoWindow();
		InfoWindow.closeInfoWindow();
	}

	/**
	 * Resets the check for hovering
	 */
	private void resetHovering() {
		hoverStartCheck = -1;
		closeHoverWindow();
	}

	/**
	 * Show or hide the annotation layer.
	 * 
	 * @param show
	 *            the value of the display flag.
	 */
	public void showAnnotations(boolean show) {
		if (getContainer().getAnnotationLayer() != null) {
			getContainer().getAnnotationLayer().setVisible(show);
		}
	}

	/**
	 * Sets whether or not we display the magnetic field layer.
	 * 
	 * @param show
	 *            the value of the display flag.
	 */
	public void showMagneticField(boolean show) {
		if (getMagneticFieldLayer() != null) {
			getMagneticFieldLayer().setVisible(show);
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
	 * @param event
	 *            the new event.
	 */
	@Override
	public void newPhysicsEvent(final EvioEvent event) {
		super.newPhysicsEvent(event);
		if (!EventControl.getInstance().isAccumulating()) {
			getUserComponent().repaint();
		}
	}

	/**
	 * When hovering, creates hovering window.
	 * 
	 * @param me Mouse event that represents the hovering.
	 */
	protected void hovering(MouseEvent me) {

		// avoid cursor
		Point p = me.getLocationOnScreen();
		p.x += 5;
		p.y += 4;

		if (_lastTrajStr != null) {
			if (TranslucentWindow.isTranslucencySupported()) {
				TranslucentWindow.info(_lastTrajStr, 0.6f, p);
			} else {
				InfoWindow.info(_lastTrajStr, p);
			}
		}
	}

}
