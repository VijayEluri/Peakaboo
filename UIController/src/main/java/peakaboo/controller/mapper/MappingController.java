package peakaboo.controller.mapper;



import java.io.InputStream;
import java.util.List;

import eventful.EventfulType;
import eventful.EventfulTypeListener;
import peakaboo.controller.mapper.mapdisplay.MapDisplayController;
import peakaboo.controller.mapper.mapset.MapSetController;
import peakaboo.controller.mapper.mapview.MapSettings;
import peakaboo.controller.plotter.IPlotController;
import peakaboo.datasource.model.DataSource;
import scitypes.Coord;


public class MappingController extends EventfulType<String>
{

	public enum UpdateType
	{
		DATA_OPTIONS, DATA, UI_OPTIONS, BOUNDING_REGION, POINT_SELECTION, TABS;
	}
	
	
	public 	MapSetController		mapsController;
	private MapDisplayController	display;
	public  MapSettings				settings;
		
	private IPlotController		plotcontroller;
	
	
	public MappingController(IPlotController plotcontroller)
	{
		
		this.mapsController = new MapSetController();
		this.settings = new MapSettings(this);

		initialize(plotcontroller);
		
	}

	/**
	 * This copy constructor copies only the user preferences from the map,
	 * and does not copy anthing else like map data
	 * @param copy
	 * @param plotcontroller
	 */
	public MappingController(MappingController copy, IPlotController plotcontroller)
	{
		this.mapsController = copy.mapsController;
		this.settings = new MapSettings(this, copy.settings);
		
		initialize(plotcontroller);
		
	}
	
	/**
	 * This copy constructor copies the user preferences from the map,
	 * and does directly references the map data
	 * @param copy
	 * @param plotcontroller
	 */
	public MappingController(MapSetController data, MapSettings settings, IPlotController plotcontroller)
	{
		this.mapsController = data;
		this.settings = new MapSettings(this, settings);
		
		initialize(plotcontroller);
		
	}
	
	
	private void initialize(IPlotController plotcontroller)
	{
		display = new MapDisplayController(this);

			
		mapsController.addListener(this::updateListeners);
		display.addListener(this::updateListeners);
		settings.addListener(this::updateListeners);
		

		this.plotcontroller = plotcontroller;
	}
	

	

	public MapDisplayController getDisplay() {
		return display;
	}

	public DataSource getDataSourceForSubset(Coord<Integer> cstart, Coord<Integer> cend)
	{
		return plotcontroller.data().getDataSourceForSubset(settings.getDataWidth(), settings.getDataHeight(), cstart, cend);
	}

	public DataSource getDataSourceForSubset(List<Integer> points)
	{
		return plotcontroller.data().getDataSourceForSubset(points);
	}
	
	
	public InputStream getSerializedPlotSettings()
	{
		return plotcontroller.getSerializedPlotSettings();
	}
	

}
