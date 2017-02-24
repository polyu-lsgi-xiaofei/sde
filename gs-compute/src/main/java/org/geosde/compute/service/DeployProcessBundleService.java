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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geosde.compute.BPELCubeService;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * This p2p service is used to deploy a BPEL process bundle to the BPEL engine 
 * of the local node.
 * 
 * @author Michael Pantazoglou
 *
 */
public class DeployProcessBundleService extends BPELCubeService {
	
	/**
	 * The request that triggered this service.
	 */
	private DeployProcessBundleRequest request;
	
	/**
	 * Constructor.
	 */
	public DeployProcessBundleService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (DeployProcessBundleRequest) req;
	}
	
	/**
	 * Unzips the specified zip file into the specified directory.
	 * 
	 * @param zipFile
	 * @param dir
	 */
	@SuppressWarnings("rawtypes")
	private void unzipFileIntoDirectory(ZipFile zipFile, File dir) {
		Enumeration files = zipFile.entries();
		File f = null;
		FileOutputStream fos = null;

		while (files.hasMoreElements()) {
			try {
				ZipEntry entry = (ZipEntry) files.nextElement();
				InputStream eis = zipFile.getInputStream(entry);
				byte[] buffer = new byte[1024];
				int bytesRead = 0;

				f = new File(dir.getAbsolutePath() + File.separator + entry.getName());

				if (entry.isDirectory()) {
					f.mkdirs();
					continue;
				} else {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}

				fos = new FileOutputStream(f);

				while ((bytesRead = eis.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

	@Override
	public void execute() {
		
		me.getLog().info("Deploying bundle with name: " + request.getBundleName());
		
		try {
			File bundleFile = new File(me.getBPELEngineDeployDirectory(), request.getBundleName()+".zip");
			if (!bundleFile.exists()) {
				OutputStream out = new FileOutputStream(bundleFile.getAbsolutePath());
				out.write(request.getBundleContent());
				out.flush();
				out.close();
			}
			
			// Open zip file for reading and unzip its contents
			ZipFile zipFile = new ZipFile(bundleFile);
			File bundleDir = new File(me.getBPELEngineDeployDirectory(), request.getBundleName());
			if (!bundleDir.exists()) {
				bundleDir.mkdir();
				this.unzipFileIntoDirectory(zipFile, bundleDir);
			} 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
