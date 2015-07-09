package infn.bed.util;

import infn.bed.geometry.GeometricConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parses a calibration file.
 * 
 * @author Angelo Licastro
 */
public class CalibrationFileParser {
	
	/**
	 * An instance of the File object.
	 */
	private final File file;
	
	/**
	 * The item (scintillator bar or veto).
	 */
	private final String item;
	
	/**
	 * The item (scintillator bar or veto) identification number.
	 */
	private final int identificationNumber;
	
	/**
	 * An ArrayList of item (scintillator bar or veto) tags.
	 */
	private final ArrayList<String> validationArrayList = new ArrayList<>();
	
	/**
	 * The comment initializer.
	 */
	private final String comment = Character.toString((char)35);
	
	/**
	 * The token delimiter.
	 */
	private final String delimiter = Character.toString((char)32);
	
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
	 * The item (scintillator bar or veto) length.
	 */
	private double itemLength;
	
	/**
	 * The constructor.
	 * 
	 * <p>
	 * NOTE: The constructor must call _parseConfigurationFile(). Additionally, validation is not intrinsic to _parseConfigurationFile().
	 * </p>
	 * 
	 * @param file The file to parse.
	 * @param item The item name (b for scintillator bar or v for veto).
	 * @param identificationNumber The identification number of the item.
	 * @throws InvalidCalibrationFileException If _isValidCalibrationFile() returns false, an unchecked exception is thrown.
	 */
	public CalibrationFileParser(File file, String item, int identificationNumber) {
		this.file = file;
		this.item = item;
		this.identificationNumber = identificationNumber;
		_populateValidationArray();
		if (_isValidCalibrationFile()) {
			_parseCalibrationFile();
		} else {
			throw new InvalidCalibrationFileException();
		}
	}
	
	/**
	 * Populates validationArrayList for use in _isValidCalibrationFile().
	 */
	private void _populateValidationArray() {
		for (int i = 1; i < GeometricConstants.BARS + 1; i++) {
			validationArrayList.add("b" + i);
		}
		for (int i = 1; i < GeometricConstants.CRYSTALS + 1; i++) {
			validationArrayList.add("v" + i);
		}
		for (int i = 1 + GeometricConstants.CRYSTALS; i < GeometricConstants.VETOES + 1; i++) {
			validationArrayList.add("v" + i);
		}
	}
	
	/**
	 * Formats a string by trimming excessive whitespace.
	 * 
	 * @param s The string to be formatted.
	 * @return The formatted string.
	 */
	private String _format(String s) {
		return s.replaceAll("\\s+", Character.toString((char)32)).trim();
	}
	
	/**
	 * Validates the calibration file.
	 * 
	 * @return true if the calibration file is valid, false otherwise.
	 */
	private boolean _isValidCalibrationFile() {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int i = 0;
			while (i < validationArrayList.size()) {
				String s = bufferedReader.readLine();
				if (!s.startsWith(comment) && s.length() > 0) {
					String tag = _format(s).split(delimiter)[0];
					if (!(validationArrayList.get(i).equals(tag))) {
						return false;
					}
					i++;
				}
			}
			bufferedReader.close();
		} catch (NullPointerException e) {
			throw new InvalidCalibrationFileException();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Parses the calibration file.
	 */
	private void _parseCalibrationFile() {
		if (file == null || !file.exists()) {
			// Oops. Something went wrong.
		} else {
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String tag = item + identificationNumber;
				boolean found = false;
				while (!found) {
					String s = bufferedReader.readLine();
					if (s == null) {
						break;
					} else {
						if (!s.startsWith(comment) && s.length() > 0) {
							String[] tokens = _format(s).split(delimiter);
							if (tokens[0].equals(tag)) {
								found = true;
								effectiveVelocity        = Double.parseDouble(tokens[1]);
								leftADCConversionFactor  = Double.parseDouble(tokens[2]);
								rightADCConversionFactor = Double.parseDouble(tokens[3]);
								attenuationLength        = Double.parseDouble(tokens[4]);
								leftShift                = Double.parseDouble(tokens[5]);
								rightShift               = Double.parseDouble(tokens[6]);
								leftTDCConversionFactor  = Double.parseDouble(tokens[7]);
								rightTDCConversionFactor = Double.parseDouble(tokens[8]);
								itemLength               = Double.parseDouble(tokens[9]);
								bufferedReader.close();
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the effective velocity.
	 * 
	 * @return effectiveVelocity The effective velocity;
	 */
	public double getEffectiveVelocity() {
		return effectiveVelocity;
	}
	
	/**
	 * Returns the left ADC (analog-to-digital converter) conversion factor.
	 * 
	 * @return leftADCConversionFactor The left ADC (analog-to-digital converter) conversion factor.
	 */
	public double getLeftADCConversionFactor() {
		return leftADCConversionFactor;
	}
	
	/**
	 * Returns the right ADC (analog-to-digital converter) conversion factor.
	 * 
	 * @return rightADCConversionFactor The right ADC (analog-to-digital converter) conversion factor.
	 */
	public double getRightADCConversionFactor() {
		return rightADCConversionFactor;
	}
	
	/**
	 * Returns the attenuation length.
	 * 
	 * @return attenuationLength The attenuation length.
	 */
	public double getAttenuationLength() {
		return attenuationLength;
	}
	
	/**
	 * Returns the left shift.
	 * 
	 * @return leftShift The left shift.
	 */
	public double getLeftShift() {
		return leftShift;
	}
	
	/**
	 * Returns the right shift.
	 * 
	 * @return rightShift The right shift.
	 */
	public double getRightShift() {
		return rightShift;
	}
	
	/**
	 * Returns the left TDC (time-to-digital converter) conversion factor.
	 * 
	 * @return leftTDCConversionFactor The left TDC (time-to-digital converter) conversion factor.
	 */
	public double getLeftTDCConversionFactor() {
		return leftTDCConversionFactor;
	}
	
	/**
	 * Returns the right TDC (time-to-digital converter) conversion factor.
	 * 
	 * @return rightTDCConversionFactor The right TDC (time-to-digital converter) conversion factor.
	 */
	public double getRightTDCConversionFactor() {
		return rightTDCConversionFactor;
	}
	
	/**
	 * Returns the item (scintillator bar or veto) length.
	 * 
	 * @return itemLength The item (scintillator bar or veto) length.
	 */
	public double getItemLength() {
		return itemLength;
	}
	
}