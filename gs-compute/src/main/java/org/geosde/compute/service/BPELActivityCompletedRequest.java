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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PRequest;
import org.geosde.compute.p2p.P2PService;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class BPELActivityCompletedRequest extends P2PRequest {

	private static final long serialVersionUID = 201204261359L;
	/**
	 * The id of the p2p session where the specified activity was executed.
	 */
	private String p2pSessionId;
	/**
	 * The id of the completed activity.
	 */
	private String activityId;
	
	/**
	 * The new variable holders.
	 */
	private Hashtable<String, P2PEndpoint> newVariableHolders;

	/**
	 * If set to true, this flag indicates failed execution of the activity.
	 */
	private boolean executionFailed;
	
	/**
	 * The message explaining the cause of failure.
	 */
	private String failureMessage;
	
	/**
	 * The list of nodes involved in the p2p session which have been already 
	 * notified about the completion of the activity.
	 */
	private List<String> notifiedNodes;
	
	/**
	 * Constructor.
	 */
	public BPELActivityCompletedRequest() {
		super();
		newVariableHolders = new Hashtable<String, P2PEndpoint>();
		setNotifiedNodes(new ArrayList<String>());
	}

	@Override
	public P2PService createService() {
		return new BPELActivityCompletedService();
	}

	public String getP2PSessionId() {
		return p2pSessionId;
	}

	public void setP2PSessionId(String p2pSessionId) {
		this.p2pSessionId = p2pSessionId;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public Hashtable<String, P2PEndpoint> getNewVariableHolders() {
		return newVariableHolders;
	}

	public void setNewVariableHolders(Hashtable<String, P2PEndpoint> newVariableHolders) {
		this.newVariableHolders = newVariableHolders;
	}


	public boolean isExecutionFailed() {
		return executionFailed;
	}

	public void setExecutionFailed(boolean executionFailed) {
		this.executionFailed = executionFailed;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public List<String> getNotifiedNodes() {
		return notifiedNodes;
	}

	public void setNotifiedNodes(List<String> notifiedNodes) {
		this.notifiedNodes = notifiedNodes;
	}

}
