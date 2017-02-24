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

import java.io.EOFException;
import java.util.concurrent.SynchronousQueue;


/**
 * This class is used to handle incoming p2p connections, i.e. connections
 * created by a p2p connection listener, by means of the appropriate p2p
 * service.
 * 
 * @author Michael Pantazoglou
 */
public class P2PConnectionHandler implements Runnable {
	
	protected final Log log = P2PNode.sharedInstance.getLog();

	protected P2PConnection connection;

	/**
	 * Constructor.
	 *
	 * @param conn the p2p connection to handle
	 */
	public P2PConnectionHandler(P2PConnection conn) {
		connection = conn;
	}

	/**
	 * Handles the connection in the following way: First, the p2p message is
	 * received. If it is a p2p response, the correlation id is extracted and 
	 * the response is pushed to the appropriate correlator. Otherwise, it is used 
	 * to create an instance of the corresponding service. Next, the service is 
	 * executed based on the request, and, if a response is required, it is sent 
	 * back through the connection. 
	 */
	protected void handle() {
		try {
			P2PMessage message = connection.receive();
			
			if (message instanceof P2PResponse) {
				P2PResponse response = (P2PResponse) message;
				String correlationId = response.getCorrelationId();
				if (correlationId != null) {
					SynchronousQueue<P2PResponse> correlator = 
							P2PNode.sharedInstance.removeCorrelator(correlationId);
					if (correlator != null) {
						correlator.put(response);
					}
				}
				return;
			}
			
			if (!(message instanceof P2PRequest)) {
				log.error("Invalid request");
				return;
			}
			P2PService service = ((P2PRequest)message).createService();
			if (service == null) {
				log.error("Unknown request");
				return;
			}
			log.debug("Executing service " + service.getClass().getSimpleName());
			service.setRequest((P2PRequest)message);
			service.execute();
			if (service.isRequestResponse()) {
				P2PResponse response = service.getResponse();
				connection.send(response);
			}
		} catch (InterruptedException e) {
			return;
		} catch (Exception e) {
			if (!(e instanceof EOFException)) {
				e.printStackTrace();
			}
		} finally {
			try {
//				log.debug("Closing the P2P connection.");
				connection.close();
				connection = null;
//				log.debug("P2P Connection closed.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the p2p connection handler.
	 */
	public void run() {
		handle();
	}

}
