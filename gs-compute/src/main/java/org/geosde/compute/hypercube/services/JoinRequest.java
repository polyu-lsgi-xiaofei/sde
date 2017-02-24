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

import java.net.URI;

import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * This class implements the request message that is sent by a joining node 
 * to another node that is already integrated into the hypercube topology. 
 * The receiver node then will execute the {@link JoinService} in order to 
 * integrate the requester node into the hypercube.
 * 
 * @author Michael Pantazoglou
 *
 */
public class JoinRequest extends P2PRequest {

	private static final long serialVersionUID = 201204061547L;
	/**
	 * Network address of the joining node.
	 */
	private URI joiningNodeNetworkAddress;

	/**
	 * Constructor.
	 */
	public JoinRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new JoinService();
	}

	/**
	 * @return the joiningNodeNetworkAddress
	 */
	public URI getJoiningNodeNetworkAddress() {
		return joiningNodeNetworkAddress;
	}

	/**
	 * @param joiningNodeNetworkAddress the joiningNodeNetworkAddress to set
	 */
	public void setJoiningNodeNetworkAddress(URI joiningNodeNetworkAddress) {
		this.joiningNodeNetworkAddress = joiningNodeNetworkAddress;
	}

}
