package org.peakaboo.mapping.filter.plugin.plugins.clipping;

import java.util.Arrays;

import org.peakaboo.framework.autodialog.model.Parameter;
import org.peakaboo.framework.autodialog.model.style.editors.RealSpinnerStyle;
import org.peakaboo.framework.cyclops.ISpectrum;
import org.peakaboo.framework.cyclops.ReadOnlySpectrum;
import org.peakaboo.framework.cyclops.Spectrum;
import org.peakaboo.mapping.filter.model.AreaMap;
import org.peakaboo.mapping.filter.plugin.MapFilterDescriptor;
import org.peakaboo.mapping.filter.plugin.plugins.AbstractMapFilter;

public class SignalOutlierCorrectionMapFilter extends AbstractMapFilter {

	Parameter<Float> percent;
	
	@Override
	public String getFilterName() {
		return "Outlier Correction";
	}

	@Override
	public String getFilterDescription() {
		return "The Outlier Correction filter is useful for correcting a few erroneously intense pixels in a map. These intense pixels will be capped at the same value as the maximum remaining pixels. Manually managing bad scans or erroneous fits in the plot before mapping is still preferable, but can be quite time consuming.";
	}

	@Override
	public void initialize() {
		percent = new Parameter<>("Cutoff Percent", new RealSpinnerStyle(), 0.1f, this::validate);
		addParameter(percent);
	}

	private boolean validate(Parameter<?> param) {
		if (percent.getValue() <= 0f) { return false; }
		if (percent.getValue() > 5f) { return false; }
		return true;
	}
	
	@Override
	public AreaMap filter(AreaMap map) {
		
		float[] sorted = map.getData().backingArrayCopy();
		Arrays.sort(sorted);
		int index = Math.round((sorted.length-1) * (100f-percent.getValue()) / 100f );
		//bounds check the index, and we have to trim at least 1 pixel
		if (index == sorted.length-1) { index = sorted.length-2; }
		if (index < 0) { index = 0; }
		
		float cap = sorted[index];
		
		ReadOnlySpectrum olddata = map.getData();
		Spectrum newdata = new ISpectrum(olddata.size());
		for (int i = 0; i < olddata.size(); i++) {
			newdata.set(i, Math.min(cap, olddata.get(i)));
		}
		
		return new AreaMap(newdata, map.getSize(), map.getRealDimensions());
		
	}

	@Override
	public boolean pluginEnabled() {
		return true;
	}

	@Override
	public String pluginVersion() {
		return "1.0";
	}

	@Override
	public String pluginUUID() {
		return "ecb7ac3e-254e-4383-bb14-c75926d0eb76";
	}
	
	@Override
	public MapFilterDescriptor getFilterDescriptor() {
		return MapFilterDescriptor.CLIPPING;
	}
	
	@Override
	public boolean isReplottable() {
		return true;
	}

}
