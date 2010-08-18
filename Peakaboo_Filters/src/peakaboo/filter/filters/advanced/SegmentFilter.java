package peakaboo.filter.filters.advanced;

import java.util.List;

import fava.Fn;
import fava.signatures.FnMap;

import peakaboo.filter.AbstractFilter;
import peakaboo.filter.AvailableFilters;
import peakaboo.filter.Parameter;
import peakaboo.filter.Parameter.ValueType;
import scidraw.drawing.painters.PainterData;
import scidraw.drawing.plot.painters.PlotPainter;
import scitypes.Spectrum;


public class SegmentFilter extends AbstractFilter
{

	public static final int FILTER = 0;
	private static final int START = 1;
	private static final int END = 2;
	
	
	public SegmentFilter()
	{

	}
	
	
	@Override
	public void initialize()
	{
		List<AbstractFilter> filters = Fn.filter(
				AvailableFilters.getNewInstancesForAllFilters(), 
				
				new FnMap<AbstractFilter, Boolean>() {

					public Boolean f(AbstractFilter f)
					{
						return f.showFilter() && f.canFilterSubset();
					}}

		);
		filters.add(0, new Identity());
		
		for (AbstractFilter f : filters)
		{
			f.initialize();
		}
		
		parameters.put(FILTER, new Parameter(ValueType.FILTER, "Filter", filters.get(0), filters.toArray()));
		parameters.put(START, new Parameter(ValueType.INTEGER, "Start Index", 0));
		parameters.put(END, new Parameter(ValueType.INTEGER, "Stop Index", 10));
	}
	
	
	@Override
	protected Spectrum filterApplyTo(Spectrum data, boolean cache)
	{
		
		int start = parameters.get(START).intValue();
		int stop = parameters.get(END).intValue();
		
		if (start >= data.size()) start = data.size()-1;
		if (stop >= data.size()) stop = data.size()-1;
		
		Spectrum result = new Spectrum(data);
		Spectrum subspectrum = data.subSpectrum(start, stop);
		
		subspectrum = parameters.get(FILTER).filterValue().filter(subspectrum, cache);
		
		for (int i = start; i <= stop; i++)
		{
			result.set(i, subspectrum.get(i-start));
		}
		
		return result;
	}

	@Override
	public String getFilterDescription()
	{
		return "The " + getFilterName() + " filter allows the application of another filter to a portion of a spectrum.";
	}

	@Override
	public String getFilterName()
	{
		return "Filter Partial Spectrum";
	}

	@Override
	public FilterType getFilterType()
	{
		return FilterType.ADVANCED;
	}

	@Override
	public PlotPainter getPainter()
	{

		if (parameters.get(FILTER).filterValue().getPainter() == null) return null;
		
		return new PlotPainter() {

			@Override
			public void drawElement(PainterData p)
			{			
				p.context.save();
				
					float pointWidth = p.plotSize.x / p.dr.dataWidth;
					p.context.translate(pointWidth*parameters.get(START).intValue(), 0f);
					parameters.get(FILTER).filterValue().getPainter().draw(p);
					
				p.context.restore();
			}
		};
		
	}

	@Override
	public boolean showFilter()
	{
		return true;
	}

	@Override
	public boolean validateParameters()
	{
		
		int start = parameters.get(START).intValue();
		int stop = parameters.get(END).intValue();
		
		if (start < 0) return false;
		if (stop < 0) return false;
		if (stop < start) return false;
		
		return true;
	}
	
	
	@Override
	public boolean canFilterSubset()
	{
		return false;
	}

}
