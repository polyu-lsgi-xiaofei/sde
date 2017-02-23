package org.geosde.cassandra;

import java.io.IOException;

import org.geosde.core.jdbc.JDBCFeatureReader;
import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.BatchStatement;

public class CassandraInsertFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature>{
	
	private final static int BATCH_SIZE=20;
	private SimpleFeature currentFeature;
	private BatchStatement bs;
	private int curBufferPos = 0;
	private SimpleFeatureType sft;
	
	public CassandraInsertFeatureWriter(SimpleFeatureType sft) {
		this.sft=sft;
		bs=new BatchStatement();
	}
	
	@Override
	public SimpleFeatureType getFeatureType() {
		return sft;
	}
	@Override
	public boolean hasNext() throws IOException {
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
