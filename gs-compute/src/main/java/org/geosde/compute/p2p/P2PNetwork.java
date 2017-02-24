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
package org.geosde.compute.p2p;

import java.net.URI;

/**
 * Defines an abstraction of the P2P network.
 * 
 * @author Michael Pantazoglou
 *
 */
public abstract class P2PNetwork {
	
	/**
	 * Starts up this P2P network with the specified properties.
	 * 
	 * @param nodeHome the node's home directory
	 * @param nodeName the node's name
	 * @param nodeAddress the node's endpoint address (or null if it is not known a priori)
	 * @param port the node's listening port
	 * @param networkName the name of this network
	 * @return the node's established endpoint address
	 * @throws Exception
	 */
	public abstract URI startup(URI nodeHome, String nodeName, URI nodeAddress, 
			int port, String networkName) throws Exception;
	
	/**
	 * Returns the P2P connection listener that is associated with this network.
	 * 
	 * @return
	 */
	public abstract P2PConnectionListener getP2PConnectionListener();
	
	/**
	 * Establishes and returns a P2P connection to the specified P2P endpoint.
	 * 
	 * @param e
	 * @return
	 * @throws Exception
	 */
	public abstract P2PConnection connect(P2PEndpoint e) throws Exception;
	
	/**
	 * Shuts down this network.
	 * 
	 * @throws Exception
	 */
	public abstract void shutdown() throws Exception;

}
