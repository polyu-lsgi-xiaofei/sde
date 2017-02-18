package org.geosde.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;

public class CassandraConnector {
	private Cluster cluster = null;

	public CassandraConnector() {
		cluster = Cluster.builder().addContactPoint("192.168.210.110").build();
	}

	public CassandraConnector(String[] hosts) {
		Builder builder = Cluster.builder();
		for (String host : hosts) {
			builder = builder.addContactPoint(host);
		}
		builder.build();
	}
	
	public Cluster getCluster(){
		return cluster;
	}

	public Session getSession() {
		return cluster.connect();
	}

	public void close() {
		cluster.close();
	}
}
