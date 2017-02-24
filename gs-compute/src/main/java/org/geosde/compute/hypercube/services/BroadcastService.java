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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.HypercubeService;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;
import org.geosde.compute.p2p.P2PService;

/**
 * This class implements the Hypercube broadcast service.
 * 
 * @author Michael Pantazoglou
 *
 */
public class BroadcastService extends HypercubeService {
	/**
	 * The request that triggered this service.
	 */
	private BroadcastRequest request;
	
	/**
	 * Constructor.
	 */
	public BroadcastService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (BroadcastRequest) req;
	}

	@Override
	public void execute() {
		
		// Process the actual service request in a separate thread
		new Thread(new Runnable() {
			
			public void run() {
				P2PRequest serviceRequest = request.getServiceRequest();
				P2PService p2pService = serviceRequest.createService();
				p2pService.setRequest(request.getServiceRequest());
				p2pService.execute();
			}
		}).start();		
		
		int minDimension = request.getMinDimension();
		me.getLog().debug("Broadcast received from neighbor in dimension " + 
				minDimension);
		if (minDimension == Hypercube.MAX_NUMBER_OF_DIMENSIONS - 1) {
			return;
		}
		
		// forward the broadcast request
		
		List<Integer> dimensionsCovered = new ArrayList<Integer>();
		Set<Neighbor> myNeighborSet = me.getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			String p_s = Hypercube.vectorAsString(n.getPositionVector());
			
			int linkDimensionality = Hypercube.getLinkDimensionality(
					me.getPositionVector(), n.getPositionVector());
			if (linkDimensionality > minDimension) {
				if (dimensionsCovered.contains(linkDimensionality)) {
					continue;
				}
				dimensionsCovered.add(linkDimensionality);
				P2PEndpoint p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(n.getNetworkAddress());
				request.setMinDimension(linkDimensionality);
				try {
					me.getLog().debug("Forwarding broadcast message to " + p_s);
					me.invokeOneWayService(p2pEndpoint, request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
