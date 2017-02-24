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

import java.util.concurrent.SynchronousQueue;

/**
 * 
 * @author Michael Pantazoglou
 *
 */
public class BPELActivityListener<E> {
	
	private SynchronousQueue<E> queue;
	
	private String p2pSessionId;
	
	private String activityId;
	
	public BPELActivityListener() {
		this.queue = new SynchronousQueue<E>();
	}
	
	public BPELActivityListener(String p2pSessionId, String activityId) {
		this.queue = new SynchronousQueue<E>();
		this.p2pSessionId = p2pSessionId;
		this.activityId = activityId;
	}
	
	public BPELActivityListener(SynchronousQueue<E> queue) {
		this.queue = queue;
	}
	
	public SynchronousQueue<E> getQueue() {
		return queue;
	}

	/**
	 * Listens for a notification from the activity notifier.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public E listen() throws InterruptedException {
		return queue.take();
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

}
