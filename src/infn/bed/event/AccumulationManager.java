package infn.bed.event;

import cnuphys.bCNU.event.BaseAccumulationManager;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.IPhysicsEventListener;
import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.util.Histo2DData;
import infn.bed.geometry.GeometricConstants;
import org.jlab.coda.jevio.EvioEvent;

/**
 * Manages the accumulation of data
 * 
 * @author heddle
 * 
 */
public class AccumulationManager extends BaseAccumulationManager implements
		IPhysicsEventListener {

	// the singleton
	private static AccumulationManager instance;

	// dc accumulated data indices are sector, superlayer, layer, wire
	private int _dcGemcAccumulatedData[];
	private int _maxGemcDcCount;
	
	//dc XY accumulated data stored in a 2D histogram
	private Histo2DData _dcXYGemcAccumulatedData;
	
	//bst hit xy accumulated data
	private Histo2DData _bstXYAccumulatedData;
	
	/**
	 * private constructor for singleton.
	 */
	private AccumulationManager() {
		EventControl.getInstance().addPhysicsListener(this);
		_dcGemcAccumulatedData = new int[GeometricConstants.BARS];

		//dc XY accumulated data stored in a 2D histogram
		_dcXYGemcAccumulatedData = new Histo2DData("DC XY Data",
				-390, 390, 100, -450, 450, 100);
		
		//BST XY accumulated data also stored in histo
		_bstXYAccumulatedData = new Histo2DData("BST XY Data",
				-170, 170, 100, -170, 170, 100);
		
		clear();
	}

	/**
	 * Clears all accumulated data.
	 */
	@Override
	public void clear() {
		//clear accumulated gemc dc data
		for (int sector = 0; sector < GeometricConstants.BARS; sector++) {
			_dcGemcAccumulatedData[sector] = 0;
		}
		_maxGemcDcCount = 0;
		
		//clear other stuff
		_dcXYGemcAccumulatedData.clear();
		_bstXYAccumulatedData.clear();
	}

	/**
	 * Public access to the singleton.
	 * 
	 * @return the singleton AccumulationManager
	 */
	public static AccumulationManager getInstance() {
		if (instance == null) {
			instance = new AccumulationManager();
		}
		return instance;
	}

	@Override
	public void newPhysicsEvent(EvioEvent event) {
		
		// only care if I am accumulating
		if (EventControl.getInstance().isAccumulating()) {
		}
	}

	/**
	 * Get the accumulated Gemc DC data
	 * @return the accumulated dc data
	 */
	public int[] getAccumulatedGemcDcData() {
		return _dcGemcAccumulatedData;
	}

	/**
	 * @return the max counts for any DC wire.
	 */
	public int getMaxGemcDcCount() {
		return _maxGemcDcCount;
	}

	/**
	 * @return the colorScaleModel
	 */
	public static ColorScaleModel getColorScaleModel() {
		return colorScaleModel;
	}
	
	/**
	 * Get the Gemc DC xy accumulated data
	 * @return the Gemc DC xy accumulated data
	 */
	public Histo2DData getDcXYGemcAccumulatedData() {
		return _dcXYGemcAccumulatedData;
	}
	
	/**
	 * Get the BST XY ccumulated data
	 * @return the BST XY accumulated data
	 */
	public Histo2DData getBSTXYGemcAccumulatedData() {
		return _bstXYAccumulatedData;
	}

}
