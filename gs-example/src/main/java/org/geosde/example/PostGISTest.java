package org.geosde.example;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.geosde.core.index.IndexStrategy;
import org.geosde.core.index.S2IndexStrategy;
import org.geosde.core.jdbc.JDBCDataStore;
import org.geosde.core.jdbc.JDBCFeatureStore;
import org.geosde.jdbc.postgis.PostgisNGDataStoreFactory;
import org.geosde.shapefile.ShapefileDataStore;
import org.geosde.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.datastax.driver.core.BatchStatement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBWriter;

public class PostGISTest {

	public PostGISTest() {
		// TODO Auto-generated constructor stub
	}

	public void testRead() {
		// get the params
		final PostgisNGDataStoreFactory spi = new PostgisNGDataStoreFactory();
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(PostgisNGDataStoreFactory.PORT.key, 5432);
		params.put(PostgisNGDataStoreFactory.HOST.key, "localhost");
		params.put(PostgisNGDataStoreFactory.SCHEMA.key, "public");
		params.put(PostgisNGDataStoreFactory.DATABASE.key, "japan");
		params.put(PostgisNGDataStoreFactory.LOOSEBBOX.key, true);
		params.put(PostgisNGDataStoreFactory.USER.key, "postgres");
		params.put(PostgisNGDataStoreFactory.PASSWD.key, "869222");

		// create schema
		try {
			final JDBCDataStore datastore = spi.createDataStore(params);
			System.out.println(datastore);
			JDBCFeatureStore featureSource =(JDBCFeatureStore)datastore.getFeatureSource("gis.osm_pois_free_1");
			SimpleFeatureCollection features = featureSource.getFeatures();
			SimpleFeatureIterator iterator = features.features();
			try {
				while (iterator.hasNext()) {
					SimpleFeature feature = iterator.next();
					System.out.println(feature.getDefaultGeometry());
					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					System.out.println(feature.getID() + " default geometry " + geometry);
				}
			} catch (Throwable t) {
				iterator.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void testWrite() {
		// get the params
		final PostgisNGDataStoreFactory spi = new PostgisNGDataStoreFactory();
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(PostgisNGDataStoreFactory.PORT.key, 5432);
		params.put(PostgisNGDataStoreFactory.HOST.key, "localhost");
		params.put(PostgisNGDataStoreFactory.SCHEMA.key, "public");
		params.put(PostgisNGDataStoreFactory.DATABASE.key, "japan");
		params.put(PostgisNGDataStoreFactory.LOOSEBBOX.key, true);
		params.put(PostgisNGDataStoreFactory.USER.key, "postgres");
		params.put(PostgisNGDataStoreFactory.PASSWD.key, "869222");

		// create schema
		try {
			final JDBCDataStore datastore = spi.createDataStore(params);
			System.out.println(datastore);
			ShapefileDataStoreFactory datasoreFactory = new ShapefileDataStoreFactory();
			ShapefileDataStore sds = (ShapefileDataStore) datasoreFactory.createDataStore(
					new File("E:\\Data\\OSM\\USA\\california\\california-170101-free.shp\\gis.osm_landuse_a_free_1.shp")
							.toURI().toURL());
			sds.setCharset(Charset.forName("GBK"));
			SimpleFeatureSource featureSource = sds.getFeatureSource();
			SimpleFeatureType featureType = featureSource.getFeatures().getSchema();
			//datastore.createSchema(featureType);
			
			JDBCFeatureStore jdbcFeatureSource =(JDBCFeatureStore)datastore.getFeatureSource("gis.osm_landuse_a_free_1");
			SimpleFeatureCollection featureCollection = featureSource.getFeatures();
			jdbcFeatureSource.addFeatures(featureCollection);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new PostGISTest().testWrite();
	}

}