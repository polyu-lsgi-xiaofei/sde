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

import java.util.List;

import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.HypercubeService;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;
import org.geosde.compute.p2p.P2PService;

/**
 * Implements the hypercube's shortest-path routing algorithm.
 * 
 * @author Michael Pantazoglou
 *
 */
public class ShortestPathRouteService extends HypercubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private ShortestPathRouteRequest request;
	
	/**
	 * Constructor.
	 */
	public ShortestPathRouteService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (ShortestPathRouteRequest) req;
	}

	@Override
	public void execute() {
		
		int[] p_dest = request.getDestinationPositionVector();
		int[] p = me.getPositionVector();
		int[] c = me.getCoverMapVector();
		
		String s1 = Hypercube.vectorAsString(p_dest);
		List<int[]> coveredPositions = Hypercube.getCoveredPositions(p, c);
		for (int[] p_covered : coveredPositions) {
			String s2 = Hypercube.vectorAsString(p_covered);
			if (s1.equals(s2)) {
				// The destination position is taken by me, so I will stop 
				// routing and I will process the service request
				me.getLog().debug("I am the destination of shortest-path routing");
				new Thread(new Runnable() {
					
					public void run() {
						P2PRequest serviceRequest = request.getServiceRequest();
						P2PService p2pService = serviceRequest.createService();
						p2pService.setRequest(request.getServiceRequest());
						p2pService.execute();
					}
				}).start();	
				return;
			}
		}
		
		// The destination position is not taken by me, so I will find the next
		// node in the shortest-path
		Neighbor next = me.getNextNeighborInShortestPath(p_dest);
		if (next == null) {
			// This should not really happen!
			me.getLog().debug("Failed to resolve next neighbor in shortest path to position: " + s1);
			return;
		}
		
		try {
			me.getLog().debug("Next position in shortest path routing: " + Hypercube.vectorAsString(next.getPositionVector()) + "(" + next.getNetworkAddress() + ")");
			me.invokeOneWayService(next.asP2PEndpoint(), request);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
