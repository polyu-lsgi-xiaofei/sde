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

import org.geosde.compute.hypercube.HypercubeService;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * This service updates the timestamp of last use of the specified neighbor.
 * 
 * @author Michael Pantazoglou
 *
 */
public class UpdateNeighborLastUsedService extends HypercubeService {
	/**
	 * The request message that triggered this service.
	 */
	private UpdateNeighborLastUsedRequest request;
	
	/**
	 * Constructor.
	 */
	public UpdateNeighborLastUsedService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (UpdateNeighborLastUsedRequest) req;
	}

	@Override
	public void execute() {
		Neighbor n = me.getNeighbor(request.getNeighborPosition());
		n.setLastUsed(request.getLastUsed());
	}

	@Override
	public boolean isRequestResponse() {
		return false;
	}

	@Override
	public P2PResponse getResponse() {
		return null;
	}

}
