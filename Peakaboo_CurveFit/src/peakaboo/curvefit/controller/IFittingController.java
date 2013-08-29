package peakaboo.curvefit.controller;

import java.util.List;

import peakaboo.curvefit.model.FittingModel;
import peakaboo.curvefit.model.FittingResultSet;
import peakaboo.curvefit.model.FittingSet;
import peakaboo.curvefit.model.transitionseries.EscapePeakType;
import peakaboo.curvefit.model.transitionseries.TransitionSeries;
import peakaboo.curvefit.model.transitionseries.TransitionSeriesType;
import scitypes.Spectrum;
import eventful.IEventfulType;
import fava.functionable.FList;


public interface IFittingController extends IEventfulType<Boolean>
{

	//For Committed Fittings
	void addTransitionSeries(TransitionSeries e);
	void addAllTransitionSeries(List<TransitionSeries> e);
	void removeTransitionSeries(TransitionSeries e);
	void clearTransitionSeries();
	
	FList<TransitionSeries> getFittedTransitionSeries();
	FList<TransitionSeries> getUnfittedTransitionSeries(TransitionSeriesType tst);
	
	void setTransitionSeriesVisibility(TransitionSeries e, boolean show);
	boolean getTransitionSeriesVisibility(TransitionSeries e);
	FList<TransitionSeries> getVisibleTransitionSeries();
	
	float getTransitionSeriesIntensity(TransitionSeries ts);
	void moveTransitionSeriesUp(TransitionSeries e);
	void moveTransitionSeriesDown(TransitionSeries e);
	void moveTransitionSeriesUp(List<TransitionSeries> e);
	void moveTransitionSeriesDown(List<TransitionSeries> e);
	
	FittingSet getFittingSelections();
	void calculateSelectionFittings(Spectrum data);
	void fittingDataInvalidated();
	boolean hasSelectionFitting();
	FittingResultSet	getFittingSelectionResults();
	
	boolean canMap();
	
	
	
	//For Proposed Fittings
	void addProposedTransitionSeries(TransitionSeries e);
	void removeProposedTransitionSeries(TransitionSeries e);
	void clearProposedTransitionSeries();
	
	List<TransitionSeries> getProposedTransitionSeries();
	void commitProposedTransitionSeries();
	
	void calculateProposalFittings();
	void fittingProposalsInvalidated();
	boolean hasProposalFitting();
	FittingResultSet getFittingProposalResults();
	
	
	//Escape Peaks
	void setEscapeType(EscapePeakType type);
	EscapePeakType getEscapeType();
	
	
	
	//Magic
	List<TransitionSeries> proposeTransitionSeriesFromChannel(final int channel, TransitionSeries currentTransition);
	void optimizeTransitionSeriesOrdering();
	void setFittingParameters(float energyPerChannel);
	FittingModel getFittingModel();
}
