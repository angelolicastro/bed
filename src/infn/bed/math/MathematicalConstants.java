package infn.bed.math;

/**
 * Defines the mathematical constants of the detector.
 * 
 * @author Angelo Licastro
 */
public class MathematicalConstants {
	
	/**
	 * The threshold of the ADC (analog-to-digital converter) in channel units (uncalibrated).
	 */
	public static final int ADC_THRESHOLD = 300;
	
	/**
	 * The resistance of the FADC (flash analog-to-digital converter) in ohms.
	 */
	public static final int FADC_RESISTANCE = 50;
	
	/**
	 * The upper energy limit in MeV.
	 */
	public static final float UPPER_ENERGY_LIMIT = 50f;
	
}