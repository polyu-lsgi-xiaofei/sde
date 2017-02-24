package org.geosde.cassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.geosde.core.index.IndexStrategy;
import org.geosde.core.index.S2IndexStrategy;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBWriter;

public class CassandraInsertFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

	private final static int BATCH_SIZE = 20;
	private SimpleFeature currentFeature;
	private BatchStatement bs;
	private int curBufferPos = 0;
	private SimpleFeatureType sft = null;
	// an array for reuse in Feature creation
	protected Object[] emptyAtts;

	protected List<SimpleFeature> featurelist;
	IndexStrategy indexStrategy = null;
	Date date;
	WKBWriter writer = new WKBWriter();
	String table_name;
	Session session;

	public CassandraInsertFeatureWriter(SimpleFeatureType sft, Date date, String table_name, Session session) {
		this.sft = sft;
		indexStrategy = new S2IndexStrategy(sft.getTypeName());
		bs = new BatchStatement();
		featurelist = new ArrayList<>();
		this.date = date;
		this.table_name = table_name;
		this.session = session;
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
			Geometry geom;

			for (SimpleFeature cur : featurelist) {
				// the datastore sets as userData, grab it and update the fid
				if (sft.getGeometryDescriptor().getType().getName().toString().equals("MultiPolygon")) {
					geom = (MultiPolygon) cur.getDefaultGeometry();
				} else if (sft.getGeometryDescriptor().getType().getName().toString().equals("MultiLineString")) {
					geom = (MultiLineString) cur.getDefaultGeometry();
				} else {
					geom = (Point) cur.getDefaultGeometry();
				}

				ByteBuffer buf_geom = ByteBuffer.wrap(writer.write(geom));
				Map<String, Object> primaryKey = indexStrategy.index(geom, date.getTime());
				List<AttributeDescriptor> attrDes = sft.getAttributeDescriptors();
				List<String> col_items = new ArrayList<>();
				List<Object> values = new ArrayList<>();

				values.add(primaryKey.get("cell_id"));
				values.add(primaryKey.get("epoch"));
				values.add(primaryKey.get("pos"));
				values.add(primaryKey.get("timestamp"));
				values.add(primaryKey.get("fid"));

				for (AttributeDescriptor attr : attrDes) {
					if (attr instanceof GeometryDescriptor) {
						String col_name = attr.getLocalName();
						Class type = attr.getType().getBinding();
						col_items.add(col_name);
						values.add(buf_geom);
					} else {
						String col_name = attr.getLocalName();
						Class type = attr.getType().getBinding();
						col_items.add(col_name);
						values.add(cur.getAttribute(col_name));
					}

				}
				String cols = "";
				String params = "";
				for (int i = 0; i < col_items.size() - 1; i++) {
					cols += col_items.get(i) + ",";
					params += "?,";
				}
				cols += col_items.get(col_items.size() - 1);
				params += "?";
				SimpleStatement s = new SimpleStatement("INSERT INTO " + table_name
						+ " (cell_id, epoch, pos,timestamp,fid, " + cols + ") values (?,?,?,?,?," + params + ");",
						values.toArray());
				//System.out.println(s);
				bs.add(s);
			}
			session.execute(bs);
			bs.clear();
			curBufferPos = 0;
			featurelist.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
