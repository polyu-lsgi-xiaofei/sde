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

import java.util.List;

import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * This request is sent by a node upon joining the network, in order to get 
 * updates on the currently deployed process bundles.
 * 
 * @author Michael Pantazoglou
 *
 */
public class GetDeployedProcessBundlesUpdateRequest extends P2PRequest {

	private static final long serialVersionUID = 201205031458L;
	
	/**
	 * The p2p endpoint of the requester node.
	 */
	private P2PEndpoint requester;
	
	/**
	 * The list of process bundles that are currently deployed on the requester.
	 */
	private List<String> deployedProcessBundles;
	
	/**
	 * Constructor.
	 */
	public GetDeployedProcessBundlesUpdateRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new GetDeployedProcessBundlesUpdateService();
	}

	public List<String> getDeployedProcessBundles() {
		return deployedProcessBundles;
	}

	public void setDeployedProcessBundles(List<String> deployedProcessBundles) {
		this.deployedProcessBundles = deployedProcessBundles;
	}

	public P2PEndpoint getRequester() {
		return requester;
	}

	public void setRequester(P2PEndpoint requester) {
		this.requester = requester;
	}

}
