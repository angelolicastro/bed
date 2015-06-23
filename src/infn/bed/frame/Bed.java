package infn.bed.frame;

import infn.bed.view.BarFrontView;
import infn.bed.view.BarSideView;
import infn.bed.view.FullSideView;
import infn.bed.view.plot.WavePlot;
import infn.bed.event.AccumulationManager;
import infn.bed.item.FrontViewBar;
import infn.bed.item.FullSideViewBar;
import infn.bed.item.FullSideViewVeto;
import infn.bed.item.SideViewBar;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import cnuphys.bCNU.application.BaseMDIApplication;
import cnuphys.bCNU.application.Desktop;
import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.event.AccumulationDialog;
import cnuphys.bCNU.event.EventMenu;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.menu.MenuManager;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.view.EventView;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.bCNU.view.VirtualView;
import cnuphys.splot.pdata.DataSet;

/**
 * This class is the frame of the program. It holds and manages all of the
 * views, handles the toolbar at the top of the screen and the aesthetics of the
 * program.
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
@SuppressWarnings("serial")
public class Bed extends BaseMDIApplication implements PropertyChangeListener {

	/**
	 * The path where we'll look for data such as translation tables and
	 * calibration constants.
	 */
	public static String dataPath = "data";

	/**
	 * The instance of the VirtualView class
	 */
	private VirtualView virtualView;

	/**
	 * The instance of the EventView class
	 */
	private EventView eventView;

	/**
	 * The instance of the BarSideView class
	 */
	private BarSideView barSideView;

	/**
	 * The instance of the BarFrontView class
	 */
	private BarFrontView barFrontView;

	/**
	 * The instance of the FullSideView class
	 */
	private FullSideView fullSideView;

	/**
	 * An array of the plots of the left PMT values
	 */
	private WavePlot leftPlot[];

	/**
	 * An array of the plots of the right PMT values
	 */
	private WavePlot rightPlot[];

	/**
	 * The instance of this class being used
	 */
	private static Bed instance;

	/**
	 * If this is the start of the program
	 */
	private int firstTime = 0;

	/**
	 * String used in the about bed pop up
	 */
	private static String aboutString = "<html><span style=\"font-size:8px\">bed: the bDX eVENT dISPLAY<br><br>Developed by INFN-GE";

	/**
	 * Image used in the about bed pop up
	 */
	protected static ImageIcon _aboutIcon = ImageManager.getInstance()
			.loadImageIcon("images/infn.jpg");

	/**
	 * Constructor that adds the component listener.
	 * 
	 * @param keyVals
	 *            Attributes that are handles by the superclass.
	 */
	private Bed(Object... keyVals) {
		super(keyVals);

		ComponentListener cl = new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent ce) {
			}

			@Override
			public void componentMoved(ComponentEvent ce) {
			}

			@Override
			public void componentResized(ComponentEvent ce) {
				placeViewsOnVirtualDesktop();
			}

			@Override
			public void componentShown(ComponentEvent ce) {
				placeViewsOnVirtualDesktop();
			}

		};

		addComponentListener(cl);
	}

	/**
	 * Moves the views to their default positions in the frame.
	 */
	private void placeViewsOnVirtualDesktop() {
		if (firstTime == 1) {
			// reaarange some views in virtual space
			virtualView.reconfigure();
			virtualView.moveTo(fullSideView, 0, 0);
			virtualView.moveTo(eventView, 0, 1, true);
			virtualView.moveTo(barFrontView, 0, 2);
			virtualView.moveTo(barSideView, 0, 3);
			for (int i = 0; i < 9; i++) {
				virtualView.moveTo(leftPlot[i], 0, 3);
				virtualView.moveTo(rightPlot[i], 0, 3);
			}
			Log.getInstance().config("reset views on virtual dekstop");
		}
		firstTime++;
	}

	/**
	 * Creates the views and plots for the frame.
	 */
	private void addInitialViews() {

		// make sure accumulation manager is instantiated
		AccumulationManager.getInstance();

		// add a virtual view
		virtualView = VirtualView.createVirtualView();

		eventView = EventView.createEventView();

		barSideView = BarSideView.createBarSideView();

		barFrontView = BarFrontView.createBarFrontView();

		fullSideView = FullSideView.createFullSideView();

		leftPlot = new WavePlot[9];

		rightPlot = new WavePlot[9];
		
		for (int i = 0; i < 9; i++) {
			leftPlot[i] = new WavePlot();
			rightPlot[i] = new WavePlot();
		}
		clearViewMenu();
		// log some environment info
		Log.getInstance().config(Environment.getInstance().toString());

		// use config file info
		Desktop.getInstance().configureViews();

		virtualView.toFront();
	}

	/**
	 * Creates the menus
	 */
	private void createMenus() {
		MenuManager mmgr = MenuManager.getInstance();

		// ET menu
		mmgr.addMenu(ETSupport.getETMenu());

		// remove the option menu until I need it
		mmgr.removeMenu(mmgr.getOptionMenu());

		// add to the file menu
		JMenu fmenu = mmgr.getFileMenu();

		JMenuItem aboutItem = new JMenuItem("About BED...");
		ActionListener al0 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(Bed.getInstance(), aboutString,
						"About BED", JOptionPane.INFORMATION_MESSAGE,
						_aboutIcon);

			}
		};
		aboutItem.addActionListener(al0);

		fmenu.add(aboutItem, 0);

		// add to the event menu
		addToEventMenu();

	}

	/**
	 * Adds items to the event menu
	 */
	private void addToEventMenu() {
		MenuManager mmgr = MenuManager.getInstance();

		JMenu menu = mmgr.getMenu(EventView.EVENTMENU);

		menu.add(EventMenu.getRecentEvioFileMenu(), 1);
		
		JMenuItem calibrationItem = new JMenuItem("Open Calibration Constants File");
		ActionListener calibAL = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { //TODO
				JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			        "DAT files", "dat");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(Bed.getInstance());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File calibrationFile = chooser.getSelectedFile();
		            FullSideViewBar[] fsvBars = fullSideView.getBars();
		            for(int i = 0; i < fsvBars.length; i++) {
		            	fsvBars[i].getConstants(calibrationFile);
		            }
		            FullSideViewVeto[] fsvVetoes = fullSideView.getVetoes();
		            for(int i = 0; i < fsvBars.length; i++) {
		            	fsvVetoes[i].getConstants(calibrationFile);
		            }
		            FrontViewBar[] frontBars = barFrontView.getBars();
		            for(int i = 0; i < frontBars.length; i++) {
		            	frontBars[i].getConstants(calibrationFile);
		            }
		            SideViewBar[] sideBars = barSideView.getBars();
		            for(int i = 0; i < sideBars.length; i++) {
		            	sideBars[i].getConstants(calibrationFile);
		            }
		        } 
			}
		};
		calibrationItem.addActionListener(calibAL);
		menu.add(calibrationItem, 2);
		
		EventMenu.menuAdditions();

		// add the accumulation dialog item,
		ActionListener al1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccumulationDialog dialog = new AccumulationDialog(
						AccumulationManager.getInstance());
				dialog.setVisible(true);
			}
		};
		JMenuItem accItem = MenuManager.addMenuItem("Accumulate Events...",
				menu, al1);
		accItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		accItem.setEnabled(false);
		EventMenu.setAccumulationItem(accItem);

		// add the noise parameter menu item
		ActionListener al2 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
		MenuManager.addMenuItem("Noise Algorithm Parameters...", menu, al2);
		
	}

	/**
	 * Returns the instance of the frame
	 * 
	 * @return The instance of Bed
	 */
	public static Bed getInstance() {
		if (instance == null) {
			instance = new Bed(AttributeType.TITLE, "BDX Event Display (BED)",
					AttributeType.BACKGROUNDIMAGE, "",
					AttributeType.WINDOWMENU, false, AttributeType.FRACTION,
					0.85);

			instance.addInitialViews();
			instance.createMenus();
		}
		return instance;
	}

	/**
	 * Makes the selected bar's waveshape plots visible.
	 * 
	 * (Note: the order of the plots is different than the order of the bars in
	 * the views.)
	 * 
	 * @param bar
	 *            The bar that was clicked on
	 */
	public void setPlotsVisible(int bar) {
		switch (bar) {
		case 0:
			leftPlot[6].setVisible(true);
			rightPlot[6].setVisible(true);
			leftPlot[6].setTitle("Bar 1 Left");
			rightPlot[6].setTitle("Bar 1 Right");
			
			break;
		case 1:
			leftPlot[7].setVisible(true);
			rightPlot[7].setVisible(true);
			leftPlot[7].setTitle("Bar 2 Left");
			rightPlot[7].setTitle("Bar 2 Right");
			break;
		case 2:
			leftPlot[8].setVisible(true);
			rightPlot[8].setVisible(true);
			leftPlot[8].setTitle("Bar 3 Left");
			rightPlot[8].setTitle("Bar 3 Right");
			break;
		case 3:
			leftPlot[3].setVisible(true);
			rightPlot[3].setVisible(true);
			leftPlot[3].setTitle("Bar 4 Left");
			rightPlot[3].setTitle("Bar 4 Right");
			break;
		case 4:
			leftPlot[4].setVisible(true);
			rightPlot[4].setVisible(true);
			leftPlot[4].setTitle("Bar 5 Left");
			rightPlot[4].setTitle("Bar 5 Right");
			break;
		case 5:
			leftPlot[5].setVisible(true);
			rightPlot[5].setVisible(true);
			leftPlot[5].setTitle("Bar 6 Left");
			rightPlot[5].setTitle("Bar 6 Right");
			break;
		case 6:
			leftPlot[0].setVisible(true);
			rightPlot[0].setVisible(true);
			leftPlot[0].setTitle("Bar 7 Left");
			rightPlot[0].setTitle("Bar 7 Right");
			break;
		case 7:
			leftPlot[1].setVisible(true);
			rightPlot[1].setVisible(true);
			leftPlot[1].setTitle("Bar 8 Left");
			rightPlot[1].setTitle("Bar 8 Right");
			break;
		case 8:
			leftPlot[2].setVisible(true);
			rightPlot[2].setVisible(true);
			leftPlot[2].setTitle("Bar 9 Left");
			rightPlot[2].setTitle("Bar 9 Right");
			break;
		}

	}

	/**
	 * Empties the plots and passes in new data sets.
	 * 
	 * @param ds
	 */
	public void fillPlots(DataSet ds[]) {
		clearPlots();
		for (int i = 0; i < leftPlot.length; i++) {
			leftPlot[i].addData(ds[2 * i], true);
			rightPlot[i].addData(ds[2 * i + 1], false);
		}
	}

	/**
	 * Resets the plots for the next event.
	 */
	private void clearPlots() { //TODO
		for(int i = 0; i < leftPlot.length; i++) {
			leftPlot[i] = new WavePlot();
			rightPlot[i] = new WavePlot();
			virtualView.moveTo(leftPlot[i], 0, 3);
			virtualView.moveTo(rightPlot[i], 0, 3);
		}
		clearViewMenu();
	}
	
	private void clearViewMenu() {
		for (int i = 0; i < leftPlot.length; i++) {
			JMenu menu = ViewManager.getInstance().getViewMenu();
			for(int j = 0; j < menu.getItemCount(); j++) {
				JMenuItem item = menu.getItem(j);
				if(item != null) {
					if(item.getText().equals("sPlot")) {
						menu.remove(menu.getItem(j));
					}
				}
			}
		}
	}

	/**
	 * Starts the program.
	 * 
	 * @param args
	 *            Command-line arguments
	 */
	public static void main(String[] args) {
		final Bed frame = Bed.getInstance();

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				frame.setVisible(true);
			}

		});
	}

	/**
	 * Currently unused
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

}
