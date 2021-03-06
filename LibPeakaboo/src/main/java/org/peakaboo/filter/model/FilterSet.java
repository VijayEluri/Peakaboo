package org.peakaboo.filter.model;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.peakaboo.common.PeakabooLog;
import org.peakaboo.framework.cyclops.ISpectrum;
import org.peakaboo.framework.cyclops.ReadOnlySpectrum;
import org.peakaboo.framework.cyclops.Spectrum;
import org.peakaboo.framework.cyclops.SpectrumCalculations;

/**
 * 
 * This class provides a method of managing filters applied to a data set. Also provides the logic for
 * applying the filter set to a data set. is Iterable over Filters for access to filters in use
 * 
 * 
 * @author Nathaniel Sherry, 2009
 */

public class FilterSet implements Iterable<Filter>
{

	private List<Filter>	filters;


	public FilterSet()
	{

		filters = new ArrayList<>();
	}


	public synchronized void add(Filter filter)
	{
		filters.add(filter);
	}


	public synchronized void add(Filter filter, int index)
	{
		filters.add(index, filter);
	}


	public synchronized Filter get(int index)
	{
		return filters.get(index);
	}


	public synchronized void remove(int index)
	{
		if (index >= filters.size()) return;
		if (index < 0) return;
		filters.remove(index);
	}


	public synchronized void remove(Filter filter)
	{
		filters.remove(filter);
	}


	public synchronized void clear()
	{
		filters.clear();
	}


	public synchronized int size()
	{
		return filters.size();
	}


	public synchronized boolean contains(Filter f)
	{
		return filters.contains(f);
	}


	public synchronized int indexOf(Filter f)
	{
		return filters.indexOf(f);
	}


	public synchronized void moveFilterUp(int index)
	{

		Filter filter = filters.get(index);
		index -= 1;
		if (index < 0) index = 0;


		filters.remove(filter);
		filters.add(index, filter);

	}


	public synchronized void moveFilterDown(int index)
	{

		Filter filter = filters.get(index);
		index += 1;
		if (index >= filters.size()) index = filters.size() - 1;

		filters.remove(filter);
		filters.add(index, filter);

	}


	public synchronized ReadOnlySpectrum applyFilters(ReadOnlySpectrum data)
	{

		return applyFiltersUnsynchronized(data);
	}

	
	public ReadOnlySpectrum applyFiltersUnsynchronized(ReadOnlySpectrum data)
	{

		for (Filter f : filters) {
			if (f != null && f.isEnabled() && !f.isPreviewOnly()) {
				data = f.filter(data);
			}
		}
		
		//Replace Inf/NaN with 0
		data = correctNonFinite(data);

		return data;
	}
	
	//Scan the Spectrum for Infinity and NaN values, and replace them with 0 if found
	private ReadOnlySpectrum correctNonFinite(ReadOnlySpectrum data) {
		//Scan the results for Infinity and NaN values, and replace them with 0 if found
		Spectrum corrected = null;
		for (int i = 0; i < data.size(); i++) {
			float v = data.get(i);
			if (Float.isInfinite(v) || Float.isNaN(v)) {
				//only incur the copy penalty if needed
				if (corrected == null) {
					corrected = new ISpectrum(data);
				}
				corrected.set(i, 0);
			}
		}
		if (corrected != null) {
			PeakabooLog.get().log(Level.WARNING, "Filtered data contained NaN or Infinity");
			data = corrected;
		}
		return data;
	}

	public Iterator<Filter> iterator()
	{
		// TODO Auto-generated method stub
		return filters.iterator();
	}



	public synchronized List<Filter> getFilters() {
		return new ArrayList<>(filters);
	}


	public Map<Filter, ReadOnlySpectrum> calculateDeltas(ReadOnlySpectrum data) {
		
		Map<Filter, ReadOnlySpectrum> deltas = new LinkedHashMap<>();
		
		ReadOnlySpectrum last = data;
		ReadOnlySpectrum current = null;
		ReadOnlySpectrum delta = null;
		
		for (Filter f : filters) {
			if (f != null && f.isEnabled()) {
				current = f.filter(last);
				current = correctNonFinite(current);
				delta = SpectrumCalculations.subtractLists(last, current, 0f);
				deltas.put(f, delta);
				
				//only commit this filter's results for the next round if this filter is not in preview mode.
				if (!f.isPreviewOnly()) {
					last = current;
				}
			}
		}
		
		return deltas;
		
	}


}
