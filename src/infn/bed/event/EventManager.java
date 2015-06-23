package infn.bed.event;

import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.StructureHandler;
import infn.bed.event.FullWaveformData;
import infn.bed.frame.Bed;

import org.jlab.coda.jevio.BaseStructure;
import org.jlab.coda.jevio.BaseStructureHeader;
import org.jlab.coda.jevio.IEvioListener;
import org.jlab.coda.jevio.IEvioStructure;

/**
 * This is the manager for BDX specific events. This is where we load in the
 * data and store them in the data classes. This class handles passing the
 * waveshape data to the charge-time class and the waveshape data set to the
 * plots in Bed.java.
 * 
 * @author heddle, Andy Beiter
 * 
 */
public class EventManager implements IEvioListener {

	/**
	 * The instance of this class. There can only be one.
	 */
	private static EventManager instance;

	/**
	 * Structure handler that separates evio structures in banks
	 */
	private StructureHandler _structureHandler = new StructureHandler(1543);

	/**
	 * The instance to read in full waveform data
	 */
	private FullWaveformData fullWaveformData;

	/**
	 * The instance to read in charge-time data or convert to charge-time data
	 */
	private ChargeTimeData ctData;

	/**
	 * Private constructor for singleton EventManager. This with getInstance()
	 * prevents multiple instances.
	 */
	private EventManager() {
		// listen for events from jevio
		EventControl.getEvioParser().addEvioListener(this);
	}

	/**
	 * Public access to the event manager singleton.
	 * 
	 * @return the event control singleton.
	 */
	public static EventManager getInstance() {
		if (instance == null) {
			instance = new EventManager();
		}
		return instance;
	}

	/**
	 * Got a structure from the event source. This is where we look for
	 * structures of interest and put them in conveniently accessible arrays.
	 * 
	 * @param baseStructure
	 *            the base structure being passed.
	 * @param structure
	 *            structure received.
	 */
	@Override
	public void gotStructure(BaseStructure baseStructure,
			IEvioStructure structure) {

		_structureHandler.addStructure(structure);

		// grab the structures I'm interested in
		BaseStructureHeader header = structure.getHeader();
		int tag = header.getTag();
		int num = header.getNumber();
		if (tag == 102 || tag == 202) {
			if (ctData == null) {
				ctData = new ChargeTimeData();
			}
			ctData.load(structure, tag, num);
		}

		if (tag == 57601) {
			if (fullWaveformData == null) {
				fullWaveformData = new FullWaveformData();
			}
			fullWaveformData.load(structure, tag, num);
			ctData = new ChargeTimeData(fullWaveformData.getChannelSamples());
			Bed.getInstance().fillPlots(fullWaveformData.getDataSet());
		}
	}

	/**
	 * A new event is starting to be parsed by jevio.
	 * 
	 * @param baseStructure
	 *            the base structure being passed.
	 */
	@Override
	public void startEventParse(BaseStructure baseStructure) {
		clear(); // clear event data
	}

	/**
	 * The end of an event parsing has occurred.
	 * 
	 * @param baseStructure
	 *            the base structure being passed.
	 */
	@Override
	public void endEventParse(BaseStructure baseStructure) {
	}

	/**
	 * Clear all data from arrays and hashtables
	 */
	private void clear() {
		_structureHandler.clear();

		// nullify data pointers
		fullWaveformData = null;
		ctData = null;
	}

	/**
	 * Get the full waveform data
	 * 
	 * @return The full waveform data instance
	 */
	public FullWaveformData getFullWaveformData() {
		return fullWaveformData;
	}

	/**
	 * Get the charge-time data
	 * 
	 * @return The charge-time data instance
	 */
	public ChargeTimeData getChargeTimeData() {
		return ctData;
	}

}
