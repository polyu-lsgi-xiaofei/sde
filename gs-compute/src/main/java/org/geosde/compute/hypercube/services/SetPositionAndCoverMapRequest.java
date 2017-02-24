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

import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * This request is sent by the first elected integration champion node to the 
 * new node in order to set its position vector and cover map vector.
 * 
 * @author Michael Pantazoglou
 *
 */
public class SetPositionAndCoverMapRequest extends P2PRequest {

	private static final long serialVersionUID = 1L;
	
	private int[] positionVector;
	
	private int[] coverMapVector;

	public SetPositionAndCoverMapRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new SetPositionAndCoverMapService();
	}

	/**
	 * @return the positionVector
	 */
	public int[] getPositionVector() {
		return positionVector;
	}

	/**
	 * @param positionVector the positionVector to set
	 */
	public void setPositionVector(int[] positionVector) {
		this.positionVector = positionVector;
	}

	/**
	 * @return the coverMapVector
	 */
	public int[] getCoverMapVector() {
		return coverMapVector;
	}

	/**
	 * @param coverMapVector the coverMapVector to set
	 */
	public void setCoverMapVector(int[] coverMapVector) {
		this.coverMapVector = coverMapVector;
	}

}
