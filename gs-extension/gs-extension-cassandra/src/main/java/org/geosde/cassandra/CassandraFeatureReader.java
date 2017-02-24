package org.geosde.cassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2RegionCoverer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

public class CassandraFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

	ExecutorService executorService = Executors.newFixedThreadPool(20);

	Query query;
	CQLDialect dialect;
	SimpleFeatureType sft;
	Envelope bbox;
	Session session;
	LoadingCache<String, SimpleFeature> featureCache = null;
	String datetime = "2017010100";
	SimpleFeature currentFeature;
	Iterator<SimpleFeature> itr;

	public CassandraFeatureReader(Session session, SimpleFeatureType sft, Query query) {
		this.sft = sft;
		this.query = query;
		this.session = CassandraConnector.getSession();
		featureCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(5000000L)
				.build(new CacheLoader<String, SimpleFeature>() {
					@Override
					public SimpleFeature load(String key) throws Exception {
						// TODO Auto-generated method stub
						return null;
					}
				});
		try {
			fetch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fetch() throws Exception {
		bbox = new ReferencedEnvelope();
		
		if (query.getFilter() != null) {
			bbox = (Envelope) query.getFilter().accept(ExtractBoundsFilterVisitor.BOUNDS_VISITOR, bbox);
			if (bbox == null) {
				bbox = new ReferencedEnvelope();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(bbox.getMinY() + ":" + bbox.getMinX() + ",");
		sb.append(bbox.getMinY() + ":" + bbox.getMaxX() + ",");
		sb.append(bbox.getMaxY() + ":" + bbox.getMaxX() + ",");
		sb.append(bbox.getMaxY() + ":" + bbox.getMinX() + ";");
		String polygon = sb.toString();

		S2RegionCoverer coverer = new S2RegionCoverer();
		coverer.setMaxCells(1);
		S2Polygon a = GeometryTestCase.makePolygon(polygon);
		List<String> quad_ids = new ArrayList<>();
		ArrayList<S2CellId> covering = new ArrayList<>();
		coverer.getCovering(a, covering);

		S2CellId cell = covering.get(0);
		int level = cell.level();
		for (int i = 1; i <= level; i++) {
			S2CellId parent = cell.parent(i);
			quad_ids.add(parent.toToken());
		}
		for (int i = level + 1; i <= 10; i++) {
			covering.clear();
			coverer.setMaxLevel(i);
			coverer.setMinLevel(i);
			coverer.getCovering(a, covering);
			for (S2CellId id : covering) {
				quad_ids.add(id.toToken());
			}

		}
		// System.out.println(quad_ids);
		System.out.println(quad_ids.size());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
		Date date = formatter.parse(datetime);
		String year_month = new SimpleDateFormat("yyyyMM").format(date);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(sft);
		session.execute("use usa");
		PreparedStatement statement = session.prepare(
				"select cell_id,epoch,the_geom,fid,timestamp from gis_osm_pois_free_1 where cell_id = ? and epoch=?;");

		List<QueryProcess> tasks = new ArrayList<>();
		int index = 0;
		for (String quad_id : quad_ids) {
			QueryProcess process = new QueryProcess(quad_id, session, featureCache, statement.bind(quad_id, year_month),
					builder, bbox);
			tasks.add(process);
		}
		executorService.invokeAll(tasks);
		System.out.println(featureCache.size());
		itr = featureCache.asMap().values().iterator();
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return sft;
	}

	@Override
	public boolean hasNext() throws IOException {

		return itr.hasNext();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		// TODO Auto-generated method stub
		currentFeature= itr.next();
		return currentFeature;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

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
				UUID fid = row.getUUID("fid");
				long time = row.getLong("timestamp");
				try {
					geometry = reader.read(buffer.array());
					if (!bbox.intersects(geometry.getEnvelopeInternal())) {
						continue;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				builder.set("the_geom", geometry);
				SimpleFeature feature = builder.buildFeature(fid.toString());
				featureCache.put(fid.toString(), feature);
			}

			return 0;
		}
	}

}
