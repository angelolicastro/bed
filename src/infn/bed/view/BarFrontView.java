package infn.bed.view;

import infn.bed.component.ControlPanel;
import infn.bed.geometry.GeometricConstants;
import infn.bed.item.FrontViewBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
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

/**
 * This class handles the drawing of the front view of the bars. It orders the
 * bars from bottom to top and then front to back starting with the bottom of
 * the screen. So from top to bottom, the order will be back-top, back-middle,
 * back-bot, middle-top, middle-middle, middle-bot, front-top, front-middle, and
 * finally front-bot.
 * 
 * @author Andy Beiter
 * 
 */
@SuppressWarnings("serial")
public class BarFrontView extends BedView {

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
	private FrontViewBar _superLayerItems[];

	/**
	 * The grid used for positioning the rectangles. The shape is a 3x3 grid.
	 */
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			0.0, 0.0, 3.0, 3.0);

	/**
	 * Constructor used to set up parameters and to handle the drawing and
	 * adding of bar rectangles and objects
	 * 
	 * @param keyVals
	 *            used in the super class (BedView) to set up parameters of this
	 *            view
	 */
	private BarFrontView(Object... keyVals) {
		super(keyVals);

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
	public static BarFrontView createBarFrontView() {
		BarFrontView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.5);

		// create the view
		view = new BarFrontView(
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
				AttributeType.TITLE, "Front View", // TODO changing the title makes the
											// view disappear?
				AttributeType.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.FEEDBACK, 0);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();
		return view;
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
		double y19 = bottom + defaultWorld.height / 9.0;
		double y29 = y19 + defaultWorld.height / 9.0;
		double y39 = y29 + defaultWorld.height / 9.0;
		double y49 = y39 + defaultWorld.height / 9.0;
		double y59 = y49 + defaultWorld.height / 9.0;
		double y69 = y59 + defaultWorld.height / 9.0;
		double y79 = y69 + defaultWorld.height / 9.0;
		double y89 = y79 + defaultWorld.height / 9.0;

		_barWorldRects[2] = new Rectangle2D.Double(left, y29, right, y39 - y29);
		_barWorldRects[5] = new Rectangle2D.Double(left, y19, right, y29 - y19);
		_barWorldRects[8] = new Rectangle2D.Double(left, bottom, right, y19
				- bottom);

		_barWorldRects[1] = new Rectangle2D.Double(left, y59, right, y69 - y59);
		_barWorldRects[4] = new Rectangle2D.Double(left, y49, right, y59 - y49);
		_barWorldRects[7] = new Rectangle2D.Double(left, y39, right, y49 - y39);

		_barWorldRects[0] = new Rectangle2D.Double(left, y89, right, top - y89);
		_barWorldRects[3] = new Rectangle2D.Double(left, y79, right, y89 - y79);
		_barWorldRects[6] = new Rectangle2D.Double(left, y69, right, y79 - y69);
	}

	/**
	 * Draws the rectangle backgrounds (probably not necessary)
	 */
	private void setBeforeDraw() {
		// style for bar rects
		_barStyle = new Styled(X11Colors.getX11Color("Dark Blue"));
		_barStyle.setLineColor(Color.white);

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

		_superLayerItems = new FrontViewBar[GeometricConstants.BARS];

		for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
			_superLayerItems[bar] = new FrontViewBar(detectorLayer, this,
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

	public FrontViewBar[] getBars() {
		return _superLayerItems;
	}
}
