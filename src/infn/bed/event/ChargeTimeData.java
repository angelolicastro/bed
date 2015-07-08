package infn.bed.event;

import infn.bed.geometry.GeometricConstants;
import infn.bed.math.MathematicalConstants;

import java.util.ArrayList;
import java.util.Vector;

import org.jlab.coda.jevio.IEvioStructure;

import cnuphys.lund.LundId;

/**
 * Parses a charge-time data file and converts full-waveform data to charge-time data.
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
public class ChargeTimeData implements ILoad {

	/**
	 * An array of hit sectors (detectors).
	 */
	private int sectorArray[];

	/**
	 * An array of hit layers (columns).
	 */
	private int layerArray[];

	/**
	 * An array of hit paddles (rows).
	 */
	private int paddleArray[];

	/**
	 * An array of left PMT (photomultiplier tube) hit charges.
	 */
	private int leftPMTChargeArray[];

	/**
	 * An array of right PMT (photomultiplier tube) hit charges.
	 */
	private int rightPMTChargeArray[];

	/**
	 * An array of left PMT (photomultiplier tube) hit times.
	 */
	private int leftPMTTimeArray[];

	/**
	 * An array of right PMT (photomultiplier tube) hit times.
	 */
	private int rightPMTTimeArray[];

	/**
	 * An array of veto hit sectors (detectors).
	 */
	private int vetoSectorArray[];

	/**
	 * An array of veto hit layers (internal or external).
	 */
	private int vetoLayerArray[];

	/**
	 * An array of veto hit channels.
	 * 
	 * @see infn.bed.view.FullSideView
	 */
	private int vetoChannelArray[];

	/**
	 * An array of veto hit charges.
	 */
	private int vetoChargeArray[];

	/**
	 * An array of dual SiPM (silicon photomultiplier) veto hit charges.
	 * 
	 * <p>
	 * NOTE: This array should only contain charges from the external top and bottom vetoes.
	 * </p>
	 */
	private int dualSiPMVetoChargeArray[];

	/**
	 * An array of veto hit times.
	 */
	private int vetoTimeArray[];

	/**
	 * An array of dual SiPM (silicon photomultiplier) veto hit times.
	 * 
	 * <p>
	 * NOTE: This array should only contain times from the external top and bottom vetoes.
	 * </p>
	 */
	private int dualSiPMVetoTimeArray[];

	/**
	 * The constructor.
	 */
	public ChargeTimeData() {
		super();
	}

	/**
	 * Converts full-waveform data to charge-time data.
	 * 
	 * @param sampleArrayList An ArrayList of PMT (photomultiplier tube) full-waveform data.
	 */
	public ChargeTimeData(ArrayList<ArrayList<Short>> sampleArrayList) {
		ArrayList<Double> leftPMTChargeArrayList = new ArrayList<>();
		ArrayList<Double> leftPMTTimeArrayList = new ArrayList<>();
		
		ArrayList<Double> rightPMTChargeArrayList = new ArrayList<>();
		ArrayList<Double> rightPMTTimeArrayList = new ArrayList<>();
		
		ArrayList<Integer> sectorArrayList = new ArrayList<>();
		ArrayList<Integer> layerArrayList = new ArrayList<>();
		ArrayList<Integer> paddleArrayList = new ArrayList<>();
		
		for (int i = 0; i < sampleArrayList.size(); i++) {
			if (i < (GeometricConstants.BARS * 2)) {
				ArrayList<Short> leftPMTSampleArrayList = sampleArrayList.get(i);
				ArrayList<Short> rightPMTSampleArrayList = sampleArrayList.get(i + 1);
				
				int[] barLeftPMTArray = TranslationTable.bars[i];
				int[] barRightPMTArray = TranslationTable.bars[i + 1];
				
				@SuppressWarnings("unused")
				int barLeftPMTIndex = barLeftPMTArray[0];
				int barLeftPMTSector = barLeftPMTArray[1];
				int barLeftPMTLayer = barLeftPMTArray[2];
				int barLeftPMTPaddle = barLeftPMTArray[3];
				
				@SuppressWarnings("unused")
				int barRightPMTIndex = barRightPMTArray[0];
				int barRightPMTSector = barRightPMTArray[1];
				int barRightPMTLayer = barRightPMTArray[2];
				int barRightPMTPaddle = barRightPMTArray[3];
				
				int barLeftPMTHits = convertHits(leftPMTSampleArrayList, leftPMTChargeArrayList, leftPMTTimeArrayList);
				int barRightPMTHits = convertHits(rightPMTSampleArrayList, rightPMTChargeArrayList, rightPMTTimeArrayList);
				
				for (int hit = 0; hit < barLeftPMTHits; hit++) {
					sectorArrayList.add(barLeftPMTSector);
					layerArrayList.add(barLeftPMTLayer);
					paddleArrayList.add(barLeftPMTPaddle);
				}
				
				for (int hit = 0; hit < barRightPMTHits; hit++) {
					sectorArrayList.add(barRightPMTSector);
					layerArrayList.add(barRightPMTLayer);
					paddleArrayList.add(barRightPMTPaddle);
				}
				i++;
			} else {
				// TODO: Implement full-waveform data to charge-time data conversion for vetoes.
			}
		}

		sectorArray = getIntArrayFromIntegerArrayList(sectorArrayList);
		layerArray = getIntArrayFromIntegerArrayList(layerArrayList);
		paddleArray = getIntArrayFromIntegerArrayList(paddleArrayList);
		
		leftPMTChargeArray = getIntArrayFromDoubleArrayList(leftPMTChargeArrayList);
		rightPMTChargeArray = getIntArrayFromDoubleArrayList(rightPMTChargeArrayList);
		
		leftPMTTimeArray = getIntArrayFromDoubleArrayList(leftPMTTimeArrayList);
		rightPMTTimeArray = getIntArrayFromDoubleArrayList(rightPMTTimeArrayList);
	}
	
	/**
	 * @param sampleArrayList An ArrayList of samples.
	 * @param chargeArrayList An ArrayList of charges.
	 * @param timeArrayList An ArrayList of times.
	 * @return hits The number of hits.
	 */
	private int convertHits(ArrayList<Short> sampleArrayList, ArrayList<Double> chargeArrayList, ArrayList<Double> timeArrayList) {
		int hits = 0;
		double a_L = 0;
		double b_L = 0;
		double charge = 0;
		double time = 0;
		boolean collectingPulse = false;
		for (int i = 1; i < (sampleArrayList.size() - 1); i++) {
			if (sampleArrayList.get(i) > MathematicalConstants.ADC_THRESHOLD && sampleArrayList.get(i - 1) < MathematicalConstants.ADC_THRESHOLD) {
				a_L = sampleArrayList.get(i + 1) - sampleArrayList.get(i - 1) * 1 / 4;
				b_L = sampleArrayList.get(i + 1) - a_L * (i - 1) * 4;
				charge = charge + (sampleArrayList.get(i) / MathematicalConstants.FADC_RESISTANCE) * (i - 1) * 4;
				collectingPulse = true;
			} else if ((sampleArrayList.get(i + 1) < sampleArrayList.get(i)) && (sampleArrayList.get(i - 1) < sampleArrayList.get(i)) && (sampleArrayList.get(i) > MathematicalConstants.ADC_THRESHOLD)) {
				time = sampleArrayList.get(i) / 2;
				time = time - b_L;
				time = time / a_L;
				charge = charge + (sampleArrayList.get(i) / MathematicalConstants.FADC_RESISTANCE) * (i - 1) * 4;
			} else if ((sampleArrayList.get(i) > MathematicalConstants.ADC_THRESHOLD) && (sampleArrayList.get(i + 1) < MathematicalConstants.ADC_THRESHOLD)) {
				charge = charge + (sampleArrayList.get(i) / MathematicalConstants.FADC_RESISTANCE) * (i - 1) * 4;
				chargeArrayList.add(charge);
				timeArrayList.add(time);
				hits++;
				a_L = 0;
				b_L = 0;
				charge = 0;
				time = 0;
				collectingPulse = false;
			} else if (collectingPulse) {
				charge = charge + (sampleArrayList.get(i) / MathematicalConstants.FADC_RESISTANCE) * (i - 1) * 4;
			}
		}
		return hits;
	}

	/**
	 * Converts an ArrayList of Integers to an array of ints.
	 * 
	 * @param integerArrayList An ArrayList of Integers.
	 * @return intArray An array of ints.
	 */
	private int[] getIntArrayFromIntegerArrayList(ArrayList<Integer> integerArrayList) {
		int intArray[] = new int[integerArrayList.size()];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = integerArrayList.get(i);
		}
		return intArray;
	}
	
	/**
	 * Converts an ArrayList of Doubles to an array of ints.
	 * 
	 * @param doubleArrayList An ArrayList of Doubles.
	 * @return intArray An array of ints.
	 */
	private int[] getIntArrayFromDoubleArrayList(ArrayList<Double> doubleArrayList) {
		int intArray[] = new int[doubleArrayList.size()];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = (int)((double)doubleArrayList.get(i));
		}
		return intArray;
	}

	/**
	 * Loads charge-time data from a charge-time file.
	 * 
	 * @param structure An instance of the IEvioStructure object.
	 * @param tag The tag of the bank.
	 * @param num The num of the bank.
	 */
	@Override
	public void load(IEvioStructure structure, int tag, int num) {
		try {
			// Scintillator Bar
			if (tag == 102) {
				switch (num) {
				// Scintillator Bar Sector
				case 1:
					sectorArray = structure.getIntData();
					break;
				// Scintillator Bar Layer
				case 2:
					layerArray = structure.getIntData();
					break;
				// Scintillator Bar Paddle
				case 3:
					paddleArray = structure.getIntData();
					break;
				// Scintillator Bar Left PMT Charge
				case 4:
					leftPMTChargeArray = structure.getIntData();
					break;
				// Scintillator Bar Right PMT Charge
				case 5:
					rightPMTChargeArray = structure.getIntData();
					break;
				// Scintillator Bar Left PMT Time
				case 6:
					leftPMTTimeArray = structure.getIntData();
					break;
				// Scintillator Bar Right PMT Time
				case 7:
					rightPMTTimeArray = structure.getIntData();
					break;
				}
			// Veto
			} else if (tag == 202) {
				switch (num) {
				// Veto Sector
				case 1:
					vetoSectorArray = structure.getIntData();
					break;
				// Veto Layer
				case 2:
					vetoLayerArray = structure.getIntData();
					break;
				// Veto Channel
				case 3:
					vetoChannelArray = structure.getIntData();
					break;
				// Veto Charge
				case 4:
					vetoChargeArray = structure.getIntData();
					break;
				// Dual SiPM Veto Charge
				case 5:
					dualSiPMVetoChargeArray = structure.getIntData();
					break;
				// Veto Time
				case 6:
					vetoTimeArray = structure.getIntData();
					break;
				// Dual SiPM Veto Charge
				case 7:
					dualSiPMVetoTimeArray = structure.getIntData();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Currently unused.
	 * 
	 * @see infn.bed.event.ILoad#uniqueLundIds()
	 */
	@Override
	public Vector<LundId> uniqueLundIds() {
		return null;
	}

	/**
	 * Returns the array of hit sectors (detectors).
	 * 
	 * @return The array of hit sectors (detectors).
	 */
	public int[] getSectorArray() {
		return sectorArray;
	}

	/**
	 * Returns the array of hit layers (columns).
	 * 
	 * @return The array of hit layers (columns).
	 */
	public int[] getLayerArray() {
		return layerArray;
	}

	/**
	 * Returns the array of hit paddles (rows).
	 * 
	 * @return The array of hit paddles (rows).
	 */
	public int[] getPaddleArray() {
		return paddleArray;
	}

	/**
	 * Returns the array of left PMT (photomultiplier tube) hit charges.
	 * 
	 * @return The array of left PMT (photomultiplier tube) hit charges.
	 */
	public int[] getLeftPMTChargeArray() {
		return leftPMTChargeArray;
	}

	/**
	 * Returns the array of right PMT (photomultiplier tube) hit charges.
	 * 
	 * @return The array of right PMT (photomultiplier tube) hit charges.
	 */
	public int[] getRightPMTChargeArray() {
		return rightPMTChargeArray;
	}

	/**
	 * Returns the array of left PMT (photomultiplier tube) hit times.
	 * 
	 * @return The array of left PMT (photomultiplier tube) hit times.
	 */
	public int[] getLeftPMTTimeArray() {
		return leftPMTTimeArray;
	}

	/**
	 * Returns the array of right PMT (photomultiplier tube) hit times.
	 * 
	 * @return The array of right PMT (photomultiplier tube) hit times.
	 */
	public int[] getRightPMTTimeArray() {
		return rightPMTTimeArray;
	}

	/**
	 * Returns the array of veto hit sectors (detectors).
	 * 
	 * @return The array of veto hit sectors (detectors).
	 */
	public int[] getVetoSectorArray() {
		return vetoSectorArray;
	}

	/**
	 * Returns the array of veto hit layers (internal or external).
	 * 
	 * @return The array of veto hit layers (internal or external).
	 */
	public int[] getVetoLayerArray() {
		return vetoLayerArray;
	}

	/**
	 * Returns the array of veto hit channels.
	 * 
	 * @return The array of veto hit channels.
	 */
	public int[] getVetoChannelArray() {
		return vetoChannelArray;
	}

	/**
	 * Returns the array of veto hit charges.
	 * 
	 * @return The array of veto hit charges.
	 */
	public int[] getVetoChargeArray() {
		return vetoChargeArray;
	}

	/**
	 * Returns the array of dual SiPM (silicon photomultiplier) veto hit charges.
	 * 
	 * @return The array of dual SiPM (silicon photomultiplier) veto hit charges.
	 */
	public int[] getDualSiPMVetoChargeArray() {
		return dualSiPMVetoChargeArray;
	}

	/**
	 * Returns the array of veto hit times.
	 * 
	 * @return The array of veto hit times.
	 */
	public int[] getVetoTimeArray() {
		return vetoTimeArray;
	}

	/**
	 * Returns the array of dual SiPM (silicon photomultiplier) veto hit times.
	 * 
	 * @return The array of dual SiPM (silicon photomultiplier) veto hit times.
	 */
	public int[] getDualSiPMVetoTimeArray() {
		return dualSiPMVetoTimeArray;
	}

}
