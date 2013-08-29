package peakaboo.datasource;

import java.util.List;

import peakaboo.datasource.interfaces.DSDimensions;
import peakaboo.datasource.interfaces.DSMetadata;
import peakaboo.datasource.interfaces.DSScanData;

public interface DataSource extends DSScanData, DSDimensions, DSMetadata
{
	
	
	/**
	 * Returns true if this data source supports metadata
	 */
	boolean hasMetadata();
	
	
	/**
	 * Returns true if this data source supports information on dimensions
	 */
	boolean hasScanDimensions();
	
	
	/**
	 * Returns a list of strings representing the file extensions that
	 * this DataSource is capable of reading
	 */
	List<String> getFileExtensions();
	
	
	/**
	 * Returns true if this DataSource can read the given file as a whole 
	 * dataset, false otherwise.
	 */
	boolean canRead(String filename);

	/**
	 * Returns true if this DataSource can read the given files as a whole 
	 * dataset, false otherwise
	 */
	boolean canRead(List<String> filenames);
	

	/**
	 * Reads the given file as a whole dataset. This method, collectively with 
	 * {@link DataSource#read(List)}, will be called either 0 or 1 times 
	 * throughout the lifetime of this DataSource object.
	 * @throws Exception
	 */
	void read(String filename) throws Exception;
	
	/**
	 * Reads the given files as a whole dataset. This method, collectively with 
	 * {@link DataSource#read(String)}, will be called either 0 or 1 times 
	 * throughout the lifetime of this DataSource object.
	 * @throws Exception
	 */
	void read(List<String> filenames) throws Exception;
}
