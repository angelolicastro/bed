package infn.bed.item;

import infn.bed.event.ChargeTimeData;
import infn.bed.event.EventManager;
import infn.bed.math.MathematicalConstants;
import infn.bed.util.CalibrationFileParser;
import infn.bed.util.GetVetoLayer;
import infn.bed.view.BedView;
import infn.bed.view.FullSideView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;

/**
 * This class handles converting charge-time information to energy-time
 * information for vetoes. It then draws rectangles and colors them if they are
 * hit. It displays energy-time information for each veto in the info panel.
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
public class FullSideViewVeto extends RectangleItem {

	/**
	 * Font for label text
	 */
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);

	/**
	 * The number of the veto
	 */
	private int _veto;

	/**
	 * The array of the hit sectors (detectors)
	 */
	private int hitVetoSectors[];

	/**
	 * The array that indicates if a hit veto is internal or external
	 */
	private int hitIntOrExt[];

	/**
	 * The array that indicates which internal or external veto was hit
	 */
	private int hitChannels[];

	/**
	 * The array of the charges from the primary PMTs
	 */
	private int charge1[];

	/**
	 * The array of the charges from the secondary PMTs
	 */
	private int charge2[];

	/**
	 * The array of the times from the primary PMTs
	 */
	private int time1[];

	/**
	 * The array of the times from the secondary PMTs
	 */
	private int time2[];

	/**
	 * The array of energies from each hit
	 */
	private double totalE[];

	/**
	 * The array of times from each hit
	 */
	private double totalT[];

	/**
	 * The effective speed of light in the veto
	 */
	private double v_eff;

	/**
	 * The charge-to-energy conversion factor for the primary PMT for the veto
	 */
	private double a_left;

	/**
	 * The charge-to-energy conversion factor for the secondary PMT for the veto
	 */
	private double a_right;

	/**
	 * The attenuation length
	 */
	private double lambda;

	/**
	 * Time delay for the primary PMT
	 */
	private double delta_L;

	/**
	 * Time delay for the secondary PMT
	 */
	private double delta_R;

	/**
	 * The tdc-to-time conversion factor for the primary PMT for the veto
	 */
	private double t_left;

	/**
	 * The tdc-to-time conversion factor for the secondary PMT for the veto
	 */
	private double t_right;

	/**
	 * The length of the veto
	 */
	private double l;

	/**
	 * The view this veto is in
	 */
	private FullSideView _view;

	/**
	 * Upper energy level (MeV) for color scaling
	 */
	private static final float upperEnergyScale = MathematicalConstants.UPPER_ENERGY_LIMIT;

	/**
	 * The rectangle the veto is drawn in
	 */
	private Rectangle2D.Double _worldRectangle;

	/**
	 * Constructor for the veto used in the full side view
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 * @param view
	 *            the FullSideView parent
	 * @param worldRectangle
	 *            the rectangle the veto is in
	 * @param veto
	 *            the veto (0-13)
	 */
	public FullSideViewVeto(LogicalLayer layer, FullSideView view,
			Rectangle2D.Double worldRectangle, int veto) {
		super(layer, worldRectangle);
		_worldRectangle = worldRectangle;
		_view = view;
		
		_style.setFillColor(Color.white);
		_style.setLineColor(Color.black);
		// TODO: TIDY
		// The internal upstream vetoes (vetoes 1, 2, 3, and 4 in one-based indexing) are optically
		// decoupled, so they are given the distinction of a thicker line width.
		if (veto == 1 || veto == 2 || veto == 3 || veto == 4) _style.setLineWidth(3);
		_veto = veto + 1;

		_name = "Veto: " + _veto;
	}

	/**
	 * Gets the calibration constants from a file and stores them
	 */
	public void getConstants(File file) {
		CalibrationFileParser calibrationFileParser = new CalibrationFileParser(file, "v", _veto);
		v_eff = calibrationFileParser.getEffectiveVelocity();
		a_left = calibrationFileParser.getLeftADCConversionFactor();
		a_right = calibrationFileParser.getRightADCConversionFactor();
		lambda = calibrationFileParser.getAttenuationLength();
		delta_L = calibrationFileParser.getLeftShift();
		delta_R = calibrationFileParser.getRightShift();
		t_left = calibrationFileParser.getLeftTDCConversionFactor();
		t_right = calibrationFileParser.getRightTDCConversionFactor();
		l = calibrationFileParser.getItemLength();
	}

	/**
	 * Custom drawer for the veto.
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
	 * Draw in single event mode
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the rendering container
	 */
	private void singleEventDrawItem(Graphics g, IContainer container) {

		// draw default, blank rectangle
		WorldGraphicsUtilities.drawWorldRectangle(g, container,
				_worldRectangle, Color.white, _style.getLineColor());

		// get the data and make sure it's not null
		ChargeTimeData ctData = EventManager.getInstance().getChargeTimeData();
		if (ctData != null) {
			hitVetoSectors = ctData.getVetoSector();
			hitIntOrExt = ctData.getVetoIntOrExt();
			hitChannels = ctData.getVetoChannel();
			charge1 = ctData.getVetoCharge1();
			time1 = ctData.getVetoTime1();
			charge2 = ctData.getVetoCharge2();
			time2 = ctData.getVetoTime2();

			// if we have hits
			if (charge1 != null && charge1 != null) {

				// convert to energy
				chargeToEnergy();

				for (int i = 0; i < totalE.length; i++) {

					// if the hits are in this veto
					if (inThisVeto(hitVetoSectors[i], hitIntOrExt[i],
							hitChannels[i])) {

						// if the energy is above 0 (extra check)
						if (totalE[i] > 0) {

							// draw red rectangle
							double scale = totalE[i] / upperEnergyScale;
							try {
								WorldGraphicsUtilities
										.drawWorldRectangle(
												g,
												container,
												_worldRectangle,
												new Color(
														(int) (Math
																.ceil(scale * 255)),
														0,
														(int) Math
																.ceil(255 - scale * 255)),
												_style.getLineColor());
							} catch (Exception e) {
								WorldGraphicsUtilities.drawWorldRectangle(g,
										container, _worldRectangle, new Color(
												255, 0, 0), _style
												.getLineColor());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Converts the charge-time information to energy-time information. The
	 * algorithm is the same as the bars for the top and bottom external vetoes
	 * since they have a PMT on each side, but for the other vetoes, we just use
	 * the conversion factor because we are mainly just using them as a
	 * true/false for hits.
	 */
	private void chargeToEnergy() {
		// these are external top and bottom vetoes which had ADC/TDC left and
		// right
		if (_veto == 8 || _veto == 9 || _veto == 11 || _veto == 12) {
			double t_l[] = new double[time1.length];
			double t_r[] = new double[time2.length];
			for (int i = 0; i < time1.length; i++) {
				t_l[i] = (time1[i] * 1.0 / t_left) - delta_L;
			}
			for (int i = 0; i < time2.length; i++) {
				t_r[i] = (time2[i] * 1.0 / t_right) - delta_R;
			}
			double posFromLeft[] = new double[charge1.length];
			for (int i = 0; i < posFromLeft.length; i++) {
				posFromLeft[i] = (v_eff * (t_l[i] - t_r[i]) + l) / 2.0;
			}
			double e_l[] = new double[charge1.length];
			double e_r[] = new double[charge2.length];
			for (int i = 0; i < e_l.length; i++) {
				e_l[i] = charge1[i] * a_left;
			}
			for (int i = 0; i < e_r.length; i++) {
				e_r[i] = charge2[i] * a_right;
			}
			totalE = new double[e_l.length];
			totalT = new double[t_l.length];
			for (int i = 0; i < totalE.length; i++) {
				double e_l_prime = e_l[i] * Math.exp(posFromLeft[i] / lambda);
				double e_r_prime = e_r[i]
						* Math.exp((l - posFromLeft[i]) / lambda);
				totalE[i] = (e_l_prime + e_r_prime) / 2;
				totalT[i] = (t_l[i] + t_r[i] - (l / v_eff)) / 2.0;
			}
		} else { // internal vetoes and remaining external vetoes
			totalE = new double[charge1.length];
			totalT = new double[time1.length];
			for (int i = 0; i < totalT.length; i++) {
				totalT[i] = time1[i] * 1.0 / t_left;
			}
			for (int i = 0; i < totalE.length; i++) {
				totalE[i] = charge1[i] * a_left;
			}
		}
	}

	/**
	 * Checks if a given sector/intOrExt/channel is this veto
	 * 
	 * @param sector
	 *            Which detector (currently not used)
	 * @param intOrExt
	 *            1 if interior, 2 if exterior
	 * @param channel
	 *            Which interior or exterior veto
	 * @return True if the s/ie/c is this veto, false if not
	 */
	private boolean inThisVeto(int sector, int intOrExt, int channel) {
		switch (intOrExt) {
		case 1:
			switch (channel) {
			case 0:
				return (1 == _veto);
			case 1:
				return (2 == _veto);
			case 2:
				return (3 == _veto);
			case 3:
				return (4 == _veto);
			case 4:
				return (5 == _veto);
			case 5:
				return (6 == _veto);
			}
			break;
		case 2:
			switch (channel) {
			case 0:
				return (7 == _veto);
			case 1:
				return (8 == _veto);
			case 2:
				return (9 == _veto);
			case 3:
				return (10 == _veto);
			case 4:
				return (11 == _veto);
			case 5:
				return (12 == _veto);
			case 6:
				return (13 == _veto);
			case 7:
				return (14 == _veto);
			}
			break;
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
			int vetoLayer = GetVetoLayer.getVetoLayer(_veto);
			String feedbackString = (vetoLayer == 1 ? "Crystal: " : (vetoLayer == 2 ? "Internal Veto: " : "External Veto: ")) + _veto;
			feedbackStrings.add(feedbackString);
			if (_view.getMode() == BedView.Mode.SINGLE_EVENT) {
				singleEventFeedbackStrings(feedbackStrings);
			} else {
				accumulatedFeedbackStrings(feedbackStrings);
			}
			/*
			 * double x = 0; double y = worldPoint.y; double z = 3 -
			 * worldPoint.x; z *= 10; y *= 10;
			 * 
			 * String rtp = "approx xyz " + DoubleFormat.doubleFormat(x, 1) +
			 * "cm, " + DoubleFormat.doubleFormat(y, 1) + "cm, " +
			 * DoubleFormat.doubleFormat(z, 1) + "cm"; feedbackStrings.add(rtp);
			 */
		}
	}

	/**
	 * Get the feedback strings for single event mode. Displays energy and time
	 * for each hit.
	 * 
	 * @param feedbackStrings
	 *            The list of feedback strings
	 */
	private void singleEventFeedbackStrings(List<String> feedbackStrings) {

		if (EventManager.getInstance().getChargeTimeData() != null) {
			if (totalE != null) {
				int hits = 0;
				double vetoE = 0;
				boolean hit[] = new boolean[totalE.length];
				for (int i = 0; i < totalE.length; i++) {
					if (inThisVeto(hitVetoSectors[i], hitIntOrExt[i],
							hitChannels[i])) {
						if (totalE[i] > 0) {
							hits++;
							vetoE += totalE[i];
							hit[i] = true;
						} else {
							hit[i] = false;
						}
					} else {
						hit[i] = false;
					}
				}
				String energyStr = "$orange$" + "Energy deposited:  " + vetoE
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
	}

}
