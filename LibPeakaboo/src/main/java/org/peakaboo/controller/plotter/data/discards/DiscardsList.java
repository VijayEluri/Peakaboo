package org.peakaboo.controller.plotter.data.discards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.peakaboo.controller.plotter.PlotController;

public class DiscardsList implements Discards {

	private List<Integer> discards = new ArrayList<Integer>();
	private PlotController plot;
	
	public DiscardsList(PlotController plot) {
		this.plot = plot;
	}
	
	@Override
	public boolean isDiscarded(int scanNo) {
		return discards.contains(scanNo);
	}

	@Override
	public void discard(int scanNo) {
		if (isDiscarded(scanNo)) { return; }
		discards.add(scanNo);
		
		plot.filtering().filteredDataInvalidated();
		plot.history().setUndoPoint("Marking Scan Bad");
		
	}

	@Override
	public void undiscard(int scanNo) {
		if (isDiscarded(scanNo)) {
			discards.remove(new Integer(scanNo));
			plot.filtering().filteredDataInvalidated();
			plot.history().setUndoPoint("Marking Scan Good");
		}
	}

	@Override
	public List<Integer> list() {
		return Collections.unmodifiableList(discards);
	}

	@Override
	public void clear() {
		discards.clear();
		
		//I added this invalidation here during a refactoring 
		//because it seemed like it needed it.
		//TODO: Why are we not also updating listeners here?
		plot.filtering().filteredDataInvalidated();
		
	}

	
	
}
