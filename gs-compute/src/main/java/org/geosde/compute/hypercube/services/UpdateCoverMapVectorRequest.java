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
 * Request message of the {@link UpdateCoverMapVectorService}.
 * 
 * @author Michael Pantazoglou
 *
 */
public class UpdateCoverMapVectorRequest extends P2PRequest {
	
	private static final long serialVersionUID = 201204132347L;
	/**
	 * The new cover map vector.
	 */
	private int[] newCoverMapVector;

	/**
	 * Constructor.
	 */
	public UpdateCoverMapVectorRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new UpdateCoverMapVectorService();
	}

	/**
	 * @return the newCoverMapVector
	 */
	public int[] getNewCoverMapVector() {
		return newCoverMapVector;
	}

	/**
	 * @param newCoverMapVector the newCoverMapVector to set
	 */
	public void setNewCoverMapVector(int[] newCoverMapVector) {
		this.newCoverMapVector = newCoverMapVector;
	}

}
