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

import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class RecruitCompleteRequest extends P2PRequest {

	private static final long serialVersionUID = 201204231724L;
	/**
	 * Id of the P2P session for which recruitment is complete.
	 */
	private String p2pSessionId;
	
	/**
	 * The p2p endpoint of the sender of this request.
	 */
	private P2PEndpoint senderEndpoint;
	
	/**
	 * Constructor.
	 */
	public RecruitCompleteRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new RecruitCompleteService();
	}

	public String getP2PSessionId() {
		return p2pSessionId;
	}
	
	public void setP2PSessionId(String p2pSessionId) {
		this.p2pSessionId = p2pSessionId;
	}

	public P2PEndpoint getSenderEndpoint() {
		return senderEndpoint;
	}

	public void setSenderEndpoint(P2PEndpoint senderEndpoint) {
		this.senderEndpoint = senderEndpoint;
	}

}
