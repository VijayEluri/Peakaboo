package org.peakaboo.mapping.filter.plugin.plugins;

import org.peakaboo.mapping.filter.model.AreaMap;

import cyclops.ISpectrum;
import cyclops.ReadOnlySpectrum;
import cyclops.Spectrum;
import net.sciencestudio.autodialog.model.Parameter;
import net.sciencestudio.autodialog.model.style.editors.IntegerSpinnerStyle;

public class SignalLimitMapFilter extends AbstractMapFilter {

	Parameter<Integer> limit;
	
	@Override
	public String getFilterName() {
		return "Signal Limit";
	}

	@Override
	public String getFilterDescription() {
		return "This filter limits the value for any pixel in a map to a specified value.";
	}

	@Override
	public void initialize() {
		limit = new Parameter<Integer>("Signal Limit", new IntegerSpinnerStyle(), 10000, this::validate);
		addParameter(limit);
	}

	private boolean validate(Parameter<?> param) {
		if (limit.getValue() <= 0) { return false; }
		return true;
	}
	
	@Override
	public AreaMap filter(AreaMap map) {
		
		float cap = limit.getValue();
		
		ReadOnlySpectrum olddata = map.getData();
		Spectrum newdata = new ISpectrum(olddata.size());
		for (int i = 0; i < olddata.size(); i++) {
			newdata.set(i, Math.min(cap, olddata.get(i)));
		}
		
		return new AreaMap(newdata, map.getSize());
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
		return "18e25949-9c6c-4487-b466-7fda8359459e";
	}
	
	@Override
	public String getFilterAction() {
		return "Clipped";
	}

}
