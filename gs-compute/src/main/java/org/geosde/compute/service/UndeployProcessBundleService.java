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

import org.geosde.compute.BPELCubeService;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * This service undeploys the BPEL process bundle that is specified in the 
 * request.
 * 
 * @author Michael Pantazoglou
 *
 */
public class UndeployProcessBundleService extends BPELCubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private UndeployProcessBundleRequest request;
	
	/**
	 * Constructor.
	 */
	public UndeployProcessBundleService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (UndeployProcessBundleRequest) req;
	}
	
	private boolean removeDirectory(File directory) {

		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory())
			return false;

		String[] list = directory.list();

		// Some JVMs return null for File.list() when the directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);
				if (entry.isDirectory()) {
					if (!removeDirectory(entry))
						return false;
				}
				else {
					if (!entry.delete()) {
						return false;
					}
				}
			}
		}
		return directory.delete();
	}

	@Override
	public void execute() {
		
		me.getLog().info("Undeploying bundle folder with name: " + request.getBundleName());
		
		File bundleDir = new File(me.getBPELEngineDeployDirectory(), request.getBundleName());
		boolean deleted = this.removeDirectory(bundleDir);
		me.getLog().info("Undeploy bundle folder " + (deleted?"success":"failure"));
		
		me.getLog().info("Removing bundle zip file in case it exists");
		File bundleZip = new File(me.getBPELEngineDeployDirectory(), request.getBundleName() + ".zip");
		if (bundleZip.exists()) {
			boolean fileDeleted = bundleZip.delete();
			me.getLog().info("Bundle zip file remove " + (fileDeleted?"success":"failure"));
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
