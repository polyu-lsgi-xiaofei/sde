package org.geosde.example;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.geosde.cassandra.CassandraDataStore;
import org.geosde.cassandra.CassandraDataStoreFactory;
import org.geosde.cassandra.CassandraFeatureStore;
import org.geosde.core.data.ContentFeatureSource;
import org.geosde.shapefile.ShapefileDataStore;
import org.geosde.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.Filters;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;

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

		params.put(CassandraDataStoreFactory.DBTYPE.key, "cassandra");
		params.put(CassandraDataStoreFactory.HOST.key, "localhost");
		params.put(CassandraDataStoreFactory.KEYSPACE.key, "usa");
		params.put(CassandraDataStoreFactory.USER.key, "cassandra");
		params.put(CassandraDataStoreFactory.PASSWD.key, "cassandra");

		Iterator<DataStoreFactorySpi> iterators = DataStoreFinder.getAllDataStores();
		while (iterators.hasNext()) {
			DataStoreFactorySpi spi = iterators.next();
			if (spi.canProcess(params)) {
				System.out.println(spi);
			}
		}

		CassandraDataStoreFactory spi = new CassandraDataStoreFactory();
		CassandraDataStore store = (CassandraDataStore) spi.createDataStore(params);
		// store.setNamespaceURI("usa");
		SimpleFeatureSource featureSource = store.getFeatureSource(store.getTypeNames()[0]);
		SimpleFeatureType sft = featureSource.getSchema();
		System.out.println(sft);
	}

	public void testFeatureSource() throws Exception {
		long t0 = System.currentTimeMillis();
		CassandraDataStore datastore = new CassandraDataStore();
		datastore.setNamespaceURI("usa");
		String[] types = datastore.getTypeNames();
		for (String type : types) {
			System.out.println(type);
			SimpleFeatureSource featureSource = datastore.getFeatureSource(type);
			System.out.println(featureSource);
		}

	}

	public void testReader() throws Exception {
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
		List<Filter> match = new ArrayList<Filter>();
		Filter spatial = CQL.toFilter("BBOX(the_geom,-124.04,32.90,-113.34,42.36)");
		match.add(spatial);
		Filter temporal = CQL.toFilter("timestamp=2016110100");
		match.add(temporal);
		Filter filter = ff.and(match);// 属性空间联合查询
		Map<String, Serializable> params = new HashMap<>();
		CassandraDataStoreFactory spi = new CassandraDataStoreFactory();
		params.put(CassandraDataStoreFactory.DBTYPE.key, "cassandra");
		params.put(CassandraDataStoreFactory.HOST.key, "localhost");
		params.put(CassandraDataStoreFactory.KEYSPACE.key, "usa");
		params.put(CassandraDataStoreFactory.USER.key, "cassandra");
		params.put(CassandraDataStoreFactory.PASSWD.key, "cassandra");
		CassandraDataStore datastore = (CassandraDataStore) spi.createDataStore(params);
		datastore.setNamespaceURI("usa");
		String[] types = datastore.getTypeNames();
		ContentFeatureSource featureSource = datastore.getFeatureSource("gis_osm_pois_free_1_2016110100");
		Query query = new Query(featureSource.getSchema().getTypeName(), filter, new String[] {});
		System.out.println(featureSource);
		// featureSource.getReader(query);
		long t0 = System.currentTimeMillis();
		SimpleFeatureCollection features = featureSource.getFeatures(query);
		SimpleFeatureIterator iterator = features.features();
		System.out.println(System.currentTimeMillis() - t0+" ms");
		System.out.println("Begin to interate...");
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();
				// System.out.println(feature.getDefaultGeometry());
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				//System.out.println(feature.getID() + " default geometry " +geometry);
			}
		} catch (Throwable t) {
			iterator.close();
		}
		System.out.println("Finished...");
	}

	public void testQuery() {
		try {
			List<Filter> match = new ArrayList<Filter>();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
			// Filter filter1 =
			// CQL.toFilter("BBOX(the_geom,-125.04,32.90,-113.34,42.36)");
			Filter filter1 = CQL.toFilter("aaa=1");
			match.add(filter1);
			Filter filter2 = CQL.toFilter("bbb=2");
			match.add(filter2);
			Filter filter3 = CQL.toFilter("BBOX(the_geom,-125.04,32.90,-113.34,42.36)");
			match.add(filter3);
			Filter filter = ff.and(match);// 属性空间联合查询
			String typeName = "TEST_PG";
			Query query = new Query(typeName, filter);
			System.out.println(query.getFilter());
			Envelope bbox = new ReferencedEnvelope();
			if (query.getFilter() != null) {
				bbox = (Envelope) query.getFilter().accept(ExtractBoundsFilterVisitor.BOUNDS_VISITOR, bbox);
				if (bbox == null) {
					bbox = new ReferencedEnvelope();
				}
			}

			ArrayList<Filter> list = Filters.children(filter);
			System.out.println(Filters.propertyNames(filter));
			for (Filter f : list) {
				System.out.println(f.accept(new SimplifyingFilterVisitor(), null));
			}

			System.out.println(CQL.toCQL(filter));
		} catch (Exception e) {
			e.printStackTrace();
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

	private Set<String> getSortPropertyNames(SortBy[] sortBy) {
		Set<String> result = new HashSet<>();
		for (SortBy sort : sortBy) {
			PropertyName p = sort.getPropertyName();
			if (p != null && p.getPropertyName() != null) {
				result.add(p.getPropertyName());
			}
		}

		return result;
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

	public void testWriter() throws Exception {
		
		//Read shapefile
		ShapefileDataStoreFactory datasoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore sds = (ShapefileDataStore) datasoreFactory.createDataStore(
				new File("D:\\Data\\OSM\\california\\california-161201-free.shp\\gis.osm_pois_free_1.shp").toURI()
						.toURL());
		sds.setCharset(Charset.forName("GBK"));
		SimpleFeatureSource featureSource = sds.getFeatureSource();
		SimpleFeatureType featureType = featureSource.getFeatures().getSchema();
		SimpleFeatureCollection featureCollection = featureSource.getFeatures();
		
		//Connect Casssandra Datastore
		Map<String, Serializable> params = new HashMap<>();
		CassandraDataStoreFactory spi = new CassandraDataStoreFactory();
		params.put(CassandraDataStoreFactory.DBTYPE.key, "cassandra");
		params.put(CassandraDataStoreFactory.HOST.key, "localhost");
		params.put(CassandraDataStoreFactory.KEYSPACE.key, "usa");
		params.put(CassandraDataStoreFactory.USER.key, "cassandra");
		params.put(CassandraDataStoreFactory.PASSWD.key, "cassandra");
		CassandraDataStore datastore = (CassandraDataStore) spi.createDataStore(params);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
		Date date = formatter.parse("2016120100");
		datastore.createSchema(featureType,date);
		datastore.setNamespaceURI("usa");
		CassandraFeatureStore cfeatureSource = (CassandraFeatureStore) datastore
				.getFeatureSource("gis_osm_pois_free_1_2016120100");
		cfeatureSource.addFeatures(featureCollection);
		System.out.println("Finished!");
	}

	public static void main(String[] args) throws Exception {
		// System.out.println("558".compareTo("546"));
		// new CassandraTest().testReader();
		//new CassandraTest().testDataStore();
		//new CassandraTest().testWriter();
		new CassandraTest().testReader();
	}

}
