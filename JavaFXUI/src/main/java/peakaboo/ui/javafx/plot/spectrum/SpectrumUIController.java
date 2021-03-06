package peakaboo.ui.javafx.plot.spectrum;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import peakaboo.controller.plotter.PlotController;
import peakaboo.controller.plotter.data.DataController;
import peakaboo.controller.plotter.fitting.FittingController;
import peakaboo.controller.plotter.view.ViewController;
import peakaboo.curvefit.curve.fitting.FittingResult;
import peakaboo.curvefit.curve.fitting.FittingResultSet;
import peakaboo.ui.javafx.change.ChangeController;
import peakaboo.ui.javafx.plot.filter.FilterChange;
import peakaboo.ui.javafx.plot.fitting.FittingChange;
import peakaboo.ui.javafx.plot.spectrum.log.LogarithmicAxis;
import peakaboo.ui.javafx.plot.window.DataLoadedChange;
import peakaboo.ui.javafx.plot.window.changes.DisplayOptionsChange;
import peakaboo.ui.javafx.plot.window.changes.EnergyLevelChange;
import peakaboo.ui.javafx.plot.zoom.ZoomChange;
import peakaboo.ui.javafx.util.FXUtil;
import peakaboo.ui.javafx.util.IActofUIController;
import peakaboo.ui.javafx.util.Spectrums;
import scidraw.drawing.DrawingRequest;
import scitypes.Pair;
import scitypes.ReadOnlySpectrum;
import scitypes.SigDigits;


public class SpectrumUIController extends IActofUIController {

	DataController data;
	PlotController plotController;

	private final String TANGO_DARK_GREEN = "#4e9a06";
	private final String TANGO_DARK_PURPLE = "#5c3566";
	private final String TANGO_BLACK = "#000000";
	private final String TANGO_DARK_RED = "#a40000";
	private final String TANGO_GREY_1 = "#eeeeec";
	private final String TANGO_GREY_2 = "#d3d7cf";
	private final String TANGO_GREY_3 = "#babdb6";
	private final String TANGO_GREY_4 = "#888a85";
	private final String TANGO_GREY_5 = "#555753";
	private final String TANGO_GREY_6 = "#2e3436";


	@FXML
	private ScrollPane scrollpane;

	@FXML
	private AreaChart<Number, Number> spectrum;
	Series<Number, Number> seriesFinalData, seriesRawData, seriesFittings;
	List<Series<Number, Number>> seriesIndividualFittings = new ArrayList<>();
	

	private double scale;

	@Override
	public void ready() {
		// TODO Auto-generated method stub
		getChangeBus().listen(DataLoadedChange.class, this::onNewData);

		scrollpane.viewportBoundsProperty().addListener((obs, o, n) -> {
			resize();
		});

		getChangeBus().listen(ZoomChange.class, "plot", zoom -> {
			scale = (zoom.getValue() * 5) + 1;
			resize();
		});

		getChangeBus().listen(DisplayOptionsChange.class, this::replot);
		getChangeBus().listen(FilterChange.class, this::update);
		getChangeBus().listen(FittingChange.class, this::update);
		getChangeBus().listen(EnergyLevelChange.class, this::update);
		

	}

	public void setPlotController(PlotController plotController) {
		this.plotController = plotController;
		replot();
	}

	//used when the chart itself needs to be changed (eg axes)
	private void replot() {

		/*
		 * NAS 2018-03-13: The drawing request used to be fetched from the plotController, 
		 * but this wasn't happening in the swing version of the UI. The only thing it 
		 * seemed to be used for was storing the unitSize, which was actually storing
		 * information about energy per channel. This method of measuring energy was
		 * replaced with a (min,max,width) tuple, and there was no need to keep the dr
		 * in the controller
		 */
		DrawingRequest dr = new DrawingRequest();
		ViewController settings = plotController.view();

		int xmax = 2048;
		if (data != null) {
			xmax = data.getDataSet().getAnalysis().channelsPerScan();
		}
		NumberAxis xAxis = new NumberAxis(0, xmax - 1, 50);
		spectrum = new AreaChart<Number, Number>(xAxis, getYAxis());
		spectrum.setLegendVisible(false);

		if (!settings.getShowAxes()) {
			spectrum.getXAxis().setTickLabelsVisible(false);
			spectrum.getXAxis().setOpacity(0);
			spectrum.getYAxis().setTickLabelsVisible(false);
			spectrum.getYAxis().setOpacity(0);
		}

		if (settings.getShowTitle()) {
			spectrum.setTitle(data == null ? "Title" : data.getDataSet().getScanData().datasetName());
		}

		spectrum.setStyle("-fx-background-color: #ffffff;");
		spectrum.lookup(".chart-plot-background").setStyle("-fx-background-color: #ffffff;");


		scrollpane.setContent(spectrum);

		if (data == null) { return; }




		int seriesnum = 0;
		
		String colour;
		boolean monochrome = plotController.view().getMonochrome();

		Pair<ReadOnlySpectrum, ReadOnlySpectrum> plotdata = plotController.getDataForPlot();

		// main data series
		seriesFinalData = Spectrums.asSeries(plotdata.first);
		seriesFinalData.setName("Processed Data");
		spectrum.getData().add(seriesFinalData);
		colour = monochrome ? TANGO_GREY_6 : TANGO_DARK_GREEN;
		setSeriesStyle(seriesnum++, colour);

		// fittings
		FittingController fittings = plotController.fitting();
		FittingResultSet fitted = fittings.getFittingSelectionResults();
		colour = monochrome ? TANGO_GREY_4 : TANGO_BLACK;
		if (settings.getShowIndividualSelections()) {
			seriesIndividualFittings.clear();
			for (FittingResult result : fitted.getFits()) {
				Series<Number, Number> series;
				series = Spectrums.asSeries(result.getFit());
				series.setName("Fittings");
				spectrum.getData().add(series);
				seriesIndividualFittings.add(series);
				setSeriesStyle(seriesnum++, colour + "7f", colour);
			}
		} else {
			System.out.println("ASDF");
			System.out.println(fitted.getTotalFit().get(250));
			seriesFittings = Spectrums.asSeries(fitted.getTotalFit());
			seriesFittings.setName("Fittings");
			spectrum.getData().add(seriesFittings);
			setSeriesStyle(seriesnum++, colour + "7f", colour);
		}


		// raw data
		seriesRawData = Spectrums.asSeries(plotdata.second);
		if (settings.getShowRawData()) {
			seriesRawData.setName("Raw Data");
			spectrum.getData().add(seriesRawData);
			colour = monochrome ? TANGO_GREY_2 : TANGO_DARK_PURPLE;
			setSeriesStyle(seriesnum++, colour + "00", colour + "7f");
		}



		resize();


	}

	// Used when the chart itself doesn't need to be changed, just the data in it
	private void update() {
		
		if (!plotController.data().hasDataSet()) { return; }
		
		//final and raw data
		Pair<ReadOnlySpectrum, ReadOnlySpectrum> plotdata = plotController.getDataForPlot();
				
		updateSeriesFromSpectrum(seriesFinalData, plotdata.first);
		updateSeriesFromSpectrum(seriesRawData, plotdata.second);

		//fittings
		FittingController fittings = plotController.fitting();
		FittingResultSet fitted = fittings.getFittingSelectionResults();
		if (plotController.view().getShowIndividualSelections()) {
			int count = 0;
			for (FittingResult result : fitted.getFits()) {
				updateSeriesFromSpectrum(seriesIndividualFittings.get(count++), result.getFit());
			}
		} else {
			updateSeriesFromSpectrum(seriesFittings, fitted.getTotalFit());
		}
		
	}
	
	private void updateSeriesFromSpectrum(Series<Number, Number> series, ReadOnlySpectrum spectrum) {
		for (Data<Number, Number> data : series.getData()) {
			data.setYValue(spectrum.get(data.getXValue().intValue()));
		}
	}
	
	
	private void setSeriesStyle(int seriesnum, String fillColour) {
		setSeriesStyle(seriesnum, fillColour, fillColour);
	}

	private void setSeriesStyle(int seriesnum, String fillColour, String strokeColour) {
		spectrum.lookup(".default-color" + seriesnum + ".chart-series-area-line").setStyle(
				"-fx-stroke: " + strokeColour + ";");
		spectrum.lookup(".default-color" + seriesnum + ".chart-series-area-fill").setStyle(
				"-fx-fill: " + fillColour + ";");
		for (Node symbol : spectrum.lookupAll(".chart-area-symbol")) {
			symbol.setVisible(false);
			symbol.setManaged(false);
		}
	}



	private void resize() {
		spectrum.setPrefWidth(scrollpane.getWidth() * scale);
		Node child = scrollpane.getContent();
		scrollpane.setFitToWidth(child.prefWidth(-1) < scrollpane.getWidth());
	}

	private void onNewData(DataLoadedChange change) {
		data = change.getData();
		replot();
	}





	private ValueAxis<Number> getYAxis() {
		double ymax = 100;
		if (data != null) {
			ymax = data.getDataSet().getAnalysis().maximumIntensity();
		}

		if (plotController.view().getViewLog()) {
			return new LogarithmicAxis(0, ymax * 1.1);
		} else {
			return new NumberAxis(0, ymax, SigDigits.toIntSigDigit(ymax / 10, 1));
		}
	}


	@Override
	protected void initialize() throws Exception {
		// TODO Auto-generated method stub

	}

	public static SpectrumUIController load(ChangeController changes) throws IOException {
		return FXUtil.load(SpectrumUIController.class, "Spectrum.fxml", changes);
	}

}
