package infn.bed.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
	private String item;
	
	/**
	 * The item (scintillator bar or veto) identification number.
	 */
	private final int id;
	
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
	private double v_eff;
	
	/**
	 * The left ADC (analog-to-digital converter) conversion factor.
	 */
	private double a_left;
	
	/**
	 * The right ADC (analog-to-digital converter) conversion factor.
	 */
	private double a_right;
	
	/**
	 * The attenuation length.
	 */
	private double lambda;
	
	/**
	 * The left shift.
	 */
	private double delta_L;
	
	/**
	 * The right shift.
	 */
	private double delta_R;
	
	/**
	 * The left TDC (time-to-digital converter) conversion factor.
	 */
	private double t_left;
	
	/**
	 * The right TDC (time-to-digital converter) conversion factor.
	 */
	private double t_right;
	
	/**
	 * The item (scintillator bar or veto) length.
	 */
	private double l;
	
	/**
	 * The constructor.
	 * 
	 * Note: The constructor must call _parseConfigurationFile().
	 * 
	 * @param file The file to parse.
	 * @param item The item name (b for scintillator bar or v for veto).
	 * @param id The identification number of the item.
	 */
	public CalibrationFileParser(File file, String item, int id) {
		this.file = file;
		this.item = item;
		this.id = id;
		_parseConfigurationFile();
	}
	
	/**
	 * Parses the calibration file.
	 */
	private void _parseConfigurationFile() {
		if (file == null || !file.exists()) {
			// Oops. Something went wrong.
		} else {
			try {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String tag = item + id;
				boolean found = false;
				while (!found) {
					String s = bufferedReader.readLine();
					if (s == null) {
						break;
					} else {
						// Is the line a comment?
						if (!s.startsWith(comment) && s.length() > 0) {
							// Tokenize the line.
							String[] tokens = s.split(delimiter);
							if (tokens[0].equals(tag)) {
								found = true;
								v_eff = Double.parseDouble(tokens[1]);
								a_left = Double.parseDouble(tokens[2]);
								a_right = Double.parseDouble(tokens[3]);
								lambda = Double.parseDouble(tokens[4]);
								delta_L = Double.parseDouble(tokens[5]);
								delta_R = Double.parseDouble(tokens[6]);
								t_left = Double.parseDouble(tokens[7]);
								t_right = Double.parseDouble(tokens[8]);
								l = Double.parseDouble(tokens[9]);
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
	 * @return v_eff The effective velocity;
	 */
	public double getEffectiveVelocity() {
		return v_eff;
	}
	
	/**
	 * Returns the left ADC (analog-to-digital converter) conversion factor.
	 * 
	 * @return a_left The left ADC (analog-to-digital converter) conversion factor.
	 */
	public double getLeftADCConversionFactor() {
		return a_left;
	}
	
	/**
	 * Returns the right ADC (analog-to-digital converter) conversion factor.
	 * 
	 * @return a_right The right ADC (analog-to-digital converter) conversion factor.
	 */
	public double getRightADCConversionFactor() {
		return a_right;
	}
	
	/**
	 * Returns the attenuation length.
	 * 
	 * @return lambda The attenuation length.
	 */
	public double getAttenuationLength() {
		return lambda;
	}
	
	/**
	 * Returns the left shift.
	 * 
	 * @return delta_L The left shift.
	 */
	public double getLeftShift() {
		return delta_L;
	}
	
	/**
	 * Returns the right shift.
	 * 
	 * @return delta_R The right shift.
	 */
	public double getRightShift() {
		return delta_R;
	}
	
	/**
	 * Returns the left TDC (time-to-digital converter) conversion factor.
	 * 
	 * @return t_left The left TDC (time-to-digital converter) conversion factor.
	 */
	public double getLeftTDCConversionFactor() {
		return t_left;
	}
	
	/**
	 * Returns the right TDC (time-to-digital converter) conversion factor.
	 * 
	 * @return t_right The right TDC (time-to-digital converter) conversion factor.
	 */
	public double getRightTDCConversionFactor() {
		return t_right;
	}
	
	/**
	 * Returns the item (scintillator bar or veto) length.
	 * 
	 * @return l The item (scintillator bar or veto) length.
	 */
	public double getItemLength() {
		return l;
	}
	
}