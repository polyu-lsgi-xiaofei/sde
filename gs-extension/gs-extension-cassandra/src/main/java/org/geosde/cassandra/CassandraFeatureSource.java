package org.geosde.cassandra;

import java.io.IOException;

import org.geosde.core.data.ContentDataStore;
import org.geosde.core.data.ContentEntry;
import org.geosde.core.data.ContentFeatureSource;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.Session;

public class CassandraFeatureSource extends ContentFeatureSource {

	
	Session session;
	/**
	 * Creates the new feature store.
	 * 
	 * @param entry
	 *            The datastore entry.
	 * @param query
	 *            The defining query.
	 */
	public CassandraFeatureSource(Session session, ContentEntry entry) {
		this(session, entry, Query.ALL);

	}

	public CassandraFeatureSource(Session session, ContentEntry entry, Query query) {
		super(entry, query);
		this.session = session;
	}

	/**
	 * Builds the feature type from database metadata.
	 */
	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		return getDataStore().getSchema(entry.getName());
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {

		return null;
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {

		return 0;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {

		return new CassandraFeatureReader(query);
	}
	
    @Override
    public CassandraDataStore getDataStore() {
        return (CassandraDataStore) super.getDataStore();
    }

}
