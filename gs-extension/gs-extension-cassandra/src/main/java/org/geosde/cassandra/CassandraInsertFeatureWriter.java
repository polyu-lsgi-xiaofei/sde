package org.geosde.cassandra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.BatchStatement;

public class CassandraInsertFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

	private final static int BATCH_SIZE = 20;
	private SimpleFeature currentFeature;
	private BatchStatement bs;
	private int curBufferPos = 0;
	private SimpleFeatureType sft;
	// an array for reuse in Feature creation
	protected Object[] emptyAtts;

	protected List<SimpleFeature> featurelist;

	public CassandraInsertFeatureWriter(SimpleFeatureType sft) {
		this.sft = sft;
		bs = new BatchStatement();
		featurelist = new ArrayList<>();
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
		// reader has no more (no were are adding to the file)
		// so return an empty feature
		String featureID = UUID.randomUUID().toString();

		currentFeature = DataUtilities.template(getFeatureType(), featureID, emptyAtts);
		featurelist.add(currentFeature);
		return currentFeature;

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public void write() throws IOException {
		if (++curBufferPos >= CassandraInsertFeatureWriter.BATCH_SIZE) {
			// buffer full => do the inserts
			flush();
		}
	}

	public void flush() {
		if (curBufferPos == 0) {
			return;
		}
		try {
			for (SimpleFeature cur : featurelist) {
				// the datastore sets as userData, grab it and update the fid
				final String fid = (String) cur.getUserData().get("fid");
				System.out.println(fid + ":" + cur.getDefaultGeometry());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		featurelist.clear();
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
