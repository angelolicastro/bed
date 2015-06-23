package infn.bed.view.plot;

import java.awt.Color;
import java.util.Collection;

import cnuphys.bCNU.view.PlotView;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.plot.HorizontalLine;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.VerticalLine;
import cnuphys.splot.style.SymbolType;

/**
 * This class is used to plot waveforms (TDC vs. ADC).
 * 
 * @author Andy Beiter
 */
@SuppressWarnings("serial")
public class WavePlot extends PlotView {

	/**
	 * Constructor.
	 */
	public WavePlot() {
		super();
	}

	/**
	 * Gets the column names for the data.
	 * 
	 * @return An array of the column names.
	 */
	public static String[] getColumnNames() {
		String names[] = { "X", "Y" };
		return names;
	}

	/**
	 * Returns the y-axis label
	 * 
	 * @return "ADC Data" - the label for the y-axis
	 */
	protected String getYAxisLabel() {
		return "ADC Data";
	}

	/**
	 * Returns the x-axis label
	 * 
	 * @return "TDC Data" - the label for the x-axis
	 */
	protected String getXAxisLabel() {
		return "TDC Data";
	}

	/**
	 * Sets up the plot info (axis labels, plot title, plot point
	 * characteristics) and correctly chooses the plot title using the isLeft
	 * parameter.
	 * 
	 * @param isLeft
	 *            True if the left PMT samples, false if right PMT samples
	 */
	private void setPreferences(boolean isLeft) {
		Color fillColor = new Color(255, 0, 0, 96);
		DataSet ds = _plotCanvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);

		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.CONNECT);
			dc.getStyle().setSymbolType(SymbolType.CIRCLE);
			dc.getStyle().setSymbolSize(4);
			dc.getStyle().setFillColor(fillColor);
			dc.getStyle().setLineColor(Color.black);
		}
		_plotCanvas.setName("hi");
		// many options controlled via plot parameters
		PlotParameters params = _plotCanvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);
		params.addPlotLine(new HorizontalLine(_plotCanvas, 0));
		params.addPlotLine(new VerticalLine(_plotCanvas, 0));
		params.setXLabel(getXAxisLabel());
		params.setYLabel(getYAxisLabel());
		if (isLeft) {
			params.setPlotTitle("ADC Left");
		} else {
			params.setPlotTitle("ADC Right");
		}
	}

	/**
	 * Sets the data for the plot and sets up the preferences.
	 * 
	 * @param ds
	 *            The data set to be used for the plot
	 * @param isLeft
	 *            True if the left PMT samples, false if right PMT samples
	 */
	public void addData(DataSet ds, boolean isLeft) {
		this._plotCanvas.setDataSet(ds);
		setPreferences(isLeft);
	}

}
