/* 
 * Copyright 2012 Michael Pantazoglou
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geosde.compute.p2p;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hsqldb.Server;

/**
 * Implements the node's data store.
 * 
 * @author Michael Pantazoglou
 *
 */
public abstract class P2PNodeDB {
	
	protected Log log; 
	
	/**
	 * The HSQLDB server.
	 */
	protected Server server;
	/**
	 * The JDBC connection.
	 */
	protected Connection connection;
	/**
	 * The home directory.
	 */
	protected String home;
	
	/**
	 * The node's database name.
	 */
	protected final String databaseName = "NODE_DB";

	protected P2PNodeDB(String home) {
		this.home = home;
		this.log = new Log(getClass(), P2PNode.sharedInstance.getLog().getLevel());
	}
	
	/**
	 * Executes the specified DDL SQL statement.
	 * 
	 * @param SQL
	 * @return
	 */
	protected boolean executeDDL(String SQL) {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(SQL);
			stmt.executeUpdate();
			return true;
		} catch (SQLException ex) {
			log.debug(ex.getMessage());
			return false;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	
	protected abstract void createTables();

	/**
	 * Opens the connection to the data store.
	 *
	 * @throws Exception
	 */
	public void openConnection() throws Exception {

		if (connection != null) {
			return;
		}

		log.info("Opening DB connection");

		// First, start the HSQLDB server

		if (server == null) {
			log.debug("Initializing DB server.");
			server = new Server();
			server.setLogWriter(null);
			server.setErrWriter(null);
			server.setSilent(true);
			server.setDatabaseName(0, databaseName);
			server.setDatabasePath(0, home + File.separator + databaseName);
			server.start();
		}

		// Open the JDBC connection

		Class.forName("org.hsqldb.jdbcDriver");
		String connectionURL = "jdbc:hsqldb:" + server.getDatabasePath(0, true);
		connection = DriverManager.getConnection(connectionURL);
		
		// Create the database tables depending on the node type
		createTables();
	}
	
	protected abstract void clear();

	/**
	 * Closes the connection to the data store.
	 *
	 * @throws Exception
	 */
	public void closeConnection() throws Exception {

		log.info("Closing DB connection");

		// Clear all tables
		clear();
		
		// Perform a clean SHUTDOWN

		PreparedStatement pstmt = connection.prepareStatement("SHUTDOWN;");
		pstmt.execute();
		pstmt.close();
		pstmt = null;

		// Close the JDBC connection

		connection.close();
		connection = null;

		// Stop the HSQLDB server

		if (server != null) {
			server.stop();
			server = null;
		}
		
//		Enumeration<Driver> registeredDrivers = DriverManager.getDrivers();
//		while (registeredDrivers.hasMoreElements()) {
//			Driver driver = registeredDrivers.nextElement();
//			log.debug("Deregistering JDBC driver.");
//			DriverManager.deregisterDriver(driver);
//		}
	}
	
}
