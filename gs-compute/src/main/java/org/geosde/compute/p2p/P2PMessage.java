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

import java.io.Serializable;
import java.util.HashMap;

/**
 * Defines a convenient abstraction for all p2p messages.
 * 
 * @author Michael Pantazoglou
 */
public class P2PMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String CORRELATION_ID = "CorrelationId";
    
    /**
     * The message's header may contain an arbitrary number of serializable 
     * objects distinguished by their name.
     */
    protected HashMap<String,Serializable> header;
    
    public P2PMessage() {
    	header = new HashMap<String,Serializable>();
    }

	public HashMap<String,Serializable> getHeader() {
		return header;
	}

	public void setHeader(HashMap<String,Serializable> header) {
		this.header = header;
	}
	
	public void addHeaderElement(String name, Serializable headerElement) {
		this.header.put(name, headerElement);
	}
	
	/**
	 * Gets the correlation id of this p2p message.
	 * 
	 * @return
	 */
	public String getCorrelationId() {
		return (String) header.get(CORRELATION_ID);
	}

}
