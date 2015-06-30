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
 * Plots ADC (analog-to-digital converter) and TDC (time-to-digital converter) waveforms.
 * 
 * @author Andy Beiter
 * @author Angelo Licastro
 */
@SuppressWarnings("serial")
public class WavePlot extends PlotView {

	/**
	 * The constructor.
	 */
	public WavePlot() {
		super();
	}

	/**
	 * Returns the column names for the plot.
	 * 
	 * @return names An array of column names for the plot.
	 */
	public static String[] getColumnNames() {
		return new String[]{"X", "Y"};
	}

	/**
	 * Returns the y-axis label for the plot.
	 * 
	 * @return The y-axis label for the plot.
	 */
	private String getYAxisLabel() {
		return "ADC";
	}

	/**
	 * Returns the x-axis label for the plot.
	 * 
	 * @return The x-axis label for the plot.
	 */
	private String getXAxisLabel() {
		return "TDC";
	}

	/**
	 * Sets the preferences for the plot.
	 * 
	 * @param isLeft true if the left PMT (photomultiplier tube) is sampling, false otherwise.
	 */
	private void setPreferences(boolean isLeft) {
		Color fillColor = new Color(255, 0, 0, 96);
		DataSet dataSet = _plotCanvas.getDataSet();
		Collection<DataColumn> yDataColumns = dataSet.getAllColumnsByType(DataColumnType.Y);
		for (DataColumn dataColumn : yDataColumns) {
			dataColumn.getFit().setFitType(FitType.CONNECT);
			dataColumn.getStyle().setSymbolType(SymbolType.CIRCLE);
			dataColumn.getStyle().setSymbolSize(4);
			dataColumn.getStyle().setFillColor(fillColor);
			dataColumn.getStyle().setLineColor(Color.black);
		}
		PlotParameters plotParameters = _plotCanvas.getParameters();
		plotParameters.mustIncludeXZero(true);
		plotParameters.mustIncludeYZero(true);
		plotParameters.addPlotLine(new HorizontalLine(_plotCanvas, 0));
		plotParameters.addPlotLine(new VerticalLine(_plotCanvas, 0));
		plotParameters.setXLabel(getXAxisLabel());
		plotParameters.setYLabel(getYAxisLabel());
		if (isLeft) {
			plotParameters.setPlotTitle("ADC Left");
		} else {
			plotParameters.setPlotTitle("ADC Right");
		}
	}

	/**
	 * Sets the data set for the plot and calls setPreferences().
	 * 
	 * @param dataSet The data set for the plot.
	 * @param isLeft true if the left PMT (photomultiplier tube) is sampling, false otherwise.
	 */
	public void addData(DataSet dataSet, boolean isLeft) {
		this._plotCanvas.setDataSet(dataSet);
		setPreferences(isLeft);
	}

}