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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geosde.compute.hypercube.Hypercube;
import org.geosde.compute.hypercube.HypercubeService;
import org.geosde.compute.hypercube.Neighbor;
import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PResponse;
import org.geosde.compute.p2p.P2PService;

/**
 * This service implements the algorithm that is followed by the integration 
 * champion node in order to integrate a joining node into the hypercube.
 * 
 * @author Michael Pantazoglou
 *
 */
public class JoinService extends HypercubeService {
	/**
	 * The request that triggered this service.
	 */
	private JoinRequest request;
	/**
	 * This flag indicates whether the node executing this service has non-
	 * immediate neighbors or not.
	 */
	private boolean nonImmediateNeighborDetected = false;
	
	/**
	 * Constructor.
	 */
	public JoinService() {
		super();
	}

	@Override
	public void setRequest(P2PRequest req) {
		request = (JoinRequest) req;
	}
	
	/**
	 * Resolves and returns the dimension along which the joining node will be 
	 * integrated.
	 * 
	 * @return the integration dimension
	 */
	private int selectIntegrationDimension() {
		
		// If I currently cover any additional positions, the integration 
		// dimension will be equal to the position of the most significant 1
		// in my cover map vector.
		int[] myCoverMapVector = me.getCoverMapVector();
		for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (myCoverMapVector[i] == 1) {
				return i;
			}
		}
		
		// Check if I have any non-immediate neighbors
		
		Set<Neighbor> myNeighborSet = me.getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			if (!Hypercube.areImmediateNeighbors(me.getPositionVector(), 
					n.getPositionVector())) {
				nonImmediateNeighborDetected = true;
				break;
			}
		}
		
		if (nonImmediateNeighborDetected) {
			List<Integer> immediateNeighborDimensions = 
					new ArrayList<Integer>();
			int minLcd = -1;
			for (Neighbor n : myNeighborSet) {
				int lcd = Hypercube.getLinkDimensionality(
						me.getPositionVector(), n.getPositionVector());
				if (Hypercube.areImmediateNeighbors(me.getPositionVector(), 
						n.getPositionVector())) {
					immediateNeighborDimensions.add(lcd);
					continue;
				}
				if (((minLcd == -1) || (lcd < minLcd)) && 
						!immediateNeighborDimensions.contains(lcd)) {
					minLcd = lcd;
				}
			}
			return minLcd;
		}
		
		return -1;
	}
	
	/**
	 * Computes the integration position of the new node, given the integration 
	 * dimension.
	 * 
	 * @param integrationDimension
	 * @return
	 */
	private int[] computeIntegrationPosition(int integrationDimension) {
		
		int[] integrationPositionVector = 
				new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
		for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (i == integrationDimension) {
				if (me.getPositionVector()[i] == 0) {
					integrationPositionVector[i] = 1;
				} else {
					integrationPositionVector[i] = 0;
				}
			} else {
				integrationPositionVector[i] = me.getPositionVector()[i];
			}
		}
		
		return integrationPositionVector;
	}
	
	/**
	 * Returns the position vector of the node that is closest to the specified 
	 * integration position. 
	 * 
	 * @param integrationPositionVector the integration position vector
	 * @return
	 */
	private int[] appointIntegrationChampionNode(
			int[] integrationPositionVector) {
		
		if (!nonImmediateNeighborDetected) {
			return me.getPositionVector();
		}
		
		int[] integrationChampionNodePositionVector = null;
		
		// Calculate distance between my position and the integration position
		int minDistance = Hypercube.MAX_NUMBER_OF_DIMENSIONS - 1;
		
		// Check if any of my neighbors has a smaller distance from the 
		// integration position
		Set<Neighbor> myNeighborSet = me.getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			int distance = Hypercube.getDistance(n.getPositionVector(), 
					integrationPositionVector);
			if (distance < minDistance) {
				minDistance = distance;
				integrationChampionNodePositionVector = n.getPositionVector();
			}
		}
		
		return integrationChampionNodePositionVector;
	}

	@Override
	public void execute() {
		
		// Step 1: Integration dimension selection
		me.getLog().debug("Selecting integration dimension.");
		int integrationDimension = this.selectIntegrationDimension();
		if (integrationDimension == -1) {
			
			// The hypercube is full!
			me.getLog().info("The hypercube is full!");
			return;
		}
		me.getLog().info("Integration dimension: " + integrationDimension);
		
		// Step 2: Integration position calculation
		me.getLog().debug("Computing integration position.");
		int[] integrationPositionVector = this.computeIntegrationPosition(
				integrationDimension);
		me.getLog().info("Integration position: " + Hypercube.vectorAsString(
				integrationPositionVector));
		
		// Step 3: Integration champion node appointment
		me.getLog().debug("Appointing integration champion node.");
		int[] integrationChampionNodePositionVector = 
				this.appointIntegrationChampionNode(integrationPositionVector);
		me.getLog().info("Integration champion: " + 
				Hypercube.vectorAsString(
						integrationChampionNodePositionVector));
		
		// Step 4: Node integration
		IntegrateNodeRequest integrateNodeRequest = new IntegrateNodeRequest();
		integrateNodeRequest.setIntegrationDimension(integrationDimension);
		integrateNodeRequest.setNewNodePositionVector(
				integrationPositionVector);
		integrateNodeRequest.setIntegrationChampionNodeLevel(1);
		integrateNodeRequest.setNewNodeAddress(
				request.getJoiningNodeNetworkAddress());
		
		if (integrationChampionNodePositionVector == me.getPositionVector()) {
			
			// I will do the integration
			me.getLog().info("Performing node integration locally.");
			P2PService service = integrateNodeRequest.createService();
			service.setRequest(integrateNodeRequest);
			service.execute();
			
		} else {
			
			me.getLog().info("Forwarding node integration control.");
			
			// One of my neighbors will do the integration
			Neighbor integrationChampionNode = me.getNeighbor(
					integrationChampionNodePositionVector);
			
			P2PEndpoint p2pEndpoint = new P2PEndpoint();
			p2pEndpoint.setAddress(integrationChampionNode.getNetworkAddress());
			try {
				me.invokeOneWayService(p2pEndpoint, request);
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
