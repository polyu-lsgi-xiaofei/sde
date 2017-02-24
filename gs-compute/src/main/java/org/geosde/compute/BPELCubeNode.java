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
package org.geosde.compute;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.HypercubeNode;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.Log.Level;
import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PNodeDB;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.service.GetDeployedProcessBundlesUpdateRequest;
import org.geosde.compute.service.RecruitRequest;

/**
 * Hypercube-based implementation of the P2P engine node.
 * 
 * @author Michael Pantazoglou
 *
 */
public class BPELCubeNode extends HypercubeNode {
	
	public static enum Role {
		MANAGER,
		WORKER
	};
	
	/**
	 * The embedded database of this node.
	 */
	private BPELCubeNodeDB db;
	
	/**
	 * The absolute path of the directory that is used by the BPEL engine in 
	 * order to store the deployed BPEL bundles.
	 */
	private String bpelEngineDeployDirectory; 
	
	private Hashtable<String, BPELActivityListener<P2PRequest>> activityListeners = 
			new Hashtable<String, BPELActivityListener<P2PRequest>>();
	
	private Hashtable<String, BPELProcessExecutionListener<P2PRequest>> processExecutionListeners = 
			new Hashtable<String, BPELProcessExecutionListener<P2PRequest>>();
	
	/**
	 * The port of the underlying BPEL engine.
	 */
	private int bpelEnginePort;
	
	/**
	 * Constructor.
	 * 
	 * @param home
	 * @param name
	 * @param domain
	 * @param port
	 * @param logLevel
	 */
	public BPELCubeNode(URI home, String name, String domain, int port,
			Level logLevel) {
		super(home, name, domain, port, logLevel);
		db = new BPELCubeNodeDB(home.toString());
	}
	
	/**
	 * Constructor.
	 * 
	 * @param home
	 * @param name
	 * @param domain
	 * @param address
	 * @param port
	 * @param logLevel
	 */
	public BPELCubeNode(URI home, String name, String domain, URI address,
			int port, Level logLevel) {
		super(home, name, domain, address, port, logLevel);
		db = new BPELCubeNodeDB(home.toString());
	}
	
	@Override
	protected void join() throws Exception {
		
		super.join();
		
		if (getBootstrapURI() == null) {
			return;
		}
		
		// get updates wrt the currently deployed processes
		GetDeployedProcessBundlesUpdateRequest getDeployedProcessBundlesUpdateRequest = 
				new GetDeployedProcessBundlesUpdateRequest();
		getDeployedProcessBundlesUpdateRequest.setRequester(getEndpoint());
		getDeployedProcessBundlesUpdateRequest.setDeployedProcessBundles(getCurrentlyDeployedProcessBundles());
		
		P2PEndpoint bootstrapEndpoint = new P2PEndpoint();
		bootstrapEndpoint.setAddress(getBootstrapURI());
		invokeOneWayService(bootstrapEndpoint, getDeployedProcessBundlesUpdateRequest);
	}
	
	@Override
	public P2PNodeDB getNodeDB() {
		return db;
	}
	
	/**
	 * Gets the BPEL engine deploy directory's absolute path.
	 * 
	 * @return
	 */
	public String getBPELEngineDeployDirectory() {
		return this.bpelEngineDeployDirectory;
	}
	
	/**
	 * Sets the BPEL engine deploy directory's absolute path.
	 * 
	 * @param directoryPath
	 */
	public void setBPELEngineDeployDirectory(String directoryPath) {
		this.bpelEngineDeployDirectory = directoryPath;
	}
	
	/**
	 * Initializes the distributed execution of the specified activities by 
	 * assigning them to a series of selected worker nodes.
	 * 
	 * @param p2pSessionId
	 * @param activityIds ids of the activities to be distributed
	 */
	public void recruitWorkers(String p2pSessionId, List<String> activityIds) {
		
		Long p2pSessionCreationTime = System.currentTimeMillis();
		db.addP2PSession(p2pSessionId, Role.MANAGER.toString(), p2pSessionCreationTime, null);
		
		setLastUsed(System.currentTimeMillis());
		
		RecruitRequest recruitRequest = new RecruitRequest();
		recruitRequest.setP2PSessionId(p2pSessionId);
		recruitRequest.setP2PSessionCreationTime(p2pSessionCreationTime);
		recruitRequest.setManagerEndpoint(getEndpoint());
		recruitRequest.setRequesterEndpoint(getEndpoint());
		recruitRequest.setActivityIds(activityIds);
		
		Neighbor LRU = getLRUNeighbor();
		if (LRU == null) {
			// I have no neighbors so I will do everything by myself
			for (String aId : activityIds) {
				db.addP2PSessionActivity(p2pSessionId, aId);
			}
		} else {
			// Recruit LRU
			getLog().debug("LRU neighbor: " + Hypercube.vectorAsString(LRU.getPositionVector()));
			if (!db.p2pSessionNeighborExists(p2pSessionId, LRU.asP2PEndpoint().toString())) {
				db.addP2PSessionNeighor(p2pSessionId, LRU.asP2PEndpoint().toString());
			}
			try {
				invokeOneWayService(LRU.asP2PEndpoint(), recruitRequest);
				
				// Now wait until the recruitment process is complete
				SynchronousQueue<P2PRequest> queue = new SynchronousQueue<P2PRequest>();
				BPELProcessExecutionListener<P2PRequest> processExecutionListener = 
						new BPELProcessExecutionListener<P2PRequest>(queue);
				processExecutionListener.setP2PSessionId(p2pSessionId);
				this.addProcessExecutionListener(processExecutionListener);
				processExecutionListener.listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Finalizes the specified P2P session.
	 * 
	 * @param p2pSessionId
	 * @param completionTime
	 */
	public void finalizeP2PSession(String p2pSessionId, Long completionTime) {
		
		db.setCompletionTime(p2pSessionId, completionTime);
	}
	
	/**
	 * Adds the specified activity listener.
	 * 
	 * @param activityListener
	 */
	public void addActivityListener(BPELActivityListener<P2PRequest> activityListener) {
		
		String p2pSessionId = activityListener.getP2PSessionId();
		String activityId = activityListener.getActivityId();
		
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(p2pSessionId).append("-").append(activityId);
		
		String key = keyBuilder.toString();
		activityListeners.put(key, activityListener);
	}
	
	/**
	 * Gets the listener of the specified activity.
	 * 
	 * @param p2pSessionId
	 * @param activityId
	 * @return
	 */
	public BPELActivityListener<P2PRequest> getActivityListener(String p2pSessionId, String activityId) {
		
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(p2pSessionId).append("-").append(activityId);
		
		String key = keyBuilder.toString();
		
		return activityListeners.get(key);
	}
	
	public BPELActivityListener<P2PRequest> removeActivityListener(String p2pSessionId, 
			String activityId) {
		
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(p2pSessionId).append("-").append(activityId);
		
		String key = keyBuilder.toString();
		
		return activityListeners.remove(key);
	}
	
	/**
	 * Adds the specified BPEL process execution listener.
	 * 
	 * @param processExecutionListener
	 */
	public void addProcessExecutionListener(
			BPELProcessExecutionListener<P2PRequest> processExecutionListener) {
		processExecutionListeners.put(processExecutionListener.getP2PSessionId(), 
				processExecutionListener);
	}
	
	/**
	 * Gets the BPEL process execution listener that corresponds to the 
	 * specified P2P session.
	 * 
	 * @param p2pSessionId
	 * @return
	 */
	public BPELProcessExecutionListener<P2PRequest> getProcessExecutionListener(
			String p2pSessionId) {
		return processExecutionListeners.get(p2pSessionId);
	}
	
	/**
	 * Removes the BPEL process execution listener that corresponds to the 
	 * specified P2P session.
	 * 
	 * @param p2pSessionId
	 * @return
	 */
	public BPELProcessExecutionListener<P2PRequest> removeProcessExecutionListener(
			String p2pSessionId) {
		return processExecutionListeners.remove(p2pSessionId);
	}
	
	/**
	 * Gets a list with the file names of the currently deployed BPEL process 
	 * bundles.
	 * 
	 * @return
	 */
	public List<String> getCurrentlyDeployedProcessBundles() {
		this.getLog().debug("Getting bundle folders from deploy directory: " + this.bpelEngineDeployDirectory);
		List<String> deployedBundles = new ArrayList<String>();
		if (this.bpelEngineDeployDirectory == null) {
			return deployedBundles;
		}
		File dir = new File(this.bpelEngineDeployDirectory);
		String[] dirContents = dir.list();
		if (dirContents.length == 0) {
			this.getLog().debug("No bundles were found.");
		}
		for (String s : dirContents) {
			File f = new File(dir, s);
			if (f.isDirectory()) {
				this.getLog().debug("Found deployed bundle: " + s);
				deployedBundles.add(s);
			}
		}
		return deployedBundles;
	}

	public int getBPELEnginePort() {
		return bpelEnginePort;
	}

	public void setBPELEnginePort(int bpelEnginePort) {
		this.bpelEnginePort = bpelEnginePort;
	}
	
	/**
	 * Returns the next recipient neighbor that participates in the specified 
	 * p2p session. This method is used each time a node needs to propagate a
	 * notification to all nodes participating in a p2p session.
	 * 
	 * @param p2pSessionId the p2p session id
	 * @param notifiedNodes list of nodes that have been already notified
	 * @return the next recipient neighbor
	 */
	public Neighbor getNextRecipientNeighborInP2PSession(String p2pSessionId, 
			List<String> notifiedNodes) {
		
		List<String> p2pSessionNeighbors = db.getP2PSessionNeighbors(p2pSessionId);
		getLog().debug("I have + " + p2pSessionNeighbors.size() + " neighbors in p2p session " + p2pSessionId);
		
		
		int minDimension = Hypercube.MAX_NUMBER_OF_DIMENSIONS - 1;
		Neighbor nextRecipientNeighbor = null;
		for (String endpoint : p2pSessionNeighbors) {
			if (notifiedNodes.contains(endpoint)) {
				getLog().debug("Neighbor " + endpoint + " has been already notified.");
				continue;
			}
			try {
				URI networkAddress = new URI(endpoint);
				Neighbor neighbor = this.getNeighbor(networkAddress);
				int d = Hypercube.getLinkDimensionality(getPositionVector(), 
						neighbor.getPositionVector());
				if (d <= minDimension) {
					getLog().debug("Prospective next recipient is neighbor " + networkAddress + " in dimension " + d);
					minDimension = d;
					nextRecipientNeighbor = neighbor;
					if (minDimension == 0) {
						break;
					}
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
				continue;
			}
		}
		
		if (nextRecipientNeighbor != null) {
			getLog().debug("Next recipient is neighbor " + nextRecipientNeighbor.asP2PEndpoint());
		}
		
		return nextRecipientNeighbor;
	}

}
