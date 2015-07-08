package infn.bed.item;

import infn.bed.util.CalibrationFileParser;
import infn.bed.view.BarFrontView;
import infn.bed.view.BedView;
import infn.bed.event.AccumulationManager;
import infn.bed.event.ChargeTimeData;
import infn.bed.event.EventManager;
import infn.bed.math.MathematicalConstants;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;

/**
 * This class handles converting charge-time information to energy-time
 * information. It then draws rectangles at the position of the hit and colors
 * them. It displays energy-time information for each bar in the info panel. The
 * front view provides a good visualization of the path of the detected
 * particle.
 * 
 * @author Andy Beiter
 * 
 */
public class FrontViewBar extends RectangleItem {

	/**
	 * Font for label text
	 */
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);

	/**
	 * The number of the bar
	 */
	private int _bar;

	/**
	 * The array of the hit sectors (detectors)
	 */
	private int hitSectors[];

	/**
	 * The array of the hit layers (columns)
	 */
	private int hitLayers[];

	/**
	 * The array of the hit paddles (rows)
	 */
	private int hitPaddles[];

	/**
	 * The array of the charges from the left PMTs
	 */
	private int chargeLeft[];

	/**
	 * The array of the charges from the right PMTs
	 */
	private int chargeRight[];

	/**
	 * The array of the times from the left PMTs
	 */
	private int timeLeft[];

	/**
	 * The array of the times from the right PMTs
	 */
	private int timeRight[];

	/**
	 * The array of the positions from the left side of the bar where a hit
	 * occurred
	 */
	private double posFromLeft[];

	/**
	 * The array of energies from each hit
	 */
	private double totalE[];

	/**
	 * The array of times from each hit
	 */
	private double totalT[];

	/**
	 * The effective speed of light in the bar
	 */
	private double v_eff;

	/**
	 * The charge-to-energy conversion factor for the left PMT for the bar
	 */
	private double A_left;

	/**
	 * The charge-to-energy conversion factor for the right PMT for the bar
	 */
	private double A_right;

	/**
	 * The attenuation length
	 */
	private double lambda;

	/**
	 * Time delay for the left PMT
	 */
	private double delta_left;

	/**
	 * Time delay for the right PMT
	 */
	private double delta_right;

	/**
	 * The tdc-to-time conversion factor for the left PMT for the bar
	 */
	private double tdcConvLeft;

	/**
	 * The tdc-to-time conversion factor for the right PMT for the bar
	 */
	private double tdcConvRight;

	/**
	 * The length of the bar
	 */
	private double length;

	/**
	 * The view this bar is in
	 */
	private BarFrontView _view;

	/**
	 * Upper energy level (MeV) for color scaling
	 */
	private static final float upperEnergyScale = MathematicalConstants.UPPER_ENERGY_LIMIT;

	/**
	 * The rectangle the bar is drawn in
	 */
	private Rectangle2D.Double _worldRectangle;

	/**
	 * Constructor for the bar used in the front view
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param view
	 *            the BarFrontView parent
	 * @param worldRectangle
	 *            the rectangle the bar is in
	 * @param bar
	 *            the bar (0-8)
	 */
	public FrontViewBar(LogicalLayer layer, BarFrontView view,
			Rectangle2D.Double worldRectangle, int bar) {
		super(layer, worldRectangle);
		_worldRectangle = worldRectangle;
		_view = view;
		_bar = bar + 1;
		_style.setFillColor(Color.white);
		_style.setLineColor(Color.black);
		if (_bar % 3 == 2) {
			_style.setFillColor(Color.lightGray);
		} else if (_bar % 3 == 1) {
			_style.setFillColor(new Color(150, 150, 150));
		}
		_name = "Bar: " + _bar;
	}

	/**
	 * Gets the calibration constants from a file and stores them
	 * 
	 * TODO change file for real constants
	 */
	public void getConstants(File file) {
		CalibrationFileParser calibrationFileParser = new CalibrationFileParser(file, "b", _bar);
		v_eff = calibrationFileParser.getEffectiveVelocity();
		A_left = calibrationFileParser.getLeftADCConversionFactor();
		A_right = calibrationFileParser.getRightADCConversionFactor();
		lambda = calibrationFileParser.getAttenuationLength();
		delta_left = calibrationFileParser.getLeftShift();
		delta_right = calibrationFileParser.getRightShift();
		tdcConvLeft = calibrationFileParser.getLeftTDCConversionFactor();
		tdcConvRight = calibrationFileParser.getRightTDCConversionFactor();
		length = calibrationFileParser.getItemLength();
	}

	/**
	 * Custom drawer for the bar.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (EventControl.getInstance().isAccumulating()) {
			return;
		}
		super.drawItem(g, container);
		g.setFont(labelFont);
		g.setColor(Color.yellow);
		// now the data
		if (_view.getMode() == BedView.Mode.SINGLE_EVENT) {
			singleEventDrawItem(g, container);
		} else {
			accumulatedDrawItem(g, container);
		}

		// just to make clean
		g.setColor(_style.getLineColor());
		g.drawPolygon(_lastDrawnPolygon);
	}

	/**
	 * Draw in single event mode. Uses a small rectangle at the position of the
	 * hit.
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 */
	private void singleEventDrawItem(Graphics g, IContainer container) {
		WorldGraphicsUtilities.drawWorldRectangle(g, container,
				_worldRectangle, _style.getFillColor(), _style.getLineColor());

		ChargeTimeData ctData = EventManager.getInstance().getChargeTimeData();
		if (ctData != null) {
			hitSectors = ctData.getSectorArray();
			hitLayers = ctData.getLayerArray();
			hitPaddles = ctData.getPaddleArray();
			chargeLeft = ctData.getLeftPMTChargeArray();
			timeLeft = ctData.getLeftPMTTimeArray();
			chargeRight = ctData.getRightPMTChargeArray();
			timeRight = ctData.getRightPMTTimeArray();
			if (chargeLeft != null && chargeRight != null) {
				chargeToEnergy();
				for (int i = 0; i < totalE.length; i++) {
					if (inThisBar(hitSectors[i], hitLayers[i], hitPaddles[i])) {
						if (totalE[i] > 0) {
							// TODO EDIT SIZE OF HIT RECTANGLE
							// draw small rectangle at hit
							double pos = posFromLeft[i] / 40.0 * 3;
							Rectangle2D.Double hitRect = new Rectangle2D.Double(
									pos, _worldRectangle.y,
									_worldRectangle.width / 40.0,
									_worldRectangle.height);
							double scale = totalE[i] / upperEnergyScale;
							try {
								WorldGraphicsUtilities
										.drawWorldRectangle(
												g,
												container,
												hitRect,
												new Color(
														(int) (Math
																.ceil(scale * 255)),
														0,
														(int) Math
																.ceil(255 - scale * 255)),
												_style.getLineColor());
							} catch (Exception e) {
								WorldGraphicsUtilities.drawWorldRectangle(g,
										container, hitRect,
										new Color(255, 0, 0),
										_style.getLineColor());
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Converts the charge-time information to energy-time information
	 */
	private void chargeToEnergy() {
		double t_l[] = new double[timeLeft.length];
		double t_r[] = new double[timeRight.length];
		for (int i = 0; i < timeLeft.length; i++) {
			t_l[i] = (timeLeft[i] * 1.0 / tdcConvLeft) - delta_left;
		}
		for (int i = 0; i < timeRight.length; i++) {
			t_r[i] = (timeRight[i] * 1.0 / tdcConvRight) - delta_right;
		}
		posFromLeft = new double[chargeLeft.length];
		for (int i = 0; i < posFromLeft.length; i++) {
			posFromLeft[i] = (v_eff * (t_l[i] - t_r[i]) + length) / 2.0;
		}
		double e_l[] = new double[chargeLeft.length];
		double e_r[] = new double[chargeRight.length];
		for (int i = 0; i < e_l.length; i++) {
			e_l[i] = chargeLeft[i] * A_left;
		}
		for (int i = 0; i < e_r.length; i++) {
			e_r[i] = chargeRight[i] * A_right;
		}
		totalE = new double[e_l.length];
		totalT = new double[t_l.length];
		for (int i = 0; i < totalE.length; i++) {
			double e_l_prime = e_l[i] * Math.exp(posFromLeft[i] / lambda);
			double e_r_prime = e_r[i]
					* Math.exp((length - posFromLeft[i]) / lambda);
			totalE[i] = (e_l_prime + e_r_prime) / 2;
			totalT[i] = (t_l[i] + t_r[i] - (length / v_eff)) / 2.0;
		}
	}

	/**
	 * Checks if a given sector/layer/paddle is this bar
	 * 
	 * @param sector
	 *            Which detector (currently not used)
	 * @param layer
	 *            Which column
	 * @param paddle
	 *            Which row
	 * @return True if the s/l/p is this bar, false if not
	 */
	private boolean inThisBar(int sector, int layer, int paddle) {
		switch (layer) {
		case 0:
			switch (paddle) {
			case 0:
				return (7 == _bar);
			case 1:
				return (4 == _bar);
			case 2:
				return (1 == _bar);
			}
			break;
		case 1:
			switch (paddle) {
			case 0:
				return (8 == _bar);
			case 1:
				return (5 == _bar);
			case 2:
				return (2 == _bar);
			}
			break;
		case 2:
			switch (paddle) {
			case 0:
				return (9 == _bar);
			case 1:
				return (6 == _bar);
			case 2:
				return (3 == _bar);
			}
		}
		return false;
	}

	/**
	 * Draw hits in accumulated mode
	 * 
	 * Currently unused
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 */
	private void accumulatedDrawItem(Graphics g, IContainer container) {
	}

	/**
	 * Add any appropriate feedback strings for the heads-up display or feedback
	 * panel.
	 * 
	 * @param container
	 *            the Base container.
	 * @param screenPoint
	 *            the mouse location.
	 * @param worldPoint
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {
		if (_worldRectangle.contains(worldPoint)) {
			double x = worldPoint.x;
			double y = worldPoint.y;
			double z = 0;
			x *= 40.0 / 3.0;
			y *= 30;
			y = y % 30;

			String rtp = "approx xyz " + DoubleFormat.doubleFormat(x, 1)
					+ "cm, " + DoubleFormat.doubleFormat(y, 1) + "cm, "
					+ DoubleFormat.doubleFormat(z, 1) + "cm";
			feedbackStrings.add(rtp);

			if (_view.getMode() == BedView.Mode.SINGLE_EVENT) {
				singleEventFeedbackStrings(feedbackStrings);
			} else {
				accumulatedFeedbackStrings(feedbackStrings);
			}

		}
	}

	/**
	 * Get the feedback strings for single event mode. Displays energy and time
	 * of hits.
	 * 
	 * @param feedbackStrings
	 *            The list of feedback strings
	 */
	private void singleEventFeedbackStrings(List<String> feedbackStrings) {
		if (EventManager.getInstance().getChargeTimeData() != null) {
			if (chargeLeft != null && chargeRight != null) {
				int hits = 0;
				double barE = 0;
				boolean hit[] = new boolean[totalE.length];
				for (int i = 0; i < totalE.length; i++) {
					if (inThisBar(hitSectors[i], hitLayers[i], hitPaddles[i])) {
						if (totalE[i] > 0) {
							hits++;
							barE += totalE[i];
							hit[i] = true;
						} else {
							hit[i] = false;
						}
					} else {
						hit[i] = false;
					}
				}
				String energyStr = "$orange$" + "Energy deposited:  " + barE
						+ " MeV\n# of hits:  " + hits;
				int counter = 1;
				for (int i = 0; i < hit.length; i++) {
					if (hit[i]) {
						energyStr += "\nTime #" + counter + ":  " + totalT[i]
								+ " ns";
					}
				}
				feedbackStrings.add(energyStr);
			}
		}
	}

	/**
	 * Get the feedback strings for accumulated mode
	 * 
	 * Currently unused
	 * 
	 * @param feedbackStrings
	 *            The list of feedback strings
	 */
	private void accumulatedFeedbackStrings(List<String> feedbackStrings) {

		int dcAccumulatedData[] = AccumulationManager.getInstance()
				.getAccumulatedGemcDcData();
		int hitCount = dcAccumulatedData[_bar - 1];
		int maxHit = AccumulationManager.getInstance().getMaxGemcDcCount();
		if ((hitCount < 1) || (maxHit < 1)) {
			feedbackStrings.add("hit fraction 0.0");
		} else {
			double fract = ((double) hitCount) / maxHit;
			feedbackStrings.add("hit fraction "
					+ DoubleFormat.doubleFormat(fract, 3));
		}
	}

}
