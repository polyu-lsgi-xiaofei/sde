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
package org.geosde.compute.service;

import javax.xml.ws.EndpointReference;

import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class ExecuteBPELProcessRequest extends P2PRequest {
	
	private static final long serialVersionUID = 201205291843L;
	
	/**
	 * The endpoint address of the BPEL process to be invoked.
	 */
	private String processEndpointAddress;
	
	/**
	 * The SOAP request for the invocation of the BPEL process.
	 */
	private String processSOAPRequest;
	
	/**
	 * The endpoint reference of the original BPEL process invoker.
	 */
	private EndpointReference replyTo;

	/**
	 * Constructor.
	 */
	public ExecuteBPELProcessRequest() {
		super();
	}

	@Override
	public P2PService createService() {
		return new ExecuteBPELProcessService();
	}

	public String getProcessEndpointAddress() {
		return processEndpointAddress;
	}

	public void setProcessEndpointAddress(String processEndpointAddress) {
		this.processEndpointAddress = processEndpointAddress;
	}

	public String getProcessSOAPRequest() {
		return processSOAPRequest;
	}

	public void setProcessSOAPRequest(String processSOAPRequest) {
		this.processSOAPRequest = processSOAPRequest;
	}

	public EndpointReference getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(EndpointReference replyTo) {
		this.replyTo = replyTo;
	}

}
