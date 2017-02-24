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
 * This class implements the ReadBPELVariable p2p service, which is used to 
 * get the current value of a specified BPEL variable in the context of a 
 * specified p2p session.
 * 
 * @author Michael Pantazoglou
 *
 */
public class ReadBPELVariableService extends BPELCubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private ReadBPELVariableRequest request;
	
	/**
	 * The response produced by this service.
	 */
	private ReadBPELVariableResponse response;
	
	/**
	 * Constructor.
	 */
	public ReadBPELVariableService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (ReadBPELVariableRequest) req;
	}

	@Override
	public void execute() {
		
		response = new ReadBPELVariableResponse();
		
		String p2pSessionId = request.getP2PSessionId();
		String variableId = request.getVariableId();
		
		me.getLog().debug("Reading variable: " + variableId);
		
		String variableValue = db.getVariableValue(p2pSessionId, variableId);
		
		response.setVariableValue(variableValue);
	}

	@Override
	public boolean isRequestResponse() {
		return true;
	}

	@Override
	public P2PResponse getResponse() {
		return response;
	}

}
