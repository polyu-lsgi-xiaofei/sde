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
 * This message is sent by a node to another one asking to remove the specified 
 * neighbor.
 * 
 * @author Michael Pantazoglou
 *
 */
public class RemoveNeighborRequest extends P2PRequest {

	private static final long serialVersionUID = 201204111809L;
	/**
	 * Position vector of the neighbor to be removed.
	 */
	private int[] neighborPositionVector;
	
	/**
	 * Constructor.
	 */
	public RemoveNeighborRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new RemoveNeighborService();
	}

	/**
	 * @return the neighborPositionVector
	 */
	public int[] getNeighborPositionVector() {
		return neighborPositionVector;
	}

	/**
	 * @param neighborPositionVector the neighborPositionVector to set
	 */
	public void setNeighborPositionVector(int[] neighborPositionVector) {
		this.neighborPositionVector = neighborPositionVector;
	}

}
