package infn.bed.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
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
import infn.bed.component.ControlPanel;
import infn.bed.geometry.GeometricConstants;
import infn.bed.item.FullSideViewBar;
import infn.bed.item.FullSideViewVeto;

/**
 * Draws the Full Side View, which consists of a set of crystals, a scintillator bar matrix, and a set of vetoes.
 * 
 * <p>
 * The following is the numbering scheme for the vetoes.<br>
 * 1. Far Top Crystal<br>
 * 2. Top Crystal<br>
 * 3. Bottom Crystal<br>
 * 4. Far Bottom Crystal<br>
 * 5. Internal Upstream Far Bottom<br>
 * 6. Internal Upstream Bottom<br>
 * 7. Internal Upstream Top<br>
 * 8. Internal Upstream Far Top<br>
 * 9. Internal Top Far Left<br>
 * 10. Internal Top Left<br>
 * 11. Internal Top Right<br>
 * 12. Internal Top Far Right<br>
 * 13. Internal Downstream Far Top<br>
 * 14. Internal Downstream Top<br>
 * 15. Internal Downstream Bottom<br>
 * 16. Internal Downstream Far Bottom<br>
 * 17. Internal Bottom Far Right<br>
 * 18. Internal Bottom Right<br>
 * 19. Internal Bottom Left<br>
 * 20. Internal Bottom Far Left<br>
 * 21. Internal Left<br>
 * 22. Internal Right<br>
 * 23. External Upstream Bottom<br>
 * 24. External Upstream Top<br>
 * 25. External Top Left<br>
 * 26. External Top Middle<br>
 * 27. External Top Right<br>
 * 28. External Downstream Top<br>
 * 29. External Downstream Bottom<br>
 * 30. External Bottom Right<br>
 * 31. External Bottom Middle<br>
 * 32. External Bottom Left<br>
 * 33. External Left<br>
 * 34. External Right<br>
 * </p>
 * 
 * <p>
 * NOTE: While the crystals are not vetoes, they are classified as vetoes for brevity and consistency.
 * </p>
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
@SuppressWarnings("serial")
public class FullSideView extends BedView {

	/**
	 * An array of bar rectangles.
	 */
	private Rectangle2D.Double _barWorldRectangles[];

	/**
	 * An array of veto rectangles.
	 */
	private ArrayList<Rectangle2D.Double> _vetoWorldRectanglesArrayList;

	/**
	 * Used for drawing bar and veto rectangles.
	 */
	private Styled _barStyle;

	/**
	 * Used for the before draw phase of rectangles.
	 */
	private IDrawable _beforeDraw;

	/**
	 * An array of bar instances that display hits and information.
	 */
	private FullSideViewBar _superLayerBars[];

	/**
	 * An array of veto instances that display hits and information.
	 */
	private FullSideViewVeto _superLayerVetoes[];

	/**
	 * The 3 × 3 world grid.
	 */
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(0.0, 0.0, 3.0, 3.0);

	/**
	 * Constructor.
	 * 
	 * @param keyVals Used in the super class (BedView) to set up parameters of this view.
	 */
	private FullSideView(Object... keyVals) {
		super(keyVals);
		
		setBarWorldRectangles();
		setBeforeDraw();
		setAfterDraw();
		addItems();
	}

	/**
	 * Creates a new instance of this class.
	 * 
	 * @return view The new view instance.
	 */
	public static FullSideView createFullSideView() {
		FullSideView view = null;

		// Set the dimensions of the frame to a fraction of the screen
		Dimension d = GraphicsUtilities.screenFraction(0.5);

		// Create the view
		view = new FullSideView(AttributeType.WORLDSYSTEM, _defaultWorldRectangle, 
				// Container width (NOT frame width)
				AttributeType.WIDTH, d.width, 
				// Container height (NOT frame height)
				AttributeType.HEIGHT, d.height, 
				AttributeType.TOOLBAR, true, AttributeType.TOOLBARBITS, 
				BaseToolBar.NODRAWING & ~BaseToolBar.RANGEBUTTON & ~BaseToolBar.TEXTFIELD & ~BaseToolBar.CONTROLPANELBUTTON & ~BaseToolBar.TEXTBUTTON & ~BaseToolBar.DELETEBUTTON, 
				AttributeType.VISIBLE, true, 
				AttributeType.HEADSUP, false, 
				AttributeType.TITLE, "Full Side View", 
				AttributeType.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.FEEDBACK, 0);
		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();
		
		return view;
	}

	/**
	 * Creates each bar and veto rectangle.
	 */
	private void setBarWorldRectangles() {

		_barWorldRectangles = new Rectangle2D.Double[GeometricConstants.BARS];
		_vetoWorldRectanglesArrayList = new ArrayList<>(GeometricConstants.VETOES);

		Rectangle2D.Double worldRectangle = _defaultWorldRectangle;

		double gap = worldRectangle.width / 48;
		double boxWidth = worldRectangle.width / 12 - 2 * gap;
		double boxHeight = worldRectangle.height / 12 - 2 * gap;

		double left = worldRectangle.getMinX();
		double right = worldRectangle.getMaxX();

		double barLeft = 1.5 - (1.5 * boxWidth);
		double barBottom = 1.5 - boxWidth;

		double x13 = barLeft + boxWidth;
		double x23 = x13 + boxWidth;
		double y13 = barBottom + boxWidth;
		double y23 = y13 + boxWidth;
		
		/*
		 * SCINTILLATOR BAR MATRIX
		 */
		
		/*
		 * Row 1
		 */

		_barWorldRectangles[0] = new Rectangle2D.Double(barLeft, y23, boxWidth, boxHeight);
		_barWorldRectangles[1] = new Rectangle2D.Double(x13, y23, boxWidth, boxHeight);
		_barWorldRectangles[2] = new Rectangle2D.Double(x23, y23, boxWidth, boxHeight);
		
		/*
		 * Row 2
		 */

		_barWorldRectangles[3] = new Rectangle2D.Double(barLeft, y13, boxWidth,boxHeight);
		_barWorldRectangles[4] = new Rectangle2D.Double(x13, y13, boxWidth, boxHeight);
		_barWorldRectangles[5] = new Rectangle2D.Double(x23, y13, boxWidth, boxHeight);
		
		/*
		 * Row 3
		 */

		_barWorldRectangles[6] = new Rectangle2D.Double(barLeft, barBottom, boxWidth, boxHeight);
		_barWorldRectangles[7] = new Rectangle2D.Double(x13, barBottom, boxWidth, boxHeight);
		_barWorldRectangles[8] = new Rectangle2D.Double(x23, barBottom, boxWidth, boxHeight);

		/*
		 * VETOES
		 */
		
		/*
		 * Crystals
		 */
		
		// Far Bottom Crystal
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap - boxWidth / 2.75, barBottom + gap / 2, gap / 2, boxHeight / 2));
		
		// Bottom Crystal
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap - boxWidth / 2.75, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight() + gap / 2, gap / 2, boxHeight / 2));
		
		// Top Crystal
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap - boxWidth / 2.75, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight() + gap / 2, gap / 2, boxHeight / 2));
		
		// Far Top Crystal
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap - boxWidth / 2.75, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight() + gap / 2, gap / 2, boxHeight / 2));
		
		/*
		 * Internal Upstream Vetoes
		 */
		
		// Internal Upstream Far Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 2 * gap, barBottom, gap, 3 * boxHeight / 4));

		// Internal Upstream Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight(), gap, 3 * boxHeight / 4));

		// Internal Upstream Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight(), gap, 3 * boxHeight / 4));

		// Internal Upstream Far Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight(), gap, 3 * boxHeight / 4));
		int internalUpstreamUpperIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * Internal Top Vetoes
		 */
		
		// Internal Top Far Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft, barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap));
		
		// Internal Top Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getX() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap));
		
		// Internal Top Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getX() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap));
		
		// Internal Top Far Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getX() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap));
		int internalTopUpperIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * Internal Downstream Vetoes
		 */
		
		// Internal Downstream Far Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex).getY(), gap, 3 * boxHeight / 4));
		
		// Internal Downstream Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 1).getY(), gap, 3 * boxHeight / 4));
		
		// Internal Downstream Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 2).getY(), gap, 3 * boxHeight / 4));
		
		// Internal Downstream Far Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 3).getY(), gap, 3 * boxHeight / 4));
		int internalDownstreamFarBottomIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * Internal Bottom Vetoes
		 */
		
		// Internal Bottom Far Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(internalTopUpperIndex).getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap));
		
		// Internal Bottom Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(internalTopUpperIndex - 1).getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap));
		
		// Internal Bottom Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(internalTopUpperIndex - 2).getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap));
		
		// Internal Bottom Far Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(internalTopUpperIndex - 3).getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap));
		
		/*
		 * Internal Caps
		 */
		
		// Internal Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(left + (3 * boxWidth) + 3 * gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 3).getY(), 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 3).getHeight() + _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 2).getHeight() + _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex - 1).getHeight() + _vetoWorldRectanglesArrayList.get(internalUpstreamUpperIndex).getHeight()));
		
		// Internal Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(right - 6 * boxWidth - 4 * gap, _vetoWorldRectanglesArrayList.get(internalDownstreamFarBottomIndex).getY(), 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(internalDownstreamFarBottomIndex - 3).getHeight() + _vetoWorldRectanglesArrayList.get(internalDownstreamFarBottomIndex - 2).getHeight() + _vetoWorldRectanglesArrayList.get(internalDownstreamFarBottomIndex - 1).getHeight() + _vetoWorldRectanglesArrayList.get(internalDownstreamFarBottomIndex).getHeight()));
		
		/*
		 * External Upstream Vetoes
		 */
		
		// External Upstream Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 4 * gap, barBottom - 2 * gap, gap, 5 * boxHeight / 2));
		int externalUpstreamBottomIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		// External Upstream Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 4 * gap, _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getY() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getHeight(), gap, 5 * boxHeight / 2));
		int externalUpstreamTopIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * External Top Vetoes
		 */
		
		// External Top Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft - 2 * gap, barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		
		// External Top Middle
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getX() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getWidth(), barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		
		// External Top Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getX() + _vetoWorldRectanglesArrayList.get(_vetoWorldRectanglesArrayList.size() - 1).getWidth(), barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		int externalTopUpperIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * External Downstream Vetoes
		 */
		
		// External Downstream Top
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + 3 * gap, _vetoWorldRectanglesArrayList.get(externalUpstreamTopIndex).getY(), gap, 5 * boxHeight / 2));
		
		// External Downstream Bottom
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(barLeft + 3 * boxWidth + 3 * gap, _vetoWorldRectanglesArrayList.get(externalUpstreamBottomIndex).getY(), gap, 5 * boxHeight / 2));
		int externalDownstreamUpperIndex = _vetoWorldRectanglesArrayList.size() - 1;
		
		/*
		 * External Bottom Vetoes
		 */
		
		// External Bottom Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(externalTopUpperIndex).getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		
		// External Bottom Middle
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(externalTopUpperIndex - 1).getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		
		// External Bottom Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(_vetoWorldRectanglesArrayList.get(externalTopUpperIndex - 2).getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap));
		
		/*
		 * External Caps
		 */
		
		// External Left
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(left + gap, _vetoWorldRectanglesArrayList.get(externalUpstreamBottomIndex).getY(), 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(externalUpstreamBottomIndex).getHeight() + _vetoWorldRectanglesArrayList.get(externalUpstreamTopIndex).getHeight()));

		// External Right
		_vetoWorldRectanglesArrayList.add(new Rectangle2D.Double(right - 3 * boxWidth - 2 * gap, _vetoWorldRectanglesArrayList.get(externalDownstreamUpperIndex).getY(), 3 * boxWidth + gap, _vetoWorldRectanglesArrayList.get(externalDownstreamUpperIndex - 1).getHeight() + _vetoWorldRectanglesArrayList.get(externalDownstreamUpperIndex).getHeight()));

	}

	/**
	 * Draws the bar and veto rectangle backgrounds.
	 */
	private void setBeforeDraw() {
		_barStyle = new Styled(X11Colors.getX11Color("Dark Blue"));
		_barStyle.setLineColor(Color.black);
		
		_beforeDraw = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
				for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container, _barWorldRectangles[bar], _barStyle);
				}
				for (int veto = 0; veto < _vetoWorldRectanglesArrayList.size(); veto++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container, _vetoWorldRectanglesArrayList.get(veto), _barStyle);
				}
			}
		};
		
		getContainer().setBeforeDraw(_beforeDraw);
	}

	/**
	 * Can be used to draw things based on the final layout, but unused at the current moment
	 */
	private void setAfterDraw() {
		IDrawable _afterDraw = new DrawableAdapter() {
			@Override
			public void draw(Graphics g, IContainer container) {
				// ...
			}
		};
		
		getContainer().setAfterDraw(_afterDraw);
	}

	/**
	 * Creates the bar and veto instances that will handle and display hits
	 */
	private void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);
		
		_superLayerBars = new FullSideViewBar[GeometricConstants.BARS];
		_superLayerVetoes = new FullSideViewVeto[GeometricConstants.VETOES];

		for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
			_superLayerBars[bar] = new FullSideViewBar(detectorLayer, this, _barWorldRectangles[bar], bar);
		}
		
		for (int veto = 0; veto < _vetoWorldRectanglesArrayList.size(); veto++) {
			_superLayerVetoes[veto] = new FullSideViewVeto(detectorLayer, this, _vetoWorldRectanglesArrayList.get(veto), veto);
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
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint, List<String> feedbackStrings) {
		super.getFeedbackStrings(container, screenPoint, worldPoint, feedbackStrings);
	}

	/**
	 * Gets which bar or veto the point is contained in.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 * @return The scintillator bar (1 - 9) or veto number (1 - 31), -1 if out of bounds.
	 */
	@Override
	public int getSector(Point2D.Double worldPoint) {
		for (int bar = 0; bar < GeometricConstants.BARS; bar++) {
			if (_barWorldRectangles[bar].contains(worldPoint)) {
				// Convert to one-based indexing.
				return bar + 1;
			}
		}
		
		for (int veto = 0; veto < _vetoWorldRectanglesArrayList.size(); veto++) {
			if (_vetoWorldRectanglesArrayList.get(veto).contains(worldPoint)) {
				return veto + 1;
			}
		}
		
		return -1;
	}
	
	public FullSideViewBar[] getBars() {
		return _superLayerBars;
	}
	
	public FullSideViewVeto[] getVetoes() {
		return _superLayerVetoes;
	}
}
