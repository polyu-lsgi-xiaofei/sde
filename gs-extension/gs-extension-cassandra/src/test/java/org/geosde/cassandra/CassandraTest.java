package org.geosde.cassandra;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.base.Splitter;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

public class CassandraTest {

	public void testDataStore() throws Exception {
		Map<String, Serializable> params = new HashMap<>();
		DataStore store = DataStoreFinder.getDataStore(params);
		System.out.println(store);
		SimpleFeatureSource featureSource = store.getFeatureSource(store.getTypeNames()[0]);
	}

	public void testFeatureSource() throws Exception {
		long t0 = System.currentTimeMillis();
		CassandraDataStore datastore = new CassandraDataStore();
		datastore.setCatalog_name("usa");
		datastore.setNamespaceURI("usa");
		String[] types = datastore.getTypeNames();
		for (String type : types) {
			SimpleFeatureSource featureSource = datastore.getFeatureSource(type);
			System.out.println(featureSource.getSchema());
			System.out.println(featureSource.getDataStore());
		}

	}

	public void testReader() throws Exception {
		Filter filter = CQL.toFilter("BBOX(the_geom,-125.04,32.90,-113.34,42.36)");

		CassandraDataStore datastore = new CassandraDataStore();
		datastore.setCatalog_name("usa");
		datastore.setNamespaceURI("usa");
		String[] types = datastore.getTypeNames();
		for (String type : types) {
			System.out.println(type);
		}
		CassandraFeatureSource featureSource = (CassandraFeatureSource) datastore
				.getFeatureSource("gis.osm_pois_free_1");
		Query query = new Query(featureSource.getSchema().getTypeName(), filter, new String[] {});
		// featureSource.getReader(query);

		SimpleFeatureCollection features = featureSource.getFeatures(query);
		SimpleFeatureIterator iterator = features.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				System.out.println(feature.getDefaultGeometry());
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				//System.out.println(feature.getID() + " default geometry " + geometry);
			}
		} catch (Throwable t) {
			iterator.close();
		}

		
	}

	class QueryProcess implements Callable<Integer> {

		LoadingCache<String, SimpleFeature> queue;
		Session session;
		String datetime;
		Geometry geometry;
		Statement statement;
		ByteBuffer buffer;
		SimpleFeatureBuilder builder;
		WKBReader reader = new WKBReader();
		Envelope bbox;
		String index;

		public QueryProcess(String index, Session session, LoadingCache<String, SimpleFeature> queue,
				Statement statement, SimpleFeatureBuilder builder, Envelope bbox) {
			this.index = index;
			this.queue = queue;
			this.session = session;
			this.statement = statement;
			this.builder = builder;
			this.bbox = bbox;
		}

		@Override
		public Integer call() {
			// System.out.println(statement);
			ResultSet rs = session.execute(statement);
			for (Row row : rs) {
				// System.out.println(row);
				buffer = row.getBytes("the_geom");
				String fid = row.getString("fid");
				long time = row.getLong("timestamp");
				try {
					geometry = reader.read(buffer.array());
					System.out.println(geometry);
					if (!bbox.intersects(geometry.getEnvelopeInternal())) {
						continue;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				builder.set("the_geom", geometry);
				SimpleFeature feature = builder.buildFeature(fid);

			}

			return 0;
		}
	}

	private S2Polygon makePolygon(String str) {
		List<S2Loop> loops = Lists.newArrayList();

		for (String token : Splitter.on(';').omitEmptyStrings().split(str)) {
			S2Loop loop = makeLoop(token);
			loop.normalize();
			loops.add(loop);
		}

		return new S2Polygon(loops);
	}

	private S2Loop makeLoop(String str) {
		List<S2Point> vertices = Lists.newArrayList();
		parseVertices(str, vertices);
		return new S2Loop(vertices);
	}

	private void parseVertices(String str, List<S2Point> vertices) {
		if (str == null) {
			return;
		}

		for (String token : Splitter.on(',').split(str)) {
			int colon = token.indexOf(':');
			if (colon == -1) {
				throw new IllegalArgumentException("Illegal string:" + token + ". Should look like '35:20'");
			}
			double lat = Double.parseDouble(token.substring(0, colon));
			double lng = Double.parseDouble(token.substring(colon + 1));
			vertices.add(S2LatLng.fromDegrees(lat, lng).toPoint());
		}
	}

	public static void main(String[] args) throws Exception {
		// System.out.println("558".compareTo("546"));
		new CassandraTest().testReader();
	}

}
