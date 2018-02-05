package peakaboo.controller.mapper.mapset;

import java.util.ArrayList;
import java.util.List;

import eventful.EventfulType;
import peakaboo.controller.mapper.MappingController.UpdateType;
import peakaboo.controller.mapper.mapview.MapSettings;
import peakaboo.mapping.results.MapResultSet;
import scitypes.Bounds;
import scitypes.Coord;
import scitypes.ISpectrum;
import scitypes.SISize;
import scitypes.Spectrum;


public class MapSetController extends EventfulType<String> implements IMapSetController
{

	MapSetMapData mapModel;
	
	
	public MapSetController()
	{
		mapModel = new MapSetMapData();
	}
	

	
	public void setMapData(
			MapResultSet data,
			String datasetName,
			List<Integer> badPoints
	)
	{
		
		mapModel.mapResults = data;
		mapModel.datasetTitle = datasetName;
		mapModel.badPoints = badPoints;
		
		mapModel.dimensionsProvided = false;
		
		mapModel.realDimensions = null;
		mapModel.realDimensionsUnits = null;
		
		updateListeners(UpdateType.DATA.toString());
		
	}
	
	public void setMapData(
			MapResultSet data,
			String datasetName,
			Coord<Integer> dataDimensions,
			Coord<Bounds<Number>> realDimensions,
			SISize realDimensionsUnits,
			List<Integer> badPoints
	)
	{
	
		
		mapModel.mapResults = data;
		mapModel.datasetTitle = datasetName;
		mapModel.badPoints = badPoints;
		
		mapModel.originalDimensions = dataDimensions;
		mapModel.dimensionsProvided = true;
		mapModel.realDimensions = realDimensions;
		mapModel.realDimensionsUnits = realDimensionsUnits;

		updateListeners(UpdateType.DATA.toString());

	}
	
	public int getMapSize()
	{
		return mapModel.mapSize();
	}

	
	


	

	

	

	
	public int getOriginalDataHeight() {
		return mapModel.originalDimensions.y;
	}
	public int getOriginalDataWidth() {
		return mapModel.originalDimensions.x;
	}
	
	
	public Coord<Integer> guessDataDimensions() {
		Spectrum all = mapModel.mapResults.sumAllTransitionSeriesMaps();
		Spectrum deltas = new ISpectrum(all);
		
		
		//compute first order deltas
		for (int i = 1; i < deltas.size(); i++) {
			float delta = Math.abs(deltas.get(i) - deltas.get(i-1));
			deltas.set(i-1, delta);
		}
		
		//compute second order deltas. We're not looking for steep changes, 
		//we're looking for sudden discontinuities, the change in the change.
		for (int i = 1; i < deltas.size(); i++) {
			float delta = (float)Math.sqrt(Math.abs(deltas.get(i) - deltas.get(i-1)));
			deltas.set(i-1, delta);
		}
		
		//find the highest average edge delta
		int bestX = 0, bestY = 0;
		float bestDelta = Float.MAX_VALUE;
		int min = (int) Math.max(Math.sqrt(all.size()) / 15, 2); //don't consider dimensions that are too small
		for (int x = min; x <= all.size() / min; x++) {
			
			float delta;
			int y;
			if (all.size() % x == 0) {
				y = all.size() / x;
				delta = getDimensionScore(all, x, y);
			} else {
				y = (int)Math.ceil(all.size() / (float)x);
				delta = getDimensionScore(all, x, y); //include the last incomplete row
			}
			
			//System.out.println("x=" + x + ", y=" + y + ", delta=" + delta);
			
			if (delta < bestDelta) {
				bestX = x;
				bestY = y;
				bestDelta = delta;
			}
			
			
		}
		
		if (bestX > 0 && bestY > 0) {
			return new Coord<>(bestX, bestY);
		} else {
			return null;
		}
		
	}
	
	
	//helper for guessDataDimensions, calculates the deltas along the wrapping 
	//left-hand edge of a map between the end of one row and the start of the 
	//next. Higher values should indicate the dimensions are correct and the 
	//two sets of points are not next to each other.
	private float getDimensionScore(Spectrum map, int width, int height) {
		return map2dDelta(map2dDelta(map, width, height), width, height).sum();
	}
	
	private Spectrum map2dDelta(Spectrum map, int width, int height) {
		Spectrum deltas = new ISpectrum(map.size());
		float delta = 0;
		float value = 0;
		int count = 0;
		int dind;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (y*width+x >= map.size()) break;
				value = map.get(y*width+x);
				count = 0;
				delta = 0;
				
				dind = y*width+(x-1);
				if (x > 0) {
					delta += Math.abs(value - map.get(dind));
					count++;
				}
				
				dind = (y-1)*width+x;
				if (y > 0) {
					delta += Math.abs(value - map.get(dind));
					count++;
				}

				dind = y*width+(x+1);
				if (x < width-1 && dind < map.size()) {
					delta += Math.abs(value - map.get(dind));
					count++;
				}
				
				dind = (y+1)*width+x;
				if (y < height-1 && dind < map.size()) {
					delta += Math.abs(value - map.get(dind));
					count++;
				}
				
				delta /= (float)count;
				deltas.set(y*width+x, delta);

			}
		}
		
		return deltas;
		
	}
	
	


	public boolean isDimensionsProvided()
	{
		return mapModel.dimensionsProvided;
	}


	public List<Integer> getBadPoints()
	{
		return new ArrayList<>(mapModel.badPoints);
	}


	public boolean isValidPoint(Coord<Integer> mapCoord)
	{
		return (mapCoord.x >= 0 && mapCoord.x < mapModel.originalDimensions.x && mapCoord.y >= 0 && mapCoord.y < mapModel.originalDimensions.y);
	}


	public String getDatasetTitle()
	{
		return mapModel.datasetTitle;
	}


	public void setDatasetTitle(String name)
	{
		mapModel.datasetTitle = name;
	}


	public void setMapCoords(Coord<Number> tl, Coord<Number> tr, Coord<Number> bl, Coord<Number> br)
	{
		mapModel.topLeftCoord = tl;
		mapModel.topRightCoord = tr;
		mapModel.bottomLeftCoord = bl;
		mapModel.bottomRightCoord = br;
	}


	public Coord<Bounds<Number>> getRealDimensions()
	{
		return mapModel.realDimensions;
	}

	public Coord<Number> getTopLeftCoord()
	{
		return mapModel.topLeftCoord;
	}
	public Coord<Number> getTopRightCoord()
	{
		return mapModel.topRightCoord;
	}
	public Coord<Number> getBottomLeftCoord()
	{
		return mapModel.bottomLeftCoord;
	}
	public Coord<Number> getBottomRightCoord()
	{
		return mapModel.bottomRightCoord;
	}
	
	
	public SISize getRealDimensionUnits()
	{
		return mapModel.realDimensionsUnits;
	}
	



	public MapResultSet getMapResultSet()
	{
		return mapModel.mapResults;
	}
	
}
