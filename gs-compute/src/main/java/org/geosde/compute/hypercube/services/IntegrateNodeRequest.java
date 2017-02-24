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
 * This class implements the input of the {@link IntegrateNodeService}.
 * 
 * @author Michael Pantazoglou
 *
 */
public class IntegrateNodeRequest extends P2PRequest {

	private static final long serialVersionUID = 201204062216L;
	/**
	 * The dimension along which the new node will be integrated.
	 */
	private int integrationDimension;
	/**
	 * The position vector of the new node.
	 */
	private int[] newNodePositionVector;
	/**
	 * The cover map vector of the new node.
	 */
	private int[] newNodeCoverMapVector;
	/**
	 * The network address of the new node.
	 */
	private URI newNodeAddress;
	/**
	 * Informs the recipient integration champion node of its level. If it is 
	 * the first elected integration champion node, then this attribute takes 
	 * the value of 1. Otherwise, it takes the value of 2.
	 */
	private int integrationChampionNodeLevel;
	
	/**
	 * Constructor.
	 */
	public IntegrateNodeRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new IntegrateNodeService();
	}

	/**
	 * @return the integrationDimension
	 */
	public int getIntegrationDimension() {
		return integrationDimension;
	}

	/**
	 * @param integrationDimension the integrationDimension to set
	 */
	public void setIntegrationDimension(int integrationDimension) {
		this.integrationDimension = integrationDimension;
	}

	/**
	 * @return the newNodePositionVector
	 */
	public int[] getNewNodePositionVector() {
		return newNodePositionVector;
	}

	/**
	 * @param newNodePositionVector the newNodePositionVector to set
	 */
	public void setNewNodePositionVector(int[] newNodePositionVector) {
		this.newNodePositionVector = newNodePositionVector;
	}

	/**
	 * @return the newNodeCoverMapVector
	 */
	public int[] getNewNodeCoverMapVector() {
		return newNodeCoverMapVector;
	}

	/**
	 * @param newNodeCoverMapVector the newNodeCoverMapVector to set
	 */
	public void setNewNodeCoverMapVector(int[] newNodeCoverMapVector) {
		this.newNodeCoverMapVector = newNodeCoverMapVector;
	}

	/**
	 * @return the integrationChampionNodeLevel
	 */
	public int getIntegrationChampionNodeLevel() {
		return integrationChampionNodeLevel;
	}

	/**
	 * @param integrationChampionNodeLevel the integrationChampionNodeLevel to set
	 */
	public void setIntegrationChampionNodeLevel(int integrationChampionNodeLevel) {
		this.integrationChampionNodeLevel = integrationChampionNodeLevel;
	}

	/**
	 * @return the newNodeAddress
	 */
	public URI getNewNodeAddress() {
		return newNodeAddress;
	}

	/**
	 * @param newNodeAddress the newNodeAddress to set
	 */
	public void setNewNodeAddress(URI newNodeAddress) {
		this.newNodeAddress = newNodeAddress;
	}

}
