package peakaboo.controller.mapper.mapview;

import java.io.File;

import eventful.EventfulType;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.MappingController.UpdateType;
import peakaboo.controller.mapper.mapset.MapSetMapData;
import scitypes.Coord;

/**
 * Settings for a map view which are not directly dependent on the contents 
 * of the map (eg no TransisionSeries lists, etc)
 * @author NAS
 *
 */
public class MapSettings extends EventfulType<String> //TODO remove extends
{

	//SOURCE DATA
	private MappingController mapController;
	
	
	//SETTINGS
	public boolean	drawCoordinates		= true;
	public boolean	drawSpectrum		= true;
	public boolean	drawTitle			= true;
	
	public boolean	showDataSetTitle	= false;
	public int		spectrumSteps		= 15;
	public boolean	contour				= false;
	public int		interpolation		= 0;
	public boolean	monochrome			= false;
	
	public Coord<Integer> viewDimensions = new Coord<Integer>(1, 1);
	
	public File		savePictureFolder 	= null;
	public File		dataSourceFolder 	= null;
	
	public MapSettings(MappingController mapController, MapSettings copy)
	{
		this(mapController);
		if (copy != null) {
			this.drawCoordinates = copy.drawCoordinates;
			this.drawSpectrum = copy.drawSpectrum;
			this.drawTitle = copy.drawTitle;
			this.showDataSetTitle = copy.showDataSetTitle;
			this.spectrumSteps = copy.spectrumSteps;
			this.contour = copy.contour;
			this.interpolation = copy.interpolation;
			this.monochrome = copy.monochrome;
			
			this.viewDimensions = new Coord<>(copy.viewDimensions);
			
			this.savePictureFolder = copy.savePictureFolder;
			this.dataSourceFolder = copy.dataSourceFolder;
		}
	}
	
	
	public MapSettings(MappingController mapController)
	{
		setMappingController(mapController);
	}
	
	
	public void setMappingController(MappingController controller) {
		this.mapController = controller;
		if (mapController.mapsController.isDimensionsProvided()) {
			viewDimensions = new Coord<>(mapController.mapsController.getOriginalDataWidth(), mapController.mapsController.getOriginalDataHeight());
		} else {
			viewDimensions = new Coord<Integer>(mapController.mapsController.getMapResultSet().getMap(0).data.size(), 1);
		}
	}


	
	// interpolation
	public void setInterpolation(int passes)
	{
		int side, newside;
		while (true) {
			
			side = (int)Math.sqrt( getDataHeight() * getDataWidth() );
			
			newside = (int)(side * Math.pow(2, passes));
		
			if (newside > 750) {
				passes--;
			} else {
				break;
			}
		
		}

		
		if (passes < 0) passes = 0;
		interpolation = passes;
		updateListeners(UpdateType.DATA_OPTIONS.toString());
	}
	
	public int getInterpolation()
	{
		return interpolation;
	}
	
	

	// data height and width
	public void setDataHeight(int height)
	{

		if (getDataWidth() * height > mapController.mapsController.getMapSize()) height = mapController.mapsController.getMapSize() / getDataWidth();
		if (height < 1) height = 1;

		viewDimensions.y = height;
		
		setInterpolation(interpolation);
		
		updateListeners(UpdateType.DATA_OPTIONS.toString());
	}


	public int getDataHeight()
	{
		return viewDimensions.y;
	}


	public void setDataWidth(int width)
	{

		if (getDataHeight() * width > mapController.mapsController.getMapSize()) width = mapController.mapsController.getMapSize() / getDataHeight();
		if (width < 1) width = 1;

		viewDimensions.x = width;
		
		setInterpolation(interpolation);
		
		updateListeners(UpdateType.DATA_OPTIONS.toString());
	}

	public int getDataWidth()
	{
		return viewDimensions.x;
	}
	

	// contours
	public void setContours(boolean contours)
	{
		this.contour = contours;
				
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getContours()
	{
		return this.contour;
	}


	// spectrum
	public void setSpectrumSteps(int steps)
	{
		if (steps > 25) steps = 25;
		if (steps > 0)
		{
			this.spectrumSteps = steps;
		}
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public int getSpectrumSteps()
	{
		return this.spectrumSteps;
	}


	public void setMonochrome(boolean mono)
	{
		this.monochrome = mono;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getMonochrome()
	{
		return this.monochrome;
	}

	

	public void setShowSpectrum(boolean show)
	{
		this.drawSpectrum = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowSpectrum()
	{
		return this.drawSpectrum;
	}


	public void setShowTitle(boolean show)
	{
		this.drawTitle = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowTitle()
	{
		return this.drawTitle;
	}


	public void setShowDatasetTitle(boolean show)
	{
		this.showDataSetTitle = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowDatasetTitle()
	{
		return this.showDataSetTitle;
	}


	public void setShowCoords(boolean show)
	{
		this.drawCoordinates = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowCoords()
	{
		return this.drawCoordinates;
	}
	
	
	public boolean getDrawCoords()
	{
		return this.drawCoordinates;
	}
	public void setDrawCoords(boolean draw)
	{
		this.drawCoordinates = draw;
	}
	
	
	
	public int getInterpolatedHeight()
	{
		
		int height = getDataHeight();
		
		for (int i = 0; i < getInterpolation(); i++)
		{
			height = height * 2 - 1;
		}
		
		return height;
		
	}


	public int getInterpolatedWidth()
	{
		int width = getDataWidth();
		
		for (int i = 0; i < getInterpolation(); i++)
		{
			width = width * 2 - 1;
		}
		
		return width;
		
	}
	
}