package infn.bed.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
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
import infn.bed.component.ControlPanel;
import infn.bed.geometry.GeometricConstants;
import infn.bed.item.FullSideViewBar;
import infn.bed.item.FullSideViewVeto;

/**
 * Handles the drawing of the Full Side View, which consists of a scintillator bar matrix and a set of vetoes.
 * 
 * <p>
 * The following is the numbering scheme for the vetoes.<br>
 * 1. Crystal<br>
 * 2. Internal Upstream Far Bottom<br>
 * 3. Internal Upstream Bottom<br>
 * 4. Internal Upstream Top<br>
 * 5. Internal Upstream Far Top<br>
 * 6. Internal Top Far Left<br>
 * 7. Internal Top Left<br>
 * 8. Internal Top Right<br>
 * 9. Internal Top Far Right<br>
 * 10. Internal Downstream Far Top<br>
 * 11. Internal Downstream Top<br>
 * 12. Internal Downstream Bottom<br>
 * 13. Internal Downstream Far Bottom<br>
 * 14. Internal Bottom Far Right<br>
 * 15. Internal Bottom Right<br>
 * 16. Internal Bottom Left<br>
 * 17. Internal Bottom Far Left<br>
 * 18. Internal Left<br>
 * 19. Internal Right<br>
 * 20. External Upstream Bottom<br>
 * 21. External Upstream Top<br>
 * 22. External Top Left<br>
 * 23. External Top Middle<br>
 * 24. External Top Right<br>
 * 25. External Downstream Top<br>
 * 26. External Downstream Bottom<br>
 * 27. External Bottom Right<br>
 * 28. External Bottom Middle<br>
 * 29. External Bottom Left<br>
 * 30. External Left<br>
 * 31. External Right<br>
 * </p>
 * 
 * <p>
 * NOTE: While the crystal is not a veto, it is classified as a veto for brevity and consistency.
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
	private Rectangle2D.Double _vetoWorldRectangles[];

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
		_vetoWorldRectangles = new Rectangle2D.Double[GeometricConstants.VETOES];

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
		 * Crystal
		 */
		
		// Crystal
		_vetoWorldRectangles[0] = new Rectangle2D.Double(barLeft + 3 * boxWidth + gap - boxWidth / 2.5, y23, gap / 1.5, boxHeight / 1.5);
		
		/*
		 * Internal Upstream Vetoes
		 */
		
		// Internal Upstream Far Bottom
		_vetoWorldRectangles[1] = new Rectangle2D.Double(barLeft - 2 * gap, barBottom, gap, 3 * boxHeight / 4);

		// Internal Upstream Bottom
		_vetoWorldRectangles[2] = new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectangles[1].getY() + _vetoWorldRectangles[1].getHeight(), gap, 3 * boxHeight / 4);

		// Internal Upstream Top
		_vetoWorldRectangles[3] = new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectangles[2].getY() + _vetoWorldRectangles[2].getHeight(), gap, 3 * boxHeight / 4);

		// Internal Upstream Far Top
		_vetoWorldRectangles[4] = new Rectangle2D.Double(barLeft - 2 * gap, _vetoWorldRectangles[3].getY() + _vetoWorldRectangles[3].getHeight(), gap, 3 * boxHeight / 4);
		
		/*
		 * Internal Top Vetoes
		 */
		
		// Internal Top Far Left
		_vetoWorldRectangles[5] = new Rectangle2D.Double(barLeft, barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap);
		
		// Internal Top Left
		_vetoWorldRectangles[6] = new Rectangle2D.Double(_vetoWorldRectangles[5].getX() + _vetoWorldRectangles[5].getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap);
		
		// Internal Top Right
		_vetoWorldRectangles[7] = new Rectangle2D.Double(_vetoWorldRectangles[6].getX() + _vetoWorldRectangles[6].getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap);
		
		// Internal Top Far Right
		_vetoWorldRectangles[8] = new Rectangle2D.Double(_vetoWorldRectangles[7].getX() + _vetoWorldRectangles[7].getWidth(), barBottom + 3 * boxHeight + gap, 3 * boxWidth / 4, gap);
		
		/*
		 * Internal Downstream Vetoes
		 */
		
		// Internal Downstream Far Top
		_vetoWorldRectangles[9] = new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectangles[4].getY(), gap, 3 * boxHeight / 4);
		
		// Internal Downstream Top
		_vetoWorldRectangles[10] = new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectangles[3].getY(), gap, 3 * boxHeight / 4);
		
		// Internal Downstream Bottom
		_vetoWorldRectangles[11] = new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectangles[2].getY(), gap, 3 * boxHeight / 4);
		
		// Internal Downstream Far Bottom
		_vetoWorldRectangles[12] = new Rectangle2D.Double(barLeft + 3 * boxWidth + gap, _vetoWorldRectangles[1].getY(), gap, 3 * boxHeight / 4);
		
		/*
		 * Internal Bottom Vetoes
		 */
		
		// Internal Bottom Far Right
		_vetoWorldRectangles[13] = new Rectangle2D.Double(_vetoWorldRectangles[8].getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap);
		
		// Internal Bottom Right
		_vetoWorldRectangles[14] = new Rectangle2D.Double(_vetoWorldRectangles[7].getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap);
		
		// Internal Bottom Left
		_vetoWorldRectangles[15] = new Rectangle2D.Double(_vetoWorldRectangles[6].getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap);
		
		// Internal Bottom Far Left
		_vetoWorldRectangles[16] = new Rectangle2D.Double(_vetoWorldRectangles[5].getX(), barBottom - 2 * gap, 3 * boxWidth / 4, gap);
		
		/*
		 * Internal Caps
		 */
		
		// Internal Left
		_vetoWorldRectangles[17] = new Rectangle2D.Double(left + (3 * boxWidth) + 3 * gap, _vetoWorldRectangles[1].getY(), 3 * boxWidth + gap, _vetoWorldRectangles[1].getHeight() + _vetoWorldRectangles[2].getHeight() + _vetoWorldRectangles[3].getHeight() + _vetoWorldRectangles[4].getHeight());
		
		// Internal Right
		_vetoWorldRectangles[18] = new Rectangle2D.Double(right - 6 * boxWidth - 4 * gap, _vetoWorldRectangles[12].getY(), 3 * boxWidth + gap, _vetoWorldRectangles[9].getHeight() + _vetoWorldRectangles[10].getHeight() + _vetoWorldRectangles[11].getHeight() + _vetoWorldRectangles[12].getHeight());
		
		/*
		 * External Upstream Vetoes
		 */
		
		// External Upstream Bottom
		_vetoWorldRectangles[19] = new Rectangle2D.Double(barLeft - 4 * gap, barBottom - 2 * gap, gap, 5 * boxHeight / 2);
		
		// External Upstream Top
		_vetoWorldRectangles[20] = new Rectangle2D.Double(barLeft - 4 * gap, _vetoWorldRectangles[19].getY() + _vetoWorldRectangles[19].getHeight(), gap, 5 * boxHeight / 2);
		
		/*
		 * External Top Vetoes
		 */
		
		// External Top Left
		_vetoWorldRectangles[21] = new Rectangle2D.Double(barLeft - 2 * gap, barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		// External Top Middle
		_vetoWorldRectangles[22] = new Rectangle2D.Double(_vetoWorldRectangles[21].getX() + _vetoWorldRectangles[21].getWidth(), barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		// External Top Right
		_vetoWorldRectangles[23] = new Rectangle2D.Double(_vetoWorldRectangles[22].getX() + _vetoWorldRectangles[22].getWidth(), barBottom + 3 * boxHeight + 3 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		/*
		 * External Downstream Vetoes
		 */
		
		// External Downstream Top
		_vetoWorldRectangles[24] = new Rectangle2D.Double(barLeft + 3 * boxWidth + 3 * gap, _vetoWorldRectangles[20].getY(), gap, 5 * boxHeight / 2);
		
		// External Downstream Bottom
		_vetoWorldRectangles[25] = new Rectangle2D.Double(barLeft + 3 * boxWidth + 3 * gap, _vetoWorldRectangles[19].getY(), gap, 5 * boxHeight / 2);
		
		/*
		 * External Bottom Vetoes
		 */
		
		// External Bottom Right
		_vetoWorldRectangles[26] = new Rectangle2D.Double(_vetoWorldRectangles[23].getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		// External Bottom Middle
		_vetoWorldRectangles[27] = new Rectangle2D.Double(_vetoWorldRectangles[22].getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		// External Bottom Left
		_vetoWorldRectangles[28] = new Rectangle2D.Double(_vetoWorldRectangles[21].getX(), barBottom - 4 * gap, 2 * (2 * boxWidth + gap) / 3, gap);
		
		/*
		 * External Caps
		 */
		
		// External Left
		_vetoWorldRectangles[29] = new Rectangle2D.Double(left + gap, _vetoWorldRectangles[19].getY(), 3 * boxWidth + gap, _vetoWorldRectangles[19].getHeight() + _vetoWorldRectangles[20].getHeight());

		// External Right
		_vetoWorldRectangles[30] = new Rectangle2D.Double(right - 3 * boxWidth - 2 * gap, _vetoWorldRectangles[25].getY(), 3 * boxWidth + gap, _vetoWorldRectangles[24].getHeight() + _vetoWorldRectangles[25].getHeight());

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
				for (int veto = 0; veto < GeometricConstants.VETOES; veto++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container, _vetoWorldRectangles[veto], _barStyle);
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
		
		for (int veto = 0; veto < GeometricConstants.VETOES; veto++) {
			_superLayerVetoes[veto] = new FullSideViewVeto(detectorLayer, this, _vetoWorldRectangles[veto], veto);
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
		
		for (int veto = 0; veto < GeometricConstants.VETOES; veto++) {
			if (_vetoWorldRectangles[veto].contains(worldPoint)) {
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
