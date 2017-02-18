package org.geosde.cassandra;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class CassandraFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature>{
	
	Query query;
	
	public CassandraFeatureReader(Query query) {
		this.query=query;
	}
	
	@Override
	public SimpleFeatureType getFeatureType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasNext() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
