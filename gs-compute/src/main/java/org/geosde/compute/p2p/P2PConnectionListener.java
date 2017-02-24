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

/**
 * The interface of p2p connection listeners.
 * 
 * @author Michael Pantazoglou
 */
public interface P2PConnectionListener {

    /**
     * Waits until a connection request is received, upon which a new p2p
     * connection is created and returned.
     * 
     * @return
     * @throws Exception
     */
    public P2PConnection listen() throws Exception;
    
    /**
     * Stops the connection listener.
     * 
     * @throws Exception
     */
    public void stop() throws Exception;

}
