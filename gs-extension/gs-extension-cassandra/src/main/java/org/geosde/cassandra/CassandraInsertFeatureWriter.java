package org.geosde.cassandra;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class CassandraInsertFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature>{
	
	public CassandraInsertFeatureWriter() {
		// TODO Auto-generated constructor stub
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
	public SimpleFeature next() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	@Override
	public void write() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void remove() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
