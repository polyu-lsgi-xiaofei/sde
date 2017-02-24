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
package org.geosde.compute.hypercube.services;

import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * Implements the request message that is sent from one node to another during 
 * shortest-path routing.
 * 
 * @author Michael Pantazoglou
 *
 */
public class ShortestPathRouteRequest extends P2PRequest {

	private static final long serialVersionUID = 201205291219L;
	
	/**
	 * The position vector of the destination node.
	 */
	private int[] destinationPositionVector;
	
	/**
	 * The service request that the final destination node must process. It is 
	 * assumed that the corresponding service is one-way, i.e. request-only.
	 */
	private P2PRequest serviceRequest;
	
	/**
	 * Constructor.
	 */
	public ShortestPathRouteRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new ShortestPathRouteService();
	}
	
	public int[] getDestinationPositionVector() {
		return destinationPositionVector;
	}

	public void setDestinationPositionVector(int[] destinationPositionVector) {
		this.destinationPositionVector = destinationPositionVector;
	}

	public P2PRequest getServiceRequest() {
		return serviceRequest;
	}

	public void setServiceRequest(P2PRequest serviceRequest) {
		this.serviceRequest = serviceRequest;
	}

}
