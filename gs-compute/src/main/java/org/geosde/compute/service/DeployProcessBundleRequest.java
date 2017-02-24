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

import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * This class implements the request of the DeployProcessBundle p2p service.
 * 
 * @author Michael Pantazoglou
 *
 */
public class DeployProcessBundleRequest extends P2PRequest {

	private static final long serialVersionUID = 201204301138L;
	
	/**
	 * The name of the BPEL process bundle to be deployed.
	 */
	private String bundleName;
	
	/**
	 * The content of the BPEL process <b>zip</b> bundle to be deployed.
	 */
	private byte[] bundleContent;
	
	/**
	 * Constructor.
	 */
	public DeployProcessBundleRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new DeployProcessBundleService();
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public byte[] getBundleContent() {
		return bundleContent;
	}

	public void setBundleContent(byte[] bundleContent) {
		this.bundleContent = bundleContent;
	}

}
