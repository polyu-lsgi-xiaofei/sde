package org.geosde.cassandra.page;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

public class CassandraFeaturePage implements ICassandraPage<SimpleFeature> {

	public final static int RESULTS_PER_PAGE = 100;

	private Session session;
	private SimpleStatement statement = null;
	private PagingState currentState = null;
	private SimpleFeatureBuilder builder = null;
	private Envelope bbox = null;
	private WKBReader reader = new WKBReader();

	public CassandraFeaturePage(Session session, SimpleStatement statement, SimpleFeatureType sft, Envelope bbox) {
		this.session = session;
		this.statement = statement;
		this.bbox = bbox;
		this.builder = new SimpleFeatureBuilder(sft);
	}

	@Override
	public List<SimpleFeature> nextPage() {
		List<SimpleFeature> features = new ArrayList<>();
		statement.setFetchSize(RESULTS_PER_PAGE);
		// 第一页没有分页状态
		if (currentState != null) {
			statement.setPagingState(currentState);
		}
		ResultSet rs = session.execute(statement);
		currentState = rs.getExecutionInfo().getPagingState();
		int remaining = rs.getAvailableWithoutFetching();
		for (Row row : rs) {
			SimpleFeature feature = parse(row);
			if (feature != null)
				features.add(feature);
			if (--remaining == 0) {
				break;
			}
		}
		return features;
	}

	public boolean isFullyFetched() {
		if (currentState == null)
			return true;
		else
			return false;
	}

	@Override
	public SimpleFeature parse(Row row) {
		SimpleFeature feature = null;
		Geometry geometry = null;
		ByteBuffer buffer;
		buffer = row.getBytes("the_geom");
		String fid = row.getString("fid");
		builder.set("the_geom", geometry);
		try {
			geometry = reader.read(buffer.array());
			if (bbox.intersects(geometry.getEnvelopeInternal())) {
				feature = builder.buildFeature(fid);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return feature;
	}

}
