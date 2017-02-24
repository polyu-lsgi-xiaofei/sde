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
package org.geosde.compute.service;

import org.geosde.compute.BPELCubeNode.Role;
import org.geosde.compute.BPELCubeService;
import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.hypercube.services.UpdateNeighborLastUsedRequest;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class RecruitService extends BPELCubeService {
	/**
	 * The request that triggered this service.
	 */
	private RecruitRequest request;
	
	/**
	 * Constructor.
	 */
	public RecruitService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (RecruitRequest) req;
	}

	@Override
	public void execute() {
		
		if (!db.p2pSessionExists(request.getP2PSessionId())) {
			
			db.addP2PSession(request.getP2PSessionId(), Role.WORKER.toString(),
					request.getP2PSessionCreationTime(), null);
			
			// invoke the BPEL process locally
			me.getLog().debug("[RecruitService] Invoking BPEL process...");
			/*
			GenericWebServiceClient wsClient = new GenericWebServiceClient();
			wsClient.invoke(request.getProcessEndpointAddress(), 
					request.getProcessSOAPRequest(), 
					null, 
					request.getP2PSessionId(), 
					me.getBPELEnginePort());
					*/
		}
		
		String activityId = request.getActivityIds().remove(0);
		db.addP2PSessionActivity(request.getP2PSessionId(), activityId);
		if (!db.p2pSessionNeighborExists(request.getP2PSessionId(), 
				request.getRequesterEndpoint().toString())) {
			db.addP2PSessionNeighor(request.getP2PSessionId(), 
				request.getRequesterEndpoint().toString());
		}		
		
		me.setLastUsed(System.currentTimeMillis());
		for (Neighbor n : me.getNeighborSet()) {
			UpdateNeighborLastUsedRequest updateNeighborLastUsedRequest = 
					new UpdateNeighborLastUsedRequest();
			updateNeighborLastUsedRequest.setNeighborPosition(me.getPositionVector());
			updateNeighborLastUsedRequest.setLastUsed(me.getLastUsed());
			try {
				me.invokeOneWayService(n.asP2PEndpoint(), updateNeighborLastUsedRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (!request.getActivityIds().isEmpty()) {
			// forward the recruit request to the LRU node
			request.setRequesterEndpoint(me.getEndpoint());
			try {
				Neighbor LRU = me.getLRUNeighbor();
				me.getLog().debug("LRU neighbor: " + Hypercube.vectorAsString(LRU.getPositionVector()));
				if (!db.p2pSessionNeighborExists(request.getP2PSessionId(), 
						LRU.asP2PEndpoint().toString())) {
					db.addP2PSessionNeighor(request.getP2PSessionId(), 
							LRU.asP2PEndpoint().toString());
				}
				me.invokeOneWayService(LRU.asP2PEndpoint(), request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// notify the manager of the P2P session that recruitment is complete
			RecruitCompleteRequest recruitCompleteRequest = new RecruitCompleteRequest();
			recruitCompleteRequest.setP2PSessionId(request.getP2PSessionId());
			recruitCompleteRequest.setSenderEndpoint(me.getEndpoint());
			try {
				me.invokeOneWayService(request.getManagerEndpoint(), recruitCompleteRequest);
			} catch (Exception e) {
				e.printStackTrace();
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
