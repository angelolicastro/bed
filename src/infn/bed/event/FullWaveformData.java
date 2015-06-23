package infn.bed.event;

import infn.bed.view.plot.WavePlot;

import java.util.ArrayList;
import java.util.Vector;

import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.IEvioStructure;

import cnuphys.lund.LundId;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;

/**
 * This class handles the reading in of a full waveshape data file. The
 * waveshape is converted in ChargeTimeData.java and is plotted in WavePlot.java
 * 
 * @author Andy Beiter
 * 
 */
public class FullWaveformData implements ILoad {

	/**
	 * The waveshape from the PMTs
	 */
	private ArrayList<ArrayList<Short>> channelSamples;

	/**
	 * The data set for the plots
	 */
	private DataSet ds[];

	/**
	 * Constructor that instantiates the ArrayList and the data set
	 */
	public FullWaveformData() {
		channelSamples = new ArrayList<ArrayList<Short>>();
		ds = new DataSet[34]; // TODO data sets for vetoes too?
		for (int i = 0; i < 34; i++) {
			channelSamples.add(new ArrayList<Short>());
		}
		for (int i = 0; i < 34; i++) {
			try {
				ds[i] = new DataSet(DataSetType.XYXY, WavePlot.getColumnNames());
			} catch (DataSetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads in the full waveshape data from a full waveshape file. Currently
	 * just checks that the composite data is not null and does not use tag or
	 * num.
	 * 
	 * @param structure
	 *            The data
	 * @param tag
	 *            The tag used to identify the data
	 * @param num
	 *            The number used to identify the data
	 */
	@Override
	public void load(IEvioStructure structure, int tag, int num) {
		// TODO Auto-generated method stub
		try {
			for (int i = 0; i < channelSamples.size(); i++) {
				channelSamples.get(i).clear();
			}
			CompositeData[] comps = structure.getCompositeData();
			if (comps != null) {
				for (CompositeData cd : comps) {
					byte boardNum = cd.getByte();
					cd.getInt(); // event Num
					cd.getLong(); // trigger time
					int numChannels = cd.getNValue(); // number of channels the
														// event has data for
					for (int i = 0; i < numChannels; i++) {
						byte channelNum = cd.getByte(); // number of the channel
														// the next data set is
														// for
						int numSamples = cd.getNValue(); // number of samples
															// recorded in the
															// channel
						for (int j = 0; j < numSamples; j++) {
							short sample = cd.getShort();
							channelSamples.get(channelNum).add(sample);
							ds[channelNum].add((j + 1) * 4, sample);
						}
					}
					boardNum = cd.getByte();
					cd.getInt();
					cd.getLong();
					numChannels = cd.getNValue(); // number of channels the
													// event has data for
					for (int i = 0; i < numChannels; i++) {
						byte channelNum = cd.getByte(); // number of the channel
														// the next data set is
														// for
						int numSamples = cd.getNValue(); // number of samples
															// recorded in the
															// channel
						for (int j = 0; j < numSamples; j++) {
							short sample = cd.getShort();
							channelSamples
									.get((boardNum - 7) * 16 + channelNum).add(
											sample);
							ds[(boardNum - 7) * 16 + channelNum].add(
									(j + 1) * 4, sample);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the array list of array lists of channel samples
	 * 
	 * @return The samples for each bar/veto
	 */
	public ArrayList<ArrayList<Short>> getChannelSamples() {
		return channelSamples;
	}

	/**
	 * Gets the data sets for the plots
	 * 
	 * @return An array of data sets for plots
	 */
	public DataSet[] getDataSet() {
		return ds;
	}

	/**
	 * Currently unused.
	 * 
	 * @see infn.bed.event.ILoad#uniqueLundIds()
	 */
	@Override
	public Vector<LundId> uniqueLundIds() {
		// TODO Auto-generated method stub
		return null;
	}

}
