package peakaboo.filter.filters.noise;



import bolt.plugin.Plugin;
import peakaboo.calculations.Noise;
import peakaboo.filter.AbstractSimpleFilter;
import peakaboo.filter.Parameter;
import peakaboo.filter.Parameter.ValueType;
import scitypes.Spectrum;



/**
 * This class is a filter exposing the Wavelet Noise Filter functionality elsewhere in this programme.
 * 
 * @author Nathaniel Sherry, 2009
 */

@Plugin
public final class WaveletNoiseFilter extends AbstractSimpleFilter
{

	private final int	PASSES	= 0;


	public WaveletNoiseFilter()
	{
		super();

	}

	@Override
	public void initialize()
	{
		addParameter(PASSES, new Parameter(ValueType.INTEGER, "Passes to Transform", 1));
	}

	@Override
	public String getPluginName()
	{
		return "Wavelet Low-Pass";
	}


	@Override
	public FilterType getFilterType()
	{
		return FilterType.NOISE;
	}


	@Override
	public boolean validateParameters()
	{
		int passes;

		// remove largest, least significant passes from the wavelet transform
		// data
		// probably a bad idea to do more than 3 passes, but less than 1 is
		// senseless
		passes = getParameter(PASSES).intValue();
		if (passes > 8 || passes < 1) return false;

		return true;
	}


	@Override
	public String getPluginDescription()
	{
		return "The "
				+ getPluginName()
				+ " filter attempts to reduce high-frequency noise by performing a Wavelet transformation on the spectrum. This breaks the data down into sections each representing a different frequency range. The high-frequency regions are then smoothed, and a reverse transform is applied.";
	}


	@Override
	protected Spectrum filterApplyTo(Spectrum data)
	{
		Spectrum result;
		int passes = getParameter(PASSES).intValue();

		result = Noise.FWTLowPassFilter(data, passes);

		return result;
	}
	
	@Override
	public boolean pluginEnabled()
	{
		return true;
	}


	@Override
	public boolean canFilterSubset()
	{
		return false;
	}




}
