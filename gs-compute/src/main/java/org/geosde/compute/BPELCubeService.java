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

import org.geosde.compute.p2p.P2PService;

/**
 * Abstract base of all P2P Engine service implementations.
 * 
 * @author Michael Pantazoglou
 *
 */
public abstract class BPELCubeService implements P2PService {
	
	/**
	 * The node that executes this service.
	 */
	protected BPELCubeNode me;
	/**
	 * The embedded database of the node that executes this service.
	 */
	protected BPELCubeNodeDB db;
	
	/**
	 * Constructor.
	 */
	protected BPELCubeService() {
		super();
		me = (BPELCubeNode) BPELCubeNode.sharedInstance;
		db = (BPELCubeNodeDB) me.getNodeDB();
	}

}
