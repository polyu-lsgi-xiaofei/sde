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
 * Defines an abstraction of all p2p services. A p2p service is used by a node
 * to respond to an incoming message. Depending on the kind of message, the 
 * appropriate service is loaded and executed by means of this interface.
 * 
 * @author Michael Pantazoglou
 * @see p2p.P2PConnectionHandler#handle()
 */
public interface P2PService {

    public void setRequest(P2PRequest req);

    public void execute();

    public boolean isRequestResponse();

    public P2PResponse getResponse();

}
