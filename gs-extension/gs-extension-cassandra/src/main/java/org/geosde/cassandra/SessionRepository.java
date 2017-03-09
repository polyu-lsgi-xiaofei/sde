package org.geosde.cassandra;

import java.util.PropertyResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

public class SessionRepository {
	private static Session instance = null;
	private static Cluster cluster = null;
	private static Lock lock = new ReentrantLock();
	private static PropertyResourceBundle bundle;

	private SessionRepository() {
	}

	public static Session getSession() {
		if (null == instance) {
			try {
				lock.lock();

				if (null == instance) {

					// read configuration information from "config.properties"
					bundle = new PropertyResourceBundle(
							SessionRepository.class.getResourceAsStream("config.properties"));
					String[] hosts = bundle.getString("hosts").split(",");
					Builder builder = Cluster.builder();
					for (String host : hosts) {
						builder = builder.addContactPoint(host);
					}
					cluster = builder.build();
					instance = cluster.connect();

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	public static Metadata getMetadata() {
		return cluster.getMetadata();
	}
	

	public static void close() {
		if (null == cluster) {
			try {
				lock.lock();

				if (null == cluster) {
					cluster.close();
				}
			} finally {
				lock.unlock();
			}
		}
	}
}
