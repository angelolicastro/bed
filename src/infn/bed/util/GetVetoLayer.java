package infn.bed.util;

import infn.bed.geometry.GeometricConstants;

/**
 * Returns the layer of a veto.
 * 
 * @author Angelo Licastro
 */
public class GetVetoLayer {
	
	/**
	 * Returns the layer of a veto.
	 * 
	 * @param veto The number of a veto in one-based indexing.
	 * @return The layer of the veto.
	 * If the veto is a crystal, then 1 is returned.
	 * If the veto is an internal veto, then 2 is returned.
	 * If the veto is an external veto, then 3 is returned.
	 * If the veto does not exist, then -1 is returned.
	 */
	public int getVetoLayer(int veto) {
		if (veto > 0 && veto <= GeometricConstants.VETOES) {
			if (veto <= GeometricConstants.CRYSTALS) {
				return 1;
			} else if (veto <= GeometricConstants.INTERNAL_VETOES) {
				return 2;
			} else if (veto <= GeometricConstants.VETOES) {
				return 3;
			}
		}
		return -1;
	}
	
}