package peakaboo.mapping;

import java.util.List;
import java.util.function.Consumer;

import peakaboo.curvefit.fitting.FittingResult;
import peakaboo.curvefit.fitting.FittingResultSet;
import peakaboo.curvefit.fitting.FittingSet;
import peakaboo.curvefit.transition.TransitionSeries;
import peakaboo.dataset.DataSet;
import peakaboo.dataset.StandardDataSet;
import peakaboo.filter.model.FilterSet;
import peakaboo.mapping.results.MapResultSet;
import plural.executor.ExecutorSet;
import plural.executor.eachindex.EachIndexExecutor;
import plural.executor.eachindex.implementations.PluralEachIndexExecutor;
import plural.streams.StreamExecutor;
import scitypes.Range;
import scitypes.ReadOnlySpectrum;

/**
 * This class contains logic for generating maps for a {@link AbstractDataSet}, so that functionality does not have to be duplicated across various implementations
 * @author Nathaniel Sherry, 2010
 *
 */

public class MapTS
{

	/**
	 * Generates a map based on the given inputs. Returns a {@link StreamExecutor} which can execute this task asynchronously and return the result
	 * @param dataset the {@link DataSet} providing access to data
	 * @param filters the {@link FilterSet} containing all filters needing to be applied to this data
	 * @param fittings the {@link FittingSet} containing all fittings needing to be turned into maps
	 * @param type the way in which a fitting should be mapped to a 2D map. (eg height, area, ...)
	 * @return a {@link StreamExecutor} which will return a {@link MapResultSet}
	 */
	public static StreamExecutor<MapResultSet> map(DataSet dataset, FilterSet filters, FittingSet fittings) {
		
		List<TransitionSeries> transitionSeries = fittings.getVisibleTransitionSeries();
		MapResultSet maps = new MapResultSet(transitionSeries, dataset.getScanData().scanCount());
		
		//Math.max(1, dataset.getScanData().scanCount())
		StreamExecutor<MapResultSet> streamer = new StreamExecutor<>("Applying Filters & Fittings", 1);
		streamer.setTask(new Range(0, dataset.getScanData().scanCount()-1), stream -> {
			stream.forEach(index -> {
				
				ReadOnlySpectrum data = dataset.getScanData().get(index);
				if (data == null) return;
				
				data = filters.applyFiltersUnsynchronized(data);
				FittingResultSet frs = fittings.fit(data);
				
				for (FittingResult result : frs.getFits()) {
					maps.putIntensityInMapAtPoint(result.getFit().sum(), result.getTransitionSeries(), index);
				}
				
			});
			return maps;
		}); 
		
		return streamer;
		
	}
	
}
