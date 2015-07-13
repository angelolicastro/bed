package infn.bed.item;

import infn.bed.config.FullSideViewConfig;
import infn.bed.event.ChargeTimeData;
import infn.bed.event.EventManager;
import infn.bed.geometry.GeometricConstants;
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
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.RectangleItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;

/**
 * Draws the Full Side View veto rectangles and hits.
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
public class FullSideViewVeto extends RectangleItem {

	/**
	 * The font of the label text.
	 */
	private static final Font labelTextFont = Fonts.commonFont(Font.PLAIN, 11);

	/**
	 * The number of the veto.
	 */
	private final int _veto;

	/**
	 * An array of hit sectors (detectors).
	 */
	private int sectorArray[];

	/**
	 * An array of hit layers (internal or external).
	 */
	private int layerArray[];

	/**
	 * An array of hit channels.
	 */
	private int channelArray[];

	/**
	 * An array of hit charges.
	 */
	private int chargeArray[];

	/**
	 * An array of dual SiPM (silicon photomultiplier) hit charges.
	 */
	private int dualSiPMChargeArray[];

	/**
	 * An array of hit times.
	 */
	private int timeArray[];

	/**
	 * An array of dual SiPM (silicon photomultiplier) hit times.
	 */
	private int dualSiPMTimeArray[];

	/**
	 * An array of total hit energies.
	 */
	private double totalEnergyArray[];

	/**
	 * An array of total hit times.
	 */
	private double totalTimeArray[];

	/**
	 * The effective velocity.
	 */
	private double effectiveVelocity;

	/**
	 * The left ADC (analog-to-digital converter) conversion factor.
	 */
	private double leftADCConversionFactor;

	/**
	 * The right ADC (analog-to-digital converter) conversion factor.
	 */
	private double rightADCConversionFactor;

	/**
	 * The attenuation length.
	 */
	private double attenuationLength;

	/**
	 * The left shift.
	 */
	private double leftShift;

	/**
	 * The right shift.
	 */
	private double rightShift;

	/**
	 * The left TDC (time-to-digital converter) conversion factor.
	 */
	private double leftTDCConversionFactor;

	/**
	 * The right TDC (time-to-digital converter) conversion factor.
	 */
	private double rightTDCConversionFactor;

	/**
	 * The veto length.
	 */
	private double vetoLength;

	/**
	 * The view that contains the veto.
	 */
	private final FullSideView _view;

	/**
	 * The world that contains the veto.
	 */
	private final Rectangle2D.Double _worldRectangle;

	/**
	 * The constructor.
	 * 
	 * @param layer The layer that contains the veto.
	 * @param view The view that contains the veto.
	 * @param worldRectangle The world that contains the veto.
	 * @param veto The number of the veto in zero-based indexing.
	 */
	public FullSideViewVeto(LogicalLayer layer, FullSideView view, Rectangle2D.Double worldRectangle, int veto) {
		super(layer, worldRectangle);
		
		_view = view;
		_worldRectangle = worldRectangle;
		_veto = veto + 1;
		_name = "Veto: " + _veto;
		
		_style.setFillColor(Color.white);
		
		if (isInternalUpstreamVeto()) {
			_style.setLineWidth(3);
		}
	}

	/**
	 * Retrieves the calibration constants of the veto.
	 * 
	 * @param file A calibration file.
	 */
	public void getConstants(File file) {
		CalibrationFileParser calibrationFileParser = new CalibrationFileParser(file, "v", _veto);
		effectiveVelocity        = calibrationFileParser.getEffectiveVelocity();
		leftADCConversionFactor  = calibrationFileParser.getLeftADCConversionFactor();
		rightADCConversionFactor = calibrationFileParser.getRightADCConversionFactor();
		attenuationLength        = calibrationFileParser.getAttenuationLength();
		leftShift                = calibrationFileParser.getLeftShift();
		rightShift               = calibrationFileParser.getRightShift();
		leftTDCConversionFactor  = calibrationFileParser.getLeftTDCConversionFactor();
		rightTDCConversionFactor = calibrationFileParser.getRightTDCConversionFactor();
		vetoLength               = calibrationFileParser.getItemLength();
	}

	/**
	 * Draws the veto.
	 * 
	 * @param g The graphics context.
	 * @param container The graphics container that is being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		if (EventControl.getInstance().isAccumulating()) {
			return;
		}
		
		super.drawItem(g, container);
		g.setFont(labelTextFont);

		if (_view.getMode() == BedView.Mode.SINGLE_EVENT) {
			singleEventDrawItem(g, container);
		} else {
			accumulatedDrawItem(g, container);
		}
	}

	/**
	 * Draws the single event mode hits.
	 * 
	 * @param g The graphics context.
	 * @param container The graphics container that is being rendered.
	 */
	private void singleEventDrawItem(Graphics g, IContainer container) {
		WorldGraphicsUtilities.drawWorldRectangle(g, container, _worldRectangle, Color.white, getLineColor());
		
		ChargeTimeData chargeTimeData = EventManager.getInstance().getChargeTimeData();
		if (chargeTimeData != null) {
			sectorArray         = chargeTimeData.getVetoSectorArray();
			layerArray          = chargeTimeData.getVetoLayerArray();
			channelArray        = chargeTimeData.getVetoChannelArray();
			chargeArray         = chargeTimeData.getVetoChargeArray();
			timeArray           = chargeTimeData.getVetoTimeArray();
			dualSiPMChargeArray = chargeTimeData.getDualSiPMVetoChargeArray();
			dualSiPMTimeArray   = chargeTimeData.getDualSiPMVetoTimeArray();

			if (chargeArray != null) {
				chargeToEnergy();
				for (int i = 0; i < totalEnergyArray.length; i++) {
					if (inThisVeto(sectorArray[i], layerArray[i], channelArray[i])) {
						if (totalEnergyArray[i] > 0) {
							double scaleFactor = totalEnergyArray[i] / MathematicalConstants.UPPER_ENERGY_LIMIT;
							try {
								WorldGraphicsUtilities.drawWorldRectangle(g, container, _worldRectangle, new Color((int)(Math.ceil(scaleFactor * 255)), 0, (int)Math.ceil(255 - scaleFactor * 255)), getLineColor());
							} catch (Exception e) {
								WorldGraphicsUtilities.drawWorldRectangle(g, container, _worldRectangle, new Color(255, 0, 0), _style.getLineColor());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Converts charge-time information to energy-time information.
	 */
	private void chargeToEnergy() {
		if (_veto == 8 || _veto == 9 || _veto == 11 || _veto == 12) {
			double leftTimeArray[] = new double[timeArray.length];
			double rightTimeArray[] = new double[dualSiPMTimeArray.length];
			for (int i = 0; i < timeArray.length; i++) {
				leftTimeArray[i] = (timeArray[i] / leftTDCConversionFactor) - leftShift;
			}
			for (int i = 0; i < dualSiPMTimeArray.length; i++) {
				rightTimeArray[i] = (dualSiPMTimeArray[i] / rightTDCConversionFactor) - rightShift;
			}
			double positionFromLeftArray[] = new double[chargeArray.length];
			for (int i = 0; i < positionFromLeftArray.length; i++) {
				positionFromLeftArray[i] = (effectiveVelocity * (leftTimeArray[i] - rightTimeArray[i]) + vetoLength) / 2;
			}
			double leftEnergyArray[] = new double[chargeArray.length];
			double rightEnergyArray[] = new double[dualSiPMChargeArray.length];
			for (int i = 0; i < leftEnergyArray.length; i++) {
				leftEnergyArray[i] = chargeArray[i] * leftADCConversionFactor;
			}
			for (int i = 0; i < rightEnergyArray.length; i++) {
				rightEnergyArray[i] = dualSiPMChargeArray[i] * rightADCConversionFactor;
			}
			totalEnergyArray = new double[leftEnergyArray.length];
			totalTimeArray = new double[leftTimeArray.length];
			for (int i = 0; i < totalEnergyArray.length; i++) {
				double leftEnergyPrime = leftEnergyArray[i] * Math.exp(positionFromLeftArray[i] / attenuationLength);
				double rightEnergyPrime = rightEnergyArray[i] * Math.exp((vetoLength - positionFromLeftArray[i]) / attenuationLength);
				totalEnergyArray[i] = (leftEnergyPrime + rightEnergyPrime) / 2;
				totalTimeArray[i] = (leftTimeArray[i] + rightTimeArray[i] - (vetoLength / effectiveVelocity)) / 2;
			}
		} else {
			totalEnergyArray = new double[chargeArray.length];
			totalTimeArray = new double[timeArray.length];
			for (int i = 0; i < totalTimeArray.length; i++) {
				totalTimeArray[i] = timeArray[i] / leftTDCConversionFactor;
			}
			for (int i = 0; i < totalEnergyArray.length; i++) {
				totalEnergyArray[i] = chargeArray[i] * leftADCConversionFactor;
			}
		}
	}

	/**
	 * Returns true if the sector, layer, and channel combination is this veto, false otherwise.
	 * 
	 * @param sector The number of the sector.
	 * @param layer The number of the layer (1 if the layer is interior or 2 if the layer is exterior).
	 * @param channel The number of the channel.
	 * @return true if the sector, layer, and channel combination is this veto, false otherwise.
	 */
	private boolean inThisVeto(int sector, int layer, int channel) {
		switch (layer) {
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
	 * Draws the accumulated mode hits.
	 * 
	 * @param g The graphics context.
	 * @param container The graphics container that is being rendered.
	 */
	private void accumulatedDrawItem(Graphics g, IContainer container) { }

	/**
	 * Add any appropriate feedback strings for the heads-up display or feedback
	 * panel.
	 * 
	 * @param container The graphics container that is being rendered.
	 * @param screenPoint The location of the mouse.
	 * @param worldPoint A point in the corresponding world.
	 * @param feedbackStringList A list of feedback strings.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint, List<String> feedbackStringList) {
		if (_worldRectangle.contains(worldPoint)) {
			int vetoLayer = GetVetoLayer.getVetoLayer(_veto);
			String feedbackString = "\n" + (vetoLayer == 1 ? "Crystal n." : (vetoLayer == 2 ? "Internal Veto n." : "External Veto n.")) + _veto + "\n";
			feedbackStringList.add(feedbackString);
			if (_view.getMode() == BedView.Mode.SINGLE_EVENT) {
				singleEventFeedbackStrings(feedbackStringList);
			} else {
				accumulatedFeedbackStrings(feedbackStringList);
			}
			 double x = 0;
			 double y = worldPoint.y * 10;
			 double z = (3 - worldPoint.x) * 10;
			 String approximateWorldLocation = "\nApproximate World Location:\nx = " + DoubleFormat.doubleFormat(x, 1) + " cm\ny = " + DoubleFormat.doubleFormat(y, 1) + " cm\nz = " + DoubleFormat.doubleFormat(z, 1) + " cm\n";
			 feedbackStringList.add(approximateWorldLocation);
		}
	}

	/**
	 * Collects the single event mode feedback strings.
	 * 
	 * @param feedbackStringList A list of feedback strings.
	 */
	private void singleEventFeedbackStrings(List<String> feedbackStringList) {
		if (EventManager.getInstance().getChargeTimeData() != null) {
			if (totalEnergyArray != null) {
				int hits = 0;
				double vetoEnergy = 0;
				boolean hitArray[] = new boolean[totalEnergyArray.length];
				for (int i = 0; i < totalEnergyArray.length; i++) {
					if (inThisVeto(sectorArray[i], layerArray[i], channelArray[i])) {
						if (totalEnergyArray[i] > 0) {
							hits++;
							vetoEnergy = vetoEnergy + totalEnergyArray[i];
							hitArray[i] = true;
						} else {
							hitArray[i] = false;
						}
					} else {
						hitArray[i] = false;
					}
				}
				String eventFeedbackString = "$orange$" + "\nEnergy Deposited: " + vetoEnergy + " MeV\nNumber of Hits: " + hits;
				int counter = 1;
				for (int i = 0; i < hitArray.length; i++) {
					if (hitArray[i]) {
						eventFeedbackString = eventFeedbackString + "\nTime n." + counter + ": " + totalTimeArray[i] + " ns";
					}
				}
				feedbackStringList.add(eventFeedbackString);
			}
		}
	}

	/**
	 * Collects the accumulated mode feedback strings.
	 * 
	 * @param feedbackStringList A list of feedback strings.
	 */
	private void accumulatedFeedbackStrings(List<String> feedbackStringList) { }
	
	/**
	 * Returns the line color of the veto.
	 * 
	 * @return The line color of the veto.
	 */
	private Color getLineColor() {
		if (GetVetoLayer.getVetoLayer(_veto) == 1) {
			return FullSideViewConfig.CRYSTALS_LINE_COLOR;
		} else if (GetVetoLayer.getVetoLayer(_veto) == 2) {
			return FullSideViewConfig.INTERNAL_VETOES_LINE_COLOR;
		} else if (GetVetoLayer.getVetoLayer(_veto) == 3) {
			return FullSideViewConfig.EXTERNAL_VETOES_LINE_COLOR;
		}
		return new Color(0, 0, 0);
	}
	
	/**
	 * Returns true if the veto is an internal upstream veto, false otherwise.
	 * 
	 * @return true if the veto is an internal upstream veto, false otherwise.
	 */
	private boolean isInternalUpstreamVeto() {
		return _veto >= GeometricConstants.CRYSTALS + 1 && _veto <= GeometricConstants.CRYSTALS + 4;
	}

}
