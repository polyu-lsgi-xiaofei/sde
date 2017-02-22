package org.geosde.cassandra;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.geosde.core.index.IndexStrategy;
import org.geosde.core.index.S2IndexStrategy;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBWriter;

public class CassandraIngest {

	private Cluster cluster = null;

	public CassandraIngest() {
		cluster = Cluster.builder().addContactPoint("192.168.210.110").build();
		// cluster =
		// Cluster.builder().addContactPoint("192.168.200.248").build();
	}

	public void ingest() throws Exception {
		ShapefileDataStoreFactory datasoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore sds = (ShapefileDataStore) datasoreFactory.createDataStore(
				new File("E:\\Data\\OSM\\USA\\california\\california-170101-free.shp\\gis.osm_roads_free_1.shp")
						.toURI().toURL());
		sds.setCharset(Charset.forName("GBK"));
		SimpleFeatureSource featureSource = sds.getFeatureSource();
		SimpleFeatureType featureType = featureSource.getFeatures().getSchema();
		Session session = cluster.connect();
		session.execute("use usa;");
		CassandraDataStore datastore = new CassandraDataStore();
		datastore.setNamespaceURI("usa");
		
		String table_name = featureType.getName().toString().replace(".", "_");
		SimpleFeatureCollection featureCollection = featureSource.getFeatures();
		FeatureIterator<SimpleFeature> features = featureCollection.features();
		WKBWriter writer = new WKBWriter();
		BatchStatement bs = new BatchStatement();
		int count = 0;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
		Date date = formatter.parse("2017010100");
		datastore.createSchema(featureType,date);
		IndexStrategy indexStrategy = new S2IndexStrategy(featureType.getTypeName());
		Geometry geom;
		while (features.hasNext()) {
			SimpleFeature feature = features.next();
			if (featureType.getGeometryDescriptor().getType().getName().toString().equals("MultiPolygon")) {
				geom = (MultiPolygon) feature.getDefaultGeometry();
			} else if (featureType.getGeometryDescriptor().getType().getName().toString().equals("MultiLineString")) {
				geom = (MultiLineString) feature.getDefaultGeometry();
			} else {
				geom = (Point) feature.getDefaultGeometry();
			}

			ByteBuffer buf_geom = ByteBuffer.wrap(writer.write(geom));
			Map<String, Object> primaryKey = indexStrategy.index(geom, date.getTime());
			List<AttributeDescriptor> attrDes = featureType.getAttributeDescriptors();
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
					values.add(feature.getAttribute(col_name));
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
			// System.out.println(s);
			bs.add(s);
			count++;
			if (count == 20) {
				try {
					session.execute(bs);
					bs.clear();
					count = 0;
				} catch (Exception e) {
					for (Statement stat : bs.getStatements()) {
						while (true) {
							try {
								session.execute(stat);
								break;
							} catch (Exception e2) {
								System.out.print("-");
							}
						}
						System.out.print(".");
					}
					System.out.print("\n");
					bs.clear();
					count = 0;
				}

			}

		}
		long t0 = System.currentTimeMillis();
		System.out.println("Finish!");

		cluster.close();
	}

	public static void main(String[] args) throws Exception {
		new CassandraIngest().ingest();
	}

}
