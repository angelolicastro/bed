package infn.bed.util;

/**
 * An unchecked exception that is thrown when a calibration file is invalid.
 * 
 * @author Angelo Licastro
 */
@SuppressWarnings("serial")
public class InvalidCalibrationFileException extends RuntimeException {
	
	/**
	 * The constructor.
	 */
	public InvalidCalibrationFileException() {
		super();
	}
	
}