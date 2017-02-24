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
 * Implements the request message that is sent by one node to another upon 
 * broadcasting.
 * 
 * @author Michael Pantazoglou
 *
 */
public class BroadcastRequest extends P2PRequest {
	
	private static final long serialVersionUID = 201204091622L;
	/**
	 * The minimum dimension of the requested broadcast. Its value determines 
	 * the follow-up nodes of this broadcast.
	 */
	private int minDimension = 0;
	/**
	 * The service request that each recipient node must process. It is assumed
	 * that the corresponding service is one-way, i.e. request-only.
	 */
	private P2PRequest serviceRequest;
	/**
	 * Constructor.
	 */
	public BroadcastRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new BroadcastService();
	}

	/**
	 * @return the minDimension
	 */
	public int getMinDimension() {
		return minDimension;
	}

	/**
	 * @param minDimension the minDimension to set
	 */
	public void setMinDimension(int minDimension) {
		this.minDimension = minDimension;
	}

	/**
	 * @return the serviceRequest
	 */
	public P2PRequest getServiceRequest() {
		return serviceRequest;
	}

	/**
	 * @param serviceRequest the serviceRequest to set
	 */
	public void setServiceRequest(P2PRequest serviceRequest) {
		this.serviceRequest = serviceRequest;
	}

}
