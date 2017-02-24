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
package org.geosde.compute.hypercube;

import java.io.Serializable;
import java.net.URI;

import org.geosde.compute.p2p.P2PEndpoint;

/**
 * This class implements the neighbor of a hypercube node.
 * 
 * @author Michael Pantazoglou
 *
 */
public class Neighbor implements Serializable {
	
	private static final long serialVersionUID = 201204041225L;
	
	/**
	 * The position vector of this neighbor.
	 */
	private int[] positionVector;
	/**
	 * The network address of this neighbor.
	 */
	private URI networkAddress;
	/**
	 * The timestamp (in milliseconds) of the last time this neighbor was used.
	 */
	private long lastUsed;
	
	/**
	 * Constructor.
	 */
	public Neighbor() {
		super();
	}
	
	/**
	 * @return the positionVector
	 */
	public int[] getPositionVector() {
		return positionVector;
	}

	/**
	 * @param positionVector the positionVector to set
	 */
	public void setPositionVector(int[] positionVector) {
		this.positionVector = positionVector;
	}

	/**
	 * @return the networkAddress
	 */
	public URI getNetworkAddress() {
		return networkAddress;
	}

	/**
	 * @param networkAddress the networkAddress to set
	 */
	public void setNetworkAddress(URI networkAddress) {
		this.networkAddress = networkAddress;
	}

	/**
	 * @return the lastUsed
	 */
	public long getLastUsed() {
		return lastUsed;
	}

	/**
	 * @param lastUsed the lastUsed to set
	 */
	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}
	
	/**
	 * Returns a P2PEndpoint that corresponds to this neighbor.
	 * 
	 * @return
	 */
	public P2PEndpoint asP2PEndpoint() {
		P2PEndpoint p2pEndpoint = new P2PEndpoint();
		p2pEndpoint.setAddress(networkAddress);
		return p2pEndpoint;
	}

}
