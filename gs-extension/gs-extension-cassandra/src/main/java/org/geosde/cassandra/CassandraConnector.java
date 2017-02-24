package org.geosde.cassandra;

import java.util.PropertyResourceBundle;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;

/**
 * This class applies the Singleton pattern to improve the Cassandra connection
 **/
public class CassandraConnector {

	private static Cluster cluster = null;
	private static PropertyResourceBundle bundle;

	static {
		try {
			// read configuration information from "config.properties"
			bundle = new PropertyResourceBundle(CassandraConnector.class.getResourceAsStream("config.properties"));
			String[] hosts = bundle.getString("hosts").split(",");
			Builder builder = Cluster.builder();
			for (String host : hosts) {
				builder = builder.addContactPoint(host);
			}
			cluster = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Cluster getCluster() {
		return cluster;
	}

	public static Session getSession() {
		return cluster.connect();
	}

	public static void close() {
		cluster.close();
	}
}
