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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.geosde.compute.BPELCubeService;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * This service is executed in order to update the currently deployed bundles 
 * on the requester's side.
 * 
 * @author Michael Pantazoglou
 *
 */
public class GetDeployedProcessBundlesUpdateService extends BPELCubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private GetDeployedProcessBundlesUpdateRequest request;
	
	/**
	 * Constructor.
	 */
	public GetDeployedProcessBundlesUpdateService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (GetDeployedProcessBundlesUpdateRequest) req;
	}
	
	private byte[] getBytesFromFile(File file) throws IOException {
		
		InputStream is = new FileInputStream(file);
		
		long length = file.length();
		
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		
		byte[] bytes = new byte[(int)length];
		
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		
		is.close();
		return bytes;
	}

	@Override
	public void execute() {
		
		List<String> aList = request.getDeployedProcessBundles();
		List<String> bList = me.getCurrentlyDeployedProcessBundles();
		
		// find out which elements in aList are NOT included in bList
		List<String> toBeUndeployed = new ArrayList<String>();
		for (String s : aList) {
			if (!bList.contains(s)) {
				me.getLog().debug("To be undeployed: " + s);
				toBeUndeployed.add(s);
			}
		}
		
		// find out which elements in bList are NOT included in aList
		List<String> toBeDeployed = new ArrayList<String>();
		for (String s : bList) {
			if (!aList.contains(s)) {
				me.getLog().debug("To be deployed: " + s);
				toBeDeployed.add(s);
			}
		}
		
		// send undeploy requests
		for (String bundleName : toBeUndeployed) {
			UndeployProcessBundleRequest undeployProcessBundleRequest = 
					new UndeployProcessBundleRequest();
			undeployProcessBundleRequest.setBundleName(bundleName);
			try {
				me.invokeOneWayService(request.getRequester(), undeployProcessBundleRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// send deploy requests
		for (String bundleName : toBeDeployed) {
			try {
				File bundleFile = new File(me.getBPELEngineDeployDirectory(), bundleName + ".zip");
				
				DeployProcessBundleRequest deployProcessBundleRequest = 
						new DeployProcessBundleRequest();
				deployProcessBundleRequest.setBundleName(bundleName);
				deployProcessBundleRequest.setBundleContent(getBytesFromFile(bundleFile));
				
				me.invokeOneWayService(request.getRequester(), deployProcessBundleRequest);
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
