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
 *  
 * @author Michael Pantazoglou
 *
 */
public class UpdateNeighborLastUsedRequest extends P2PRequest {
	
	private static final long serialVersionUID = 201204162002L;
	/**
	 * The position vector of the sender.
	 */
	private int[] neighborPosition;
	/**
	 * The timestamp of last use of the sender.
	 */
	private long lastUsed;

	/**
	 * Constructor.
	 */
	public UpdateNeighborLastUsedRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new UpdateNeighborLastUsedService();
	}

	/**
	 * @return the neighborPosition
	 */
	public int[] getNeighborPosition() {
		return neighborPosition;
	}

	/**
	 * @param neighborPosition the neighborPosition to set
	 */
	public void setNeighborPosition(int[] neighborPosition) {
		this.neighborPosition = neighborPosition;
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

}
