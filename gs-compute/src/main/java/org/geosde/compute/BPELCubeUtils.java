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

import javax.xml.namespace.QName;

/**
 * This class provides a number of P2P engine-related utilities.
 * 
 * @author Michael Pantazoglou
 *
 */
public class BPELCubeUtils {
	
	/**
	 * Counts the number of created process instances.
	 */
	private static long processInstanceCounter = 0L;
	
	public static final String BPELCUBE_NS = "urn:bpelcube";
	
	/**
	 * The qualified name of the p2p session id soap header extension.
	 */
	public static final QName SOAP_HEADER_P2P_SESSION_ID = new QName(BPELCUBE_NS, "P2PSessionId");
	
	/**
	 * The qualified name of the soap header extension that indicates whether a
	 * soap request was routed within the hypercube or not.
	 */
	public static final QName SOAP_HEADER_ROUTED = new QName(BPELCUBE_NS, "Routed");
	
	/**
	 * Creates and returns a new P2P session ID. Uniqueness is attempted to be 
	 * ensured by using the current time millis and a static counter as the 
	 * ingredients of the generated ID.
	 * 
	 * @return
	 */
	public static String newP2PSessionID() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("urn:bpelcube").append(":");
		sb.append(System.currentTimeMillis()).append("-");
		sb.append(processInstanceCounter++);
		
		return sb.toString();
	}
	
	

}
