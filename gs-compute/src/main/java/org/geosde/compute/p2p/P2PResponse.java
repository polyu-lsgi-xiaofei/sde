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
package org.geosde.compute.p2p;

/**
 * Defines a convenient abstraction for all p2p <b>response</b> messages.
 * 
 * @author Michael Pantazoglou
 *
 */
public abstract class P2PResponse extends P2PMessage {

	private static final long serialVersionUID = -2979715171809798380L;
	
	private static final String RESPONDER = "Responder";

	protected P2PResponse() {
		super();
		addHeaderElement(RESPONDER, P2PNode.sharedInstance.getEndpoint());
	}
	
	/**
	 * Correlates this p2p response with the specified p2p request. This should
	 * be used in cases of asynchronous, two-way communication between two nodes.
	 * 
	 * @param p2pRequest the correlated p2p request
	 */
	public void correlateWith(P2PRequest p2pRequest) {
		addHeaderElement(CORRELATION_ID, p2pRequest.getHeader().get(CORRELATION_ID));
	}
	
	public P2PEndpoint getResponder() {
		return (P2PEndpoint) header.get(RESPONDER);
	}

}
