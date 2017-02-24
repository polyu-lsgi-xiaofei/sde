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

import org.geosde.compute.BPELCubeService;
import org.geosde.compute.BPELProcessExecutionListener;
import org.geosde.compute.BPELProcessExecutionNotifier;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class RecruitCompleteService extends BPELCubeService {
	/**
	 * The request that triggered this service.
	 */
	private RecruitCompleteRequest request;
	
	/**
	 * Constructor.
	 */
	public RecruitCompleteService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (RecruitCompleteRequest) req;
	}

	@Override
	public void execute() {
		
		// Persist sender endpoint in p2p session neighbours
		String p2pSessionId = request.getP2PSessionId();
		String senderEndpoint = request.getSenderEndpoint().toString();
		
		if (!db.p2pSessionNeighborExists(p2pSessionId, senderEndpoint)) {
			db.addP2PSessionNeighor(p2pSessionId, senderEndpoint);
		}
		
		// Notify the BPEL process execution listener that the recruitment is complete.
		me.getLog().debug("Notifying the process execution listener of P2P session: " + p2pSessionId);
		
		BPELProcessExecutionListener<P2PRequest> processExecutionListener = null;
		
		while (true) {
			
			processExecutionListener = me.removeProcessExecutionListener(p2pSessionId);
			if (processExecutionListener == null) {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
				
		BPELProcessExecutionNotifier<P2PRequest> processExecutionNotifier = 
				new BPELProcessExecutionNotifier<P2PRequest>(
						processExecutionListener.getQueue());
		
		try {
			processExecutionNotifier.notify(request);
		} catch (InterruptedException e) {
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
