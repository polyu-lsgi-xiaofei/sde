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

import java.net.SocketException;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;

import org.geosde.compute.p2p.Log.Level;


/**
 * This class defines an abstract P2P node.
 * 
 * @author Michael Pantazoglou
 */
public abstract class P2PNode {

	/**
	 * Singleton pattern.
	 */
	public static P2PNode sharedInstance;

	/**
	 * The node's home directory.
	 */
	protected URI home;
	
	/**
	 * The node's name.
	 */
	private String name;
	
	/**
	 * The node's domain.
	 */
	private String domain;

	/**
	 * The node's endpoint address.
	 */
	private URI address;
	
	/**
	 * The node's listening port.
	 */
	private int port;
	
	/**
	 * The node's endpoint.
	 */
	private P2PEndpoint endpoint;

	/**
	 * A flag indicating whether this node is running or not.
	 */
	private boolean started;

	/**
	 * Schedules the execution of this node's connection listener.
	 */
	private ScheduledExecutorService connectionListenerExecutor;
	
	private Future<?> connectionListenerFuture;
	
	/**
	 * The logger of this node.
	 */
	private Log log;
	
	/**
	 * The P2P network where this node belongs.
	 */
	private P2PNetwork network;
	
	private Hashtable<String, SynchronousQueue<P2PResponse>> correlators;

	/**
	 * This constructor should be used in case the node obtains its address 
	 * dynamically.
	 * 
	 * @param home the node's home directory
	 * @param name the node's name
	 * @param domain the node's domain
	 * @param port the node's listening port
	 * @param logLevel the node's logging level
	 */
	protected P2PNode(URI home, String name, String domain, int port, Level logLevel) {
		
		sharedInstance = this;
		this.started = false;
		this.home = home;
		this.name = name;
		this.domain = domain;
		this.port = port;
		this.log = new Log(getClass(), logLevel);
		
		this.correlators = new Hashtable<String, SynchronousQueue<P2PResponse>>();
	}
	
	/**
	 * This constructor should be used in case the node has a static address.
	 * 
	 * @param home the node's home directory
	 * @param name the node's name
	 * @param domain the node's domain
	 * @param address the node's static address
	 * @param port the node's listening port
	 * @param logLevel the node's logging level
	 */
	protected P2PNode(URI home, String name, String domain, URI address, int port, Level logLevel) {
		
		sharedInstance = this;
		this.started = false;
		this.home = home;
		this.name = name;
		this.domain = domain;
		this.address = address;
		this.port = port;
		this.log = new Log(getClass(), logLevel);
		
		this.correlators = new Hashtable<String, SynchronousQueue<P2PResponse>>();
	}
	
	/**
	 * Gets the Home folder of the node
	 * @return 
	 */
	public URI getHome(){
		return this.home;
	}
	
	/**
	 * Gets the Name of the node
	 * @return
	 */
	public String getName(){
		return this.name;
	}
	
	
	/**
	 * Gets the Domain of the node
	 * @return
	 */
	public String getDomain(){
		return this.domain;
	}
	
	/**
	 * Gets the Address of the node	
	 * @return
	 */
	public URI getAddress(){
		return this.address;
	}
	
	/**
	 * Gets the Port of the node
	 * @return
	 */
	public int getPort(){
		return this.port;
	}
	
	public SynchronousQueue<P2PResponse> removeCorrelator(String correlationId) {
		return correlators.remove(correlationId);
	}
	
	public SynchronousQueue<P2PResponse> getCorrelator(String correlationId) {
		return correlators.get(correlationId);
	}
	
	public void addCorrelator(String correlationId, SynchronousQueue<P2PResponse> queue) {
		correlators.put(correlationId, queue);
	}
	
	/**
	 * Gets the data store of this node.
	 * 
	 * @return
	 */
	public abstract P2PNodeDB getNodeDB();

	/**
	 * Performs initialization logic, such as starting up the underlying P2P network and 
	 * establishing the node's endpoint address (in case it is dynamic and not static). 
	 * 
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		network = P2PNetworkFactory.newInstance();
		address = network.startup(home, name, address, port, domain);
	}

	/**
	 * Gets the network where this node belongs. If this method is called 
	 * before the {@link #initialize()} method, it will return <code>null</code>.
	 * 
	 * @return
	 */
	public final P2PNetwork getNetwork() {
		return network;
	}

	/**
	 * Gets the log of this node.
	 * 
	 * @return
	 */
	public final Log getLog() {
		
		if(log == null){
			System.out.println("Log is null");
			this.log = new Log(getClass(), null);
		}
		
		return log;
	}

	/**
	 * Gets this node's endpoint.
	 * 
	 * @return
	 */
	public final P2PEndpoint getEndpoint() {
		if (endpoint == null) {
			endpoint = new P2PEndpoint();
			endpoint.setAddress(address);
		}
		return endpoint;
	}

	/**
	 * Gets the p2p connection listener of this node.
	 * 
	 * @return
	 */
	private P2PConnectionListener getConnectionListener() {
		assert network != null;
		return network.getP2PConnectionListener();
	}
	
	/**
	 * Starts the execution of this node's connection listener.
	 * 
	 * @throws Exception
	 */
	private void startConnectionListener() throws Exception {
		log.info("Starting the p2p connection listener");
		Runnable runnable = new Runnable() {
			public void run() {
				P2PConnectionListener listener = getConnectionListener();
				while (true) {
					try {
						P2PConnection conn = listener.listen();
						if (conn == null) {
							break;
						} else {
							(new Thread(new P2PConnectionHandler(conn))).start();
						}
					} catch (InterruptedException e) {
						try {
							log.info("P2P connection listener interrupted");
							listener.stop();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						return;
					} catch (SocketException e) {
	
					} catch (NullPointerException e) {
						return;
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		};
		connectionListenerExecutor = new ScheduledThreadPoolExecutor(1);
		connectionListenerFuture = connectionListenerExecutor.submit(runnable);
	}

	/**
	 * Stops the execution of this node's connection listener.
	 * 
	 * @throws Exception
	 */
	private void stopConnectionListener() throws Exception {
		log.info("Stopping the p2p connection listener");
		
		connectionListenerFuture.cancel(true);
		connectionListenerFuture = null;
		
		connectionListenerExecutor.shutdown();
		connectionListenerExecutor.shutdownNow();
		connectionListenerExecutor = null;
	}

	/**
	 * Performs all tasks necessary for this node to join the p2p
	 * infrastructure.
	 *
	 * @throws Exception
	 */
	protected abstract void join() throws Exception;

	/**
	 * Performs all tasks necessary for this node to gracefully leave the
	 * p2p infrastructure.
	 * 
	 * @throws Exception
	 */
	protected abstract void leave() throws Exception;

	/**
	 * Starts this node.
	 * 
	 * @throws Exception
	 */
	public final void start() throws Exception {
		if (started) {
			return;
		}
		P2PNodeDB db = getNodeDB();
		if (db != null) {
			db.openConnection();
		}
		initialize();
		startConnectionListener();
		join();
		started = true;
		log.info("Started with address = " + address);
	}

	/**
	 * Stops this node.
	 * 
	 * @throws Exception
	 */
	public final void stop() throws Exception {
		if (!started) {
			return;
		}
		leave();
//		Thread.sleep(3000);
		stopConnectionListener();
		network.shutdown();
		P2PNodeDB db = getNodeDB();
		if (db != null) {
			db.closeConnection();
		}
		started = false;
		log.info("Stopped");
	}
	
	/**
	 * Returns true if the node has started.
	 * 
	 * @return
	 */
	public final boolean isStarted() {
		return started;
	}

	/**
	 * Invokes a request-only p2p service.
	 * 
	 * @param p2pEndpoint the node hosting the p2p service
	 * @param request the service request
	 * @throws Exception
	 */
	public final void invokeOneWayService(P2PEndpoint p2pEndpoint, P2PRequest request) 
			throws Exception {
		P2PConnection p2pConnection = this.getNetwork().connect(p2pEndpoint);
		p2pConnection.send(request);
	}
	
	/**
	 * Invokes a request-response p2p service.
	 * 
	 * @param p2pEndpoint the node hosting the p2p service
	 * @param request the service request
	 * @return the service response
	 * @throws Exception
	 */
	public final P2PResponse invokeTwoWayService(P2PEndpoint p2pEndpoint, P2PRequest request) 
			throws Exception {
		P2PConnection p2pConnection = this.getNetwork().connect(p2pEndpoint);
		p2pConnection.send(request);
		return (P2PResponse)p2pConnection.receive();
	}
	
	/**
	 * Invokes an asynchronous request-response p2p service.
	 * 
	 * @param p2pEndpoint the node hosting the p2p service.
	 * @param request the service request
	 * @return the service response
	 * @throws Exception
	 */
	public final P2PResponse invokeAsyncTwoWayService(P2PEndpoint p2pEndpoint, 
			P2PRequest request) throws Exception {
		P2PConnection p2pConnection = this.getNetwork().connect(p2pEndpoint);
		p2pConnection.send(request);
		SynchronousQueue<P2PResponse> queue = new SynchronousQueue<P2PResponse>();
		addCorrelator(request.getCorrelationId(), queue);
		log.debug("Waiting for response...");
		return queue.take();
	}
	
	/**
	 * Sends the specified request to all specified p2p endpoints. 
	 * 
	 * @param p2pEndpoints the list of p2p endpoints
	 * @param request the p2p request
	 * @return
	 */
	public final void broadcast(List<P2PEndpoint> p2pEndpoints, final P2PRequest request) {
		for (final P2PEndpoint p2pEndpoint : p2pEndpoints) {
			try {
				new Thread(new Runnable() {
					public void run() {
						try {
							invokeOneWayService(p2pEndpoint, request);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
				
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
