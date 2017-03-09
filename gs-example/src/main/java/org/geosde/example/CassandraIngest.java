package org.geosde.example;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geosde.cassandra.CassandraDataStore;
import org.geosde.core.index.IndexStrategy;
import org.geosde.core.index.S2IndexStrategy;
import org.geosde.shapefile.ShapefileDataStore;
import org.geosde.shapefile.ShapefileDataStoreFactory;
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
		cluster = Cluster.builder().addContactPoint("localhost").build();
		// cluster =
		// Cluster.builder().addContactPoint("192.168.200.248").build();
	}

	public void createTable() throws Exception {
		ShapefileDataStoreFactory datasoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore sds = (ShapefileDataStore) datasoreFactory.createDataStore(
				new File("D:\\Data\\OSM\\california\\california-161001-free.shp\\gis.osm_pois_free_1.shp").toURI()
						.toURL());
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
		Date date = formatter.parse("2016100100");
		datastore.createSchema(featureType, date);
	}

	public void ingest() throws Exception {
		ShapefileDataStoreFactory datasoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore sds = (ShapefileDataStore) datasoreFactory.createDataStore(
				new File("D:\\Data\\OSM\\california\\california-161001-free.shp\\gis.osm_pois_free_1.shp").toURI()
						.toURL());
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
		Date date = formatter.parse("2016100100");
		table_name += "_2016100100";
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
			Map<String, Object> primaryKey = indexStrategy.index(geom);
			List<AttributeDescriptor> attrDes = featureType.getAttributeDescriptors();
			List<String> col_items = new ArrayList<>();
			Map<String, Object> values = new HashMap<>();
			values.put("cell", primaryKey.get("cell"));
			values.put("pos", primaryKey.get("pos"));

			Object fid = null;
			for (AttributeDescriptor attr : attrDes) {
				if (attr instanceof GeometryDescriptor) {
					String col_name = attr.getLocalName();
					col_items.add(col_name);
					values.put(col_name, buf_geom);
				} else {
					String col_name = attr.getLocalName();
					Class type = attr.getType().getBinding();
					col_items.add(col_name);
					values.put(col_name, feature.getAttribute(col_name));
					if (col_name.equals("osm_id"))
						values.put("fid", feature.getAttribute(col_name));

				}

			}
			String params = "";
			
			StringBuilder builder=new StringBuilder();
			List<Object> list=new ArrayList<>();
			for(String name:values.keySet()){
				builder.append(name+",");
				list.add(values.get(name));
				params += "?,";
			}
			String items=builder.toString().substring(0, builder.toString().length()-1);
			SimpleStatement s = new SimpleStatement(
					"INSERT INTO " + table_name + " ("+items+") values (" + params.substring(0,params.length()-1) + ");",
					list.toArray());
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
		//new CassandraIngest().createTable();
	}

}
