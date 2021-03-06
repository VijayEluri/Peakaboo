package org.peakaboo.display.map;


import java.lang.ref.SoftReference;

import org.peakaboo.common.PeakabooConfiguration;
import org.peakaboo.common.PeakabooConfiguration.MemorySize;
import org.peakaboo.display.map.modes.MapMode;
import org.peakaboo.display.map.modes.composite.CompositeMapMode;
import org.peakaboo.framework.cyclops.Coord;
import org.peakaboo.framework.cyclops.visualization.Buffer;
import org.peakaboo.framework.cyclops.visualization.Surface;
import org.peakaboo.framework.cyclops.visualization.SurfaceType;
import org.peakaboo.framework.cyclops.visualization.drawing.map.MapDrawing;
import org.peakaboo.framework.cyclops.visualization.palette.Spectrums;

public class Mapper {

	private MapMode mapmode;
	
	private boolean invalidated;
	private SoftReference<Buffer> bufferRef = new SoftReference<>(null);
	private Coord<Integer> bufferSize, lastSize;
	
	public Mapper() {
		mapmode = new CompositeMapMode();
	}

	public void draw(MapRenderData data, MapRenderSettings settings, Surface context, Coord<Integer> size) {
			
		if (settings == null) {
			settings = new MapRenderSettings();
		}

		final int spectrumSteps = (settings.contours) ? settings.contourSteps : Spectrums.DEFAULT_STEPS;
		
		if (mapmode.getMode() != settings.mode) {
			mapmode = settings.mode.getMapper();
		}
		
		
		/*
		 * Determine if we want to buffer the drawing or not. When memory is tight, we
		 * take the performance hit to save space
		 */
		boolean doBuffer = true;
		//buffer space in MB
		int bufferSpace = (int)((size.x * 1.2f * size.y * 1.2f * 4) / 1024f / 1024f);
		if (bufferSpace > 10 && PeakabooConfiguration.memorySize == MemorySize.TINY) {
			doBuffer = false;
		}
		if (bufferSpace > 20 && PeakabooConfiguration.memorySize == MemorySize.SMALL) {
			doBuffer = false;
		}
		if (bufferSpace > 40 && PeakabooConfiguration.memorySize == MemorySize.MEDIUM) {
			doBuffer = false;
		}
		if (bufferSpace > 250 && PeakabooConfiguration.memorySize == MemorySize.LARGE) {
			doBuffer = false;
		}
		Runtime rt = Runtime.getRuntime();
		int freemem = (int) (rt.freeMemory() / 1024f / 1024f);
		if (bufferSpace * 1.2f > freemem) {
			doBuffer = false;
		}
		
		
		if (context.getSurfaceType() != SurfaceType.RASTER) {
			//We can't do raster-based buffering if the drawing target is vector/pdf
			//so just draw directly to the surface
			mapmode.draw(size, data, settings, context, spectrumSteps);
		} else if (doBuffer) {
			Buffer buffer = bufferRef.get();
			if (	buffer == null || 
					mapmode == null || 
					bufferSize == null || 
					lastSize == null || 
					//make sure the buffer is large enough
					bufferSize.x < size.x || 
					bufferSize.y < size.y ||
					//but if the buffer is much larger than it needs to be, make sure to reclaim that memory
					bufferSize.x > size.x*1.5f ||
					bufferSize.y > size.y*1.5f
				) {
				bufferSize = new Coord<>((int)(size.x*1.2f), (int)(size.y*1.2f));
				lastSize = new Coord<>(size);
				buffer = context.getImageBuffer(bufferSize.x, bufferSize.y);
				bufferRef = new SoftReference<>(buffer);
				mapmode.draw(size, data, settings, buffer, spectrumSteps);
				invalidated = false;
			} else if (!lastSize.equals(size) || invalidated) {
				//buffer exists, but size has changed, requiring redraw.
				mapmode.draw(size, data, settings, buffer, spectrumSteps);
				lastSize = new Coord<>(size);
				invalidated = false;
			}
			
			context.rectAt(0, 0, size.x, size.y);
			context.clip();
			context.compose(buffer, 0, 0, 1f);
		} else {
			bufferRef = new SoftReference<>(null);
			bufferSize = null;
			lastSize = null;
			mapmode.draw(size, data, settings, context, spectrumSteps);
		}
		
		
	
		//mapmode.draw(size, data, settings, context, spectrumSteps);

	}

	public MapDrawing getMap() {
		return mapmode.getMap();
	}
	
	public Coord<Integer> getCoordinate(float x, float y, boolean allowOutOfBounds) {
		return getMap().getMapCoordinateAtPoint(x, y, allowOutOfBounds);
	}
	
	/**
	 * Indicates that this map needs to be redrawn.
	 * 
	 * @param deep indicates that the map data itself has changed and the map cannot
	 *             simply be recomposited
	 */
	public void setNeedsRedraw(boolean deep) {
		if (deep) {
			mapmode.invalidate();
		}
		this.invalidated = true;
	}

}
