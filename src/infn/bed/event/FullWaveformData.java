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
 * Reads a full-waveform data file.
 * 
 * @author Andy Beiter, Angelo Licastro
 */
public class FullWaveformData implements ILoad {

	/**
	 * An ArrayList of PMT (photomultiplier tube) full-waveform data.
	 */
	private final ArrayList<ArrayList<Short>> channelSampleArrayList;

	/**
	 * An array of plot data sets.
	 */
	private final DataSet[] dataSetArray;

	/**
	 * Prepares the full-waveform data.
	 */
	public FullWaveformData() {
		channelSampleArrayList = new ArrayList<>();
		dataSetArray = new DataSet[34];
		for (int i = 0; i < 34; i++) {
			channelSampleArrayList.add(new ArrayList<>());
		}
		for (int i = 0; i < 34; i++) {
			try {
				dataSetArray[i] = new DataSet(DataSetType.XYXY, WavePlot.getColumnNames());
			} catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads full-waveform data from a full-waveform data file.
	 * 
	 * @param structure An instance of the IEvioStructure object.
	 * @param tag The tag of the bank.
	 * @param num The num of the bank.
	 */
	@Override
	public void load(IEvioStructure structure, int tag, int num) {
		try {
			channelSampleArrayList.forEach(ArrayList<Short>::clear);
			CompositeData[] compositeDataArray = structure.getCompositeData();
			if (compositeDataArray != null) {
				for (CompositeData compositeData : compositeDataArray) {
					int channelCount = compositeData.getNValue();
					for (int i = 0; i < channelCount; i++) {
						byte channelNumber = compositeData.getByte();
						int sampleCount = compositeData.getNValue();
						for (int j = 0; j < sampleCount; j++) {
							short sample = compositeData.getShort();
							channelSampleArrayList.get(channelNumber).add(sample);
							dataSetArray[channelNumber].add((j + 1) * 4, sample);
						}
					}
					byte boardNumber = compositeData.getByte();
					compositeData.getInt();
					compositeData.getLong();
					channelCount = compositeData.getNValue();
					for (int i = 0; i < channelCount; i++) {
						byte channelNum = compositeData.getByte();
						int numSamples = compositeData.getNValue();
						for (int j = 0; j < numSamples; j++) {
							short sample = compositeData.getShort();
							channelSampleArrayList.get((boardNumber - 7) * 16 + channelNum).add(sample);
							dataSetArray[(boardNumber - 7) * 16 + channelNum].add((j + 1) * 4, sample);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns an ArrayList of PMT (photomultiplier tube) full-waveform data.
	 * 
	 * @return An ArrayList of PMT (photomultiplier tube) full-waveform data.
	 */
	public ArrayList<ArrayList<Short>> getChannelSampleArrayList() {
		return channelSampleArrayList;
	}

	/**
	 * Returns an array of plot data sets.
	 * 
	 * @return An array of plot data sets.
	 */
	public DataSet[] getDataSetArray() {
		return dataSetArray;
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

}
