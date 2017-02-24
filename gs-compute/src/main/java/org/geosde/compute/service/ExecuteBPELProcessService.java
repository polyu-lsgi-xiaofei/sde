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
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class ExecuteBPELProcessService extends BPELCubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private ExecuteBPELProcessRequest request;
	
	/**
	 * Constructor.
	 */
	public ExecuteBPELProcessService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (ExecuteBPELProcessRequest) req;
	}

	@Override
	public void execute() {
		me.getLog().debug("[ExecuteBPELProcessService] Invoking BPEL process...");
		
		/*
		GenericWebServiceClient wsClient = new GenericWebServiceClient();
		wsClient.invoke(request.getProcessEndpointAddress(), 
				request.getProcessSOAPRequest(), 
				null, 
				null, // p2p session id is null, because this will be the first actual invocation
				me.getBPELEnginePort());
				*/
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
