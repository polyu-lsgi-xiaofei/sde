package org.geosde.cassandra;

import java.io.IOException;
import java.util.Set;

import org.geosde.core.data.ContentEntry;
import org.geosde.core.data.ContentFeatureStore;
import org.geosde.core.data.ContentState;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.factory.Hints.Key;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import com.datastax.driver.core.Session;

public class CassandraFeatureStore extends ContentFeatureStore {

	CassandraFeatureSource delegate;

	public CassandraFeatureStore(ContentEntry entry, Session session) {
		super(entry, Query.ALL);
		this.delegate = new CassandraFeatureSource(session, entry);
		this.hints = (Set<Key>) (Set<?>) delegate.getSupportedHints();
	}

	@Override
	protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(Query query, int flags)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	// ----------------------------------------------------------------------------------------
	// METHODS DELEGATED TO OGRFeatureSource
	// ----------------------------------------------------------------------------------------

	public CassandraDataStore getDataStore() {
		return delegate.getDataStore();
	}

	public Transaction getTransaction() {
		return delegate.getTransaction();
	}

	public ResourceInfo getInfo() {
		return delegate.getInfo();
	}

	public QueryCapabilities getQueryCapabilities() {
		return delegate.getQueryCapabilities();
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		return delegate.getBoundsInternal(query);
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		return delegate.getCountInternal(query);
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		return delegate.getReaderInternal(query);
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		return delegate.buildFeatureType();
	}

	@Override
	public ContentEntry getEntry() {
		return delegate.getEntry();
	}

	@Override
	public Name getName() {
		return delegate.getName();
	}

	@Override
	public ContentState getState() {
		return delegate.getState();
	}

	@Override
	public void setTransaction(Transaction transaction) {
		super.setTransaction(transaction);

		if (delegate.getTransaction() != transaction) {
			delegate.setTransaction(transaction);
		}
	}

}
