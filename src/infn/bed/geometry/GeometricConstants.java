package infn.bed.geometry;

/**
 * Defines the geometric constants of the detector.
 * 
 * @author David Heddle
 * @author Andy Beiter
 * @author Angelo Licastro
 */
public class GeometricConstants {

	/**
	 * The number of scinatillator bars.
	 */
	public static final int BARS = 9;
	
	/**
	 * The number of crystal vetoes.
	 */
	public static final int CRYSTAL_VETOES = 1;
	
	/**
	 * The number of internal vetoes.
	 */
	public static final int INTERNAL_VETOES = 18;
	
	/**
	 * The number of external vetoes.
	 */
	public static final int EXTERNAL_VETOES = 12;

	/**
	 * The total number of vetoes.
	 */
	public static final int VETOES = CRYSTAL_VETOES + INTERNAL_VETOES + EXTERNAL_VETOES;

}