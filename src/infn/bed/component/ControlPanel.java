package infn.bed.component;

import infn.bed.view.BedView;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.event.BaseAccumulationManager;
import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.graphics.colorscale.ColorModelLegend;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;

/**
 * This is the control panel that sits on the side of the view
 * 
 * @author David Heddle
 */
@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	private static Font smallFont = Fonts.commonFont(Font.PLAIN, 9);

	private static final int SLIDERWIDTH = 210;
	private static final int FEEDBACKWIDTH = 220;

	// widths of some optional widgets
	//private static final int FULLWIDTH = 220;

	/**
	 * Bit used to create a display array
	 */
	public static final int DISPLAYARRAY = 01;

	/**
	 * Bit used to create a phi slider
	 */
	public static final int PHISLIDER = 02;

	/**
	 * Bit used to create a torus legend
	 */
	public static final int FIELDLEGEND = 04;

	/**
	 * Bit used to create a Lund particle legend
	 */
	public static final int LUNDLEGEND = 010;

	/**
	 * Bit used to create a target slider
	 */
	public static final int TARGETSLIDER = 020;

	/**
	 * Bit used to create a feedback pane
	 */
	public static final int FEEDBACK = 040;

	/**
	 * Bit used to create an accumulation legend
	 */
	public static final int ACCUMULATIONLEGEND = 0100;

	/**
	 * Bit used to create an accumulation legend
	 */
	public static final int NOISECONTROL = 0200;
	
	/**
	 * Bit used to make phi slider have full 360 degree range
	 */
	public static final int PHI_SLIDER_BIG = 0400;
	
	/**
	 * Bit to display reconstructed display array
	 */
	public static final int RECONSARRAY = 01000;

	// the view parent
	private BedView _view;

	// control the nominal target z
	private JSlider _targetSlider;

	// control the value of phi
	private JSlider _phiSlider;

	// the feedback pane
	private FeedbackPane _feedbackPane;

	/**
	 * Create a view control panel
	 * 
	 * @param view the parent view
	 * @param controlPanelBits the bits fo which components are added
	 * @param displayArrayBits the bits for which display flags are added to the
	 *        display array.
	 */
	public ControlPanel(BedView view, int controlPanelBits, int displayArrayBits) {
		_view = view;
		
		Box box = Box.createVerticalBox();
		box.add(addTabbedPane(view, controlPanelBits, displayArrayBits));

		// feedback
		if (Bits.checkBit(controlPanelBits, FEEDBACK)) {
			_feedbackPane = new FeedbackPane(FEEDBACKWIDTH);
			view.getContainer().setFeedbackPane(_feedbackPane);
			box.add(_feedbackPane);
		}

		add(box);
	}
	
	//use a tabbed pane to save space
	private JTabbedPane addTabbedPane(BedView view, int controlPanelBits, int displayArrayBits) {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(smallFont);

		//every thing else on a "general" panel
		JPanel sp = new JPanel();
		Box box = Box.createVerticalBox();

		// target phi slider
		if (Bits.checkBit(controlPanelBits, PHISLIDER)) {
			boolean isBig = Bits.checkBit(controlPanelBits, PHI_SLIDER_BIG);
			box.add(createPhiSlider(isBig));
		}

		// lund Ids
		if (Bits.checkBit(controlPanelBits, LUNDLEGEND)) {
			JPanel panel = WorldGraphicsUtilities.getLundColorLegend();
			panel.setBorder(new CommonBorder("Particles (neutrals dashed)"));
			box.add(panel);
		}

		// accumulation
		if (Bits.checkBit(controlPanelBits, ACCUMULATIONLEGEND)) {
			box.add(new ColorModelLegend(
					BaseAccumulationManager.colorScaleModel, 160,
					"Relative Accumulation"));
		}

		// target z slider
		if (Bits.checkBit(controlPanelBits, TARGETSLIDER)) {
			box.add(createTargetSlider());
		}
		
		sp.add(box);
		
		tabbedPane.add(sp, "basic");
		
		return tabbedPane;
	}

	/**
	 * Create the slider used to control the target z
	 * 
	 * @return the slider used to control the target z
	 */
	private Box createTargetSlider() {
		Box box = Box.createVerticalBox();

		int targ_min = -200;
		int targ_max = 200;
		int targ_init = 0;

		_targetSlider = new JSlider(SwingConstants.HORIZONTAL, targ_min,
				targ_max, targ_init);

		_targetSlider.setMajorTickSpacing(100);
		_targetSlider.setMinorTickSpacing(10);
		_targetSlider.setPaintTicks(true);
		_targetSlider.setPaintLabels(true);
		_targetSlider.setFont(smallFont);
		_targetSlider.setFocusable(false); // so ugly focus border not drawn

		if (_view instanceof ChangeListener) {
			_targetSlider.addChangeListener((ChangeListener) _view);
		}

		Dimension d = _targetSlider.getPreferredSize();
		d.width = SLIDERWIDTH;
		_targetSlider.setPreferredSize(d);
		box.add(_targetSlider);

		box.setBorder(new CommonBorder("Target Z (cm)"));
		return box;
	}

	/**
	 * Create the slider used to control the target z
	 * 
	 * @return the slider used to control the target z
	 */
	private Box createPhiSlider(boolean isBig) {
		Box box = Box.createVerticalBox();

		int phi_min = -30;
		int phi_max = 30;
		int phi_init = 0;
		if(isBig){
			phi_min = -180;
			phi_max = 180;
		}

		_phiSlider = new JSlider(SwingConstants.HORIZONTAL, phi_min, phi_max,
				phi_init);
		if(!isBig){
			_phiSlider.setMajorTickSpacing(10);
			_phiSlider.setMinorTickSpacing(1);
		} else{
			_phiSlider.setMajorTickSpacing(60);
			_phiSlider.setMinorTickSpacing(10);
		}
		_phiSlider.setPaintTicks(true);
		_phiSlider.setPaintLabels(true);
		_phiSlider.setFont(smallFont);
		_phiSlider.setFocusable(false); // so ugly focus border not drawn

		if (_view instanceof ChangeListener) {
			_phiSlider.addChangeListener((ChangeListener) _view);
		}

		Dimension d = _phiSlider.getPreferredSize();
		d.width = SLIDERWIDTH;
		_phiSlider.setPreferredSize(d);
		box.add(_phiSlider);

		box.setBorder(new CommonBorder(UnicodeSupport.CAPITAL_DELTA
				+ UnicodeSupport.SMALL_PHI + " relative to midPlane (deg)"));
		return box;
	}

	/**
	 * Get the slider for target position.
	 * 
	 * @return the slider for target position.
	 */
	public JSlider getTargetSlider() {
		return _targetSlider;
	}

	/**
	 * Get the slider for the relative phi.
	 * 
	 * @return the slider for the relative phi.
	 */
	public JSlider getPhiSlider() {
		return _phiSlider;
	}

}
