package org.geosde.core;



/**
 * This interface should be implemented by any custom data type that must be
 * stored in the Cassandra index. It enables storing and retrieving the data, as
 * well as translating the data into values and queries that can be used to
 * index. Additionally, each entry is responsible for providing visibility if
 * applicable.
 * 
 * @param <T>
 *            The type for the data elements that are being adapted
 */
public interface DataAdapter<T> {
	
	


}
