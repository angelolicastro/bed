package infn.bed.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.Styled;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.X11Colors;
import infn.bed.frame.Bed;
import infn.bed.geometry.GeometricConstants;
import infn.bed.component.ControlPanel;
import infn.bed.item.SideViewBar;

/**
 * This class handles the drawing of the side view of the bars. It orders the
 * bars in a 3x3 grid and numbers them going from left-to-right, top-to-bottom.
 * This class is used to display graphs of waveform, so when a bar is clicked
 * on, a graph of the waveshape for the left and right PMTs is displayed.
 * 
 * @author Andy Beiter
 * 
 */
@SuppressWarnings("serial")
public class BarSideView extends BedView {

	/**
	 * A bar rectangle for each bar.
	 */
	private Rectangle2D.Double _barWorldRects[];

	/**
	 * Used for drawing and customizing the bar rectangles.
	 */
	private Styled _barStyle;

	/**
	 * Used for the before draw for rectangles (not very important).
	 */
	private IDrawable _beforeDraw;

	/**
	 * Array of bar instances. These handle displaying hits and info.
	 */
	private SideViewBar _superLayerItems[];

	/**
	 * The grid used for positioning the rectangles. The shape is a 3x3 grid.
	 */
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			0.0, 0.0, 3.0, 3.0);

	/**
	 * Constructor used to set up parameters and to handle the drawing and
	 * adding of bar rectangles and objects. This also adds a mouse listener to
	 * check for clicks on the rectangles.
	 * 
	 * @param keyVals
	 *            used in the super class (BedView) to set up parameters of this
	 *            view
	 */
	private BarSideView(Object... keyVals) {
		super(keyVals);
		checkForClick();
		setbarWorldRects();
		setBeforeDraw();
		setAfterDraw();
		addItems();
	}

	/**
	 * Method used to create an instance of this class
	 * 
	 * @return the new instance of the view
	 */
	public static BarSideView createBarSideView() {
		BarSideView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.5);

		// create the view
		view = new BarSideView(
				AttributeType.WORLDSYSTEM,
				_defaultWorldRectangle,
				AttributeType.WIDTH,
				d.width, // container width, not total view width
				AttributeType.HEIGHT,
				d.height, // container height, not total view width
				AttributeType.TOOLBAR, true, AttributeType.TOOLBARBITS,
				BaseToolBar.NODRAWING & ~BaseToolBar.RANGEBUTTON
						& ~BaseToolBar.TEXTFIELD
						& ~BaseToolBar.CONTROLPANELBUTTON
						& ~BaseToolBar.TEXTBUTTON & ~BaseToolBar.DELETEBUTTON,
				AttributeType.VISIBLE, true, AttributeType.HEADSUP, false,
				AttributeType.TITLE, "Side View",
				AttributeType.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.FEEDBACK, 0);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();
		return view;
	}

	/**
	 * Creates and adds a mouse listener to check for clicks
	 */
	private void checkForClick() {
		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				wasClicked(arg0);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		};
		getContainer().getComponent().addMouseListener(ml);
	}

	/**
	 * This checks which bar was clicked on and displays the correct left and
	 * right waveshape plots
	 * 
	 * @param me
	 *            The mouse event of the click
	 */
	private void wasClicked(MouseEvent me) {
		for (int bar = 0; bar < _superLayerItems.length; bar++) {
			if (_superLayerItems[bar].contains(getContainer(), me.getPoint())) {
				Bed.getInstance().setPlotsVisible(bar);
			}
		}
	}

	/**
	 * This method creates the shapes for each of the bar rectangles
	 */
	private void setbarWorldRects() {

		_barWorldRects = new Rectangle2D.Double[9];

		Rectangle2D.Double defaultWorld = _defaultWorldRectangle;

		double left = defaultWorld.getMinX();
		double right = defaultWorld.getMaxX();
		double top = defaultWorld.getMaxY();
		double bottom = defaultWorld.getMinY();
		double x13 = left + defaultWorld.width / 3.0;
		double x23 = right - defaultWorld.width / 3.0;
		double y13 = bottom + defaultWorld.height / 3.0;
		double y23 = top - defaultWorld.height / 3.0;

		_barWorldRects[0] = new Rectangle2D.Double(left, y23, x13 - left, top
				- y23);
		_barWorldRects[1] = new Rectangle2D.Double(x13, y23, x23 - x13, top
				- y23);
		_barWorldRects[2] = new Rectangle2D.Double(x23, y23, right - x13, top
				- y23);

		_barWorldRects[3] = new Rectangle2D.Double(left, y13, x13 - left, y23
				- y13);
		_barWorldRects[4] = new Rectangle2D.Double(x13, y13, x23 - x13, y23
				- y13);
		_barWorldRects[5] = new Rectangle2D.Double(x23, y13, right - x23, y23
				- y13);

		_barWorldRects[6] = new Rectangle2D.Double(left, bottom, x13 - left,
				y13 - bottom);
		_barWorldRects[7] = new Rectangle2D.Double(x13, bottom, x23 - x13, y13
				- bottom);
		_barWorldRects[8] = new Rectangle2D.Double(x23, bottom, right - x23,
				y13 - bottom);
	}

	/**
	 * Draws the rectangle backgrounds (probably not necessary)
	 */
	private void setBeforeDraw() {
		// style for bar rects
		_barStyle = new Styled(X11Colors.getX11Color("Dark Blue"));
		_barStyle.setLineColor(Color.black);

		// use a before-drawer to bar dividers and labels
		_beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container,
							_barWorldRects[bar], _barStyle);
				}
			}

		};

		getContainer().setBeforeDraw(_beforeDraw);
	}

	/**
	 * Can be used to draw things based on the final layout, but unused at the
	 * current moment
	 */
	private void setAfterDraw() {
		IDrawable _afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
			}

		};
		getContainer().setAfterDraw(_afterDraw);
	}

	/**
	 * Creates the bar instances that will handle and display hits
	 */
	private void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(
				_detectorLayerName);

		_superLayerItems = new SideViewBar[GeometricConstants.BARS];

		for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
			_superLayerItems[bar] = new SideViewBar(detectorLayer, this,
					_barWorldRects[bar], bar);
		}
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * Currently does not do anything but use the super call.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		super.getFeedbackStrings(container, screenPoint, worldPoint,
				feedbackStrings);
	}

	/**
	 * Gets which bar the point is contained in.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 * @return the bar (1-9) or -1 for none.
	 */
	@Override
	public int getSector(Point2D.Double worldPoint) {
		for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
			if (_barWorldRects[bar].contains(worldPoint)) {
				return bar + 1; // convert to 1-based index
			}
		}
		return -1;
	}
	
	public SideViewBar[] getBars() {
		return _superLayerItems;
	}
}
