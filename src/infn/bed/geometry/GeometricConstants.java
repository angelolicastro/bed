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
	 * The number of detector channels.
	 */
	public static final int CHANNELS = 18;
	
	/**
	 * The number of detector rows.
	 */
	public static final int ROWS = 3;
	
	/**
	 * The number of detector columns.
	 */
	public static final int COLUMNS = CHANNELS / ROWS;
	
	/**
	 * The number of crystals.
	 */
	public static final int CRYSTALS = 4;
	
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
	public static final int VETOES = CRYSTALS + INTERNAL_VETOES + EXTERNAL_VETOES;

}