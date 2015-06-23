package infn.bed.event;

import java.util.Vector;

import org.jlab.coda.jevio.IEvioStructure;

import cnuphys.lund.LundId;

/**
 * Interface used for data classes
 * 
 * @author heddle
 *
 */
public interface ILoad {

	/**
	 * Load data from an evio structure
	 * @param structure the structure
	 * @param tag the tag
	 * @param num the num
	 */
	public void load(IEvioStructure structure, int tag, int num);
	
	/**
	 * Get a unique set of lund Ids in this bank
	 * @return a unique set of lund Ids
	 */
	public Vector<LundId> uniqueLundIds();
}
