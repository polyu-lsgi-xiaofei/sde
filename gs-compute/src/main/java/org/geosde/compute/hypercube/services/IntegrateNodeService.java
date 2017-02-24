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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.HypercubeService;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;

/**
 * This service is used to carry out the integration of a node into the 
 * hypercube topology.
 * 
 * @author Michael Pantazoglou
 *
 */
public class IntegrateNodeService extends HypercubeService {
	/**
	 * The request that triggered this service.
	 */
	private IntegrateNodeRequest request;
	
	/**
	 * Constructor.
	 */
	public IntegrateNodeService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (IntegrateNodeRequest) req;
	}
	
	private void doLevelOneIntegrationSteps() {
		
		// Step 1: Compute the cover map vector of the new node
		me.getLog().debug("Computing the cover map vector of the new node.");
		int[] newNodeCoverMapVector = 
				new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
		for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (i == request.getIntegrationDimension()) {
				newNodeCoverMapVector[i] = 0;
				continue;
			}
			if (i > request.getIntegrationDimension() && 
					i < Hypercube.MAX_NUMBER_OF_DIMENSIONS-1) {
				newNodeCoverMapVector[i] = 1;
				continue;
			}
			newNodeCoverMapVector[i] = me.getCoverMapVector()[i];
		}
		me.getLog().info("Cover map vector: " + Hypercube.vectorAsString(
				newNodeCoverMapVector));
		request.setNewNodeCoverMapVector(newNodeCoverMapVector);
		
		// Send the cover map vector and the position vector to the new node
		SetPositionAndCoverMapRequest setPositionAndCoverMapRequest = 
				new SetPositionAndCoverMapRequest();
		setPositionAndCoverMapRequest.setPositionVector(
				request.getNewNodePositionVector());
		setPositionAndCoverMapRequest.setCoverMapVector(
				request.getNewNodeCoverMapVector());
		
		P2PEndpoint newNodeEndpoint = new P2PEndpoint();
		newNodeEndpoint.setAddress(request.getNewNodeAddress());
		try {
			me.invokeOneWayService(newNodeEndpoint, 
					setPositionAndCoverMapRequest);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// Step 2: Identify and notify other integration champion nodes
		BroadcastRequest broadcastRequest = new BroadcastRequest();
		broadcastRequest.setMinDimension(request.getIntegrationDimension() + 1);
		broadcastRequest.setServiceRequest(request);
		Set<Neighbor> myNeighborSet = me.getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			int linkDimensionality = Hypercube.getLinkDimensionality(
					me.getPositionVector(), n.getPositionVector());
			if (linkDimensionality > broadcastRequest.getMinDimension()) {
				P2PEndpoint p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(n.getNetworkAddress());
				broadcastRequest.setMinDimension(linkDimensionality);
				try {
					me.invokeOneWayService(p2pEndpoint, broadcastRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Determines whether the specified neighbor stops being my neighbor.
	 * 
	 * @param n the neighbor
	 * @param myCoveredPositions the positions currently covered by me
	 * @return
	 */
	private boolean mustStopBeingMyNeighbor(Neighbor n, 
			List<int[]> myCoveredPositions) {
		
		for (int[] p : myCoveredPositions) {
//			if (Hypercube.areVectorsEqual(p, me.getPositionVector())) {
//				continue;
//			}
			for (int d=0; d<request.getIntegrationDimension(); d++) {
				if (me.getCoverMapVector()[d] == 1) {
					continue;
				}
				int[] dVector1 = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
				for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
					dVector1[i] = i==d?1:0;
				}
				int[] neighborPosition = Hypercube.XOR(p, dVector1);
				
				int distance = Hypercube.getDistance(n.getPositionVector(), 
						neighborPosition);
				boolean smallestHammingDistance = true;
				Set<Neighbor> myNeighborSet = me.getNeighborSet();
				for (Neighbor neighbor : myNeighborSet) {
					if (Arrays.equals(neighbor.getPositionVector(), 
							n.getPositionVector())) {
						continue;
					}
					if (Hypercube.getDistance(neighbor.getPositionVector(), 
							neighborPosition) < distance) {
						smallestHammingDistance = false;
						StringBuilder sb = new StringBuilder();
						sb.append("Neighbor node "); 
						sb.append(Hypercube.vectorAsString(neighbor.getPositionVector()));
						sb.append(" is closest to position ");
						sb.append(Hypercube.vectorAsString(neighborPosition));
						sb.append(" than node ");
						sb.append(Hypercube.vectorAsString(n.getPositionVector()));
						me.getLog().debug(sb.toString());
					}
				}
				
				// Neighbor n has the smallest Hamming distance to the currently
				// processing neighbor position, hence it will remain a neighbor
				// of the integration champion node.
				if (smallestHammingDistance) {
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public void execute() {
		
		me.getLog().debug("Integrating new node in dimension: " + 
				request.getIntegrationDimension());
		
		int integrationChampionNodeLevel = 
				request.getIntegrationChampionNodeLevel();
		me.getLog().debug("Integration champion node level: " + 
				integrationChampionNodeLevel);
		
		// Perform level-one integration steps if I am the first elected 
		// integration champion node
		if (integrationChampionNodeLevel == 1) {
			this.doLevelOneIntegrationSteps();
		} 
		
		// Update my own cover map vector by flipping the bit in the 
		// integration dimension position
		me.flipCoverMapVectorAt(request.getIntegrationDimension());
		
		// Determine which nodes will become neighbors of the new node
		
		// First, compute the positions that remain covered by the integration 
		// champion node (me)
		List<int[]> championNodePositions = Hypercube.getCoveredPositions(
				me.getPositionVector(), me.getCoverMapVector());
		
		StringBuilder sb = new StringBuilder();
		sb.append("Positions that remain covered by me: ");
		for (int[] p : championNodePositions) {
			sb.append(Hypercube.vectorAsString(p));
			sb.append(" ");
		}
		me.getLog().debug(sb.toString());
		
		// Next, compute the positions that will be covered by the new node 
		// after the integration has been carried out
		List<int[]> newNodePositions = Hypercube.getCoveredPositions(
				request.getNewNodePositionVector(), 
				request.getNewNodeCoverMapVector());
		
		sb = new StringBuilder();
		sb.append("Positions that will be covered by the new node: ");
		for (int[] p : newNodePositions) {
			sb.append(Hypercube.vectorAsString(p));
			sb.append(" ");
		}
		me.getLog().debug(sb.toString());
		
		for (int[] p : newNodePositions) {
			for (int d=0; d<request.getIntegrationDimension(); d++) {
				if (me.getCoverMapVector()[d] == 1) {
					continue;
				}
				int[] dVector = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
				for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
					dVector[i] = i==d?1:0;
				}
				int[] neighborPosition = Hypercube.XOR(p, dVector);
				
				// Now, I need to find out which of my neighbors is closest to 
				// this neighbor position
				int minDistance = Hypercube.MAX_NUMBER_OF_DIMENSIONS - 1;
				Neighbor closestNeighbor = null;
				Set<Neighbor> myNeighborSet = me.getNeighborSet();
				for (Neighbor n : myNeighborSet) {
					int distance = Hypercube.getDistance(n.getPositionVector(), 
							neighborPosition);
					if (distance <= minDistance) {
						minDistance = distance;
						closestNeighbor = n;
					}
				}
				
				// The closestNeighbor will become a neighbor of the new node
				if (closestNeighbor == null) {
					continue;
				}
				
				// First send an AddNeighbor request to the closestNeighbor
				Neighbor newNodeAsNeighbor = new Neighbor();
				newNodeAsNeighbor.setLastUsed(0);
				newNodeAsNeighbor.setNetworkAddress(
						request.getNewNodeAddress());
				newNodeAsNeighbor.setPositionVector(
						request.getNewNodePositionVector());
				AddNeighborRequest addNeighborRequest = 
						new AddNeighborRequest();
				addNeighborRequest.setNewNeighbor(newNodeAsNeighbor);
				
				P2PEndpoint p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(closestNeighbor.getNetworkAddress());
				try {
					me.invokeOneWayService(p2pEndpoint, addNeighborRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Then send an AddNeighbor request to the new node
				// (actually this should be done by the closestNeighbor)
				addNeighborRequest = new AddNeighborRequest();
				addNeighborRequest.setNewNeighbor(closestNeighbor);
				
				p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(request.getNewNodeAddress());
				try {
					me.invokeOneWayService(p2pEndpoint, addNeighborRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Determine if the closestNeighbor stops being my neighbor
				if (mustStopBeingMyNeighbor(closestNeighbor, 
						championNodePositions)) {
					me.getLog().info("Removing neighbor: " + 
							Hypercube.vectorAsString(
									closestNeighbor.getPositionVector()));
					if (!me.getNeighborSet().remove(closestNeighbor)) {
						me.getLog().debug("Neighbor not found: " + 
								Hypercube.vectorAsString(
										closestNeighbor.getPositionVector()));
					}
					
					// Also inform that node so that it removes me from its 
					// neighbor set
					RemoveNeighborRequest removeNeighborRequest = 
							new RemoveNeighborRequest();
					removeNeighborRequest.setNeighborPositionVector(
							me.getPositionVector());
					
					p2pEndpoint = new P2PEndpoint();
					p2pEndpoint.setAddress(closestNeighbor.getNetworkAddress());
					try {
						me.invokeOneWayService(p2pEndpoint, removeNeighborRequest);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// Finally, add the new node into my list of neighbors
		Neighbor newNeighbor = new Neighbor();
		newNeighbor.setLastUsed(0);
		newNeighbor.setNetworkAddress(request.getNewNodeAddress());
		newNeighbor.setPositionVector(request.getNewNodePositionVector());
		me.getNeighborSet().add(newNeighbor);
		
		// ... and ask the new node to the same
		Neighbor meAsNeighbor = new Neighbor();
		meAsNeighbor.setLastUsed(me.getLastUsed());
		meAsNeighbor.setNetworkAddress(me.getAddress());
		meAsNeighbor.setPositionVector(me.getPositionVector());
		AddNeighborRequest addNeighborRequest = new AddNeighborRequest();
		addNeighborRequest.setNewNeighbor(meAsNeighbor);
		
		P2PEndpoint newNodeEndpoint = new P2PEndpoint();
		newNodeEndpoint.setAddress(request.getNewNodeAddress());
		try {
			me.invokeOneWayService(newNodeEndpoint, addNeighborRequest);
		} catch (Exception e) {
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
