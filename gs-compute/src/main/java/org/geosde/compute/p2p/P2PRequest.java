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
 * Defines a convenient abstraction for all p2p <b>request</b> messages.
 * 
 * @author Michael Pantazoglou
 *
 */
public abstract class P2PRequest extends P2PMessage {

	private static final long serialVersionUID = 3863562841558745016L;
	
	private static int CORRELATION_ID_COUNTER = 0;

	/**
	 * Constructor.
	 */
	protected P2PRequest() {
		super();
		addHeaderElement(CORRELATION_ID, newCorrelationId());
	}
	
	/**
     * Creates and returns a new correlation id.
     * 
     * @return
     */
    private static String newCorrelationId() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("p2p::").append(System.currentTimeMillis()).append("::");
    	sb.append(CORRELATION_ID_COUNTER++);
    	return sb.toString();
    }
	
	/**
     * Creates and returns the appropriate p2p service that will handle this p2p request.
     * 
     * @return
     */
    public abstract P2PService createService();

}
