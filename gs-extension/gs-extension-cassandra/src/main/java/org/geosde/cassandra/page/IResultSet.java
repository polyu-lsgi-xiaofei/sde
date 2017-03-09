package org.geosde.cassandra.page;

public interface IResultSet<T> {

	public T next();
	public boolean hasNext();
	
	
}
