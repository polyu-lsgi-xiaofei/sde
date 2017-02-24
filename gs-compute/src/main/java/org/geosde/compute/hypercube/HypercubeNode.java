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
package org.geosde.compute.hypercube;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geosde.compute.hypercube.services.AddNeighborRequest;
import org.geosde.compute.hypercube.services.BroadcastRequest;
import org.geosde.compute.hypercube.services.JoinRequest;
import org.geosde.compute.hypercube.services.RemoveNeighborRequest;
import org.geosde.compute.hypercube.services.UpdateCoverMapVectorRequest;
import org.geosde.compute.p2p.Log.Level;
import org.geosde.compute.p2p.P2PEndpoint;
import org.geosde.compute.p2p.P2PNode;
import org.geosde.compute.p2p.P2PNodeDB;
import org.geosde.compute.p2p.P2PRequest;

/**
 * This class implements the hypercube node.
 * 
 * @author Michael Pantazoglou
 *
 */
public class HypercubeNode extends P2PNode {
	/**
	 * The position vector denotes the actual position of this node on the 
	 * binary hypercube.
	 */
	private int[] positionVector;
	/**
	 * The cover map vector reflects the current coverage of other positions 
	 * by this node on the hypercube. 
	 */
	private int[] coverMapVector;
	/**
	 * The neighbors of this node on the hypercube.
	 */
	private Set<Neighbor> neighborSet;
	/**
	 * Address of a node that is already integrated into the hypercube topology.
	 */
	private URI bootstrapURI;
	/**
	 * The timestamp (in milliseconds) of last use of this node.
	 */
	private long lastUsed = 0;
	
	/**
	 * Performs initialization of the position vector, the cover map vector, 
	 * and the neighbor set of this node.
	 */
	private void initialize() {
		
		positionVector = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
		coverMapVector = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
		
		for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
			positionVector[i] = 0;
			coverMapVector[i] = bootstrapURI==null?1:0;
		}
		
		neighborSet = new HashSet<Neighbor>();
	}
	
	/**
	 * Inherited constructor from superclass.
	 * 
	 * @param home
	 * @param name
	 * @param domain
	 * @param port
	 * @param logLevel
	 * 
	 * @see {@link gr.uoa.di.s3lab.p2p.P2PNode}
	 */
	public HypercubeNode(URI home, String name, String domain, int port,
			Level logLevel) {
		super(home, name, domain, port, logLevel);
		initialize();
	}
	
	/**
	 * Inherited constructor from superclass.
	 * 
	 * @param home
	 * @param name
	 * @param domain
	 * @param address
	 * @param port
	 * @param logLevel
	 * 
	 * @see {@link gr.uoa.di.s3lab.p2p.P2PNode}
	 */
	public HypercubeNode(URI home, String name, String domain, URI address,
			int port, Level logLevel) {
		super(home, name, domain, address, port, logLevel);
		initialize();
	}

	/**
	 * @param positionVector the positionVector to set
	 */
	public void setPositionVector(int[] positionVector) {
		this.positionVector = positionVector;
	}

	/**
	 * @return the positionVector
	 */
	public int[] getPositionVector() {
		return positionVector;
	}

	/**
	 * @param coverMapVector the coverMapVector to set
	 */
	public void setCoverMapVector(int[] coverMapVector) {
		this.coverMapVector = coverMapVector;
	}

	/**
	 * @return the coverMapVector
	 */
	public int[] getCoverMapVector() {
		return coverMapVector;
	}
	
	/**
	 * Flips the bit in the specified position of the cover map vector.
	 * 
	 * @param pos
	 * @param value
	 */
	public void flipCoverMapVectorAt(int pos) {
		coverMapVector[pos] = coverMapVector[pos]==0?1:0;
	}

	/**
	 * @return the neighborSet
	 */
	public Set<Neighbor> getNeighborSet() {
		return neighborSet;
	}
	
	/**
	 * Gets the neighbor in the specified position, or null if no neighbor 
	 * exists in that position.
	 * 
	 * @param positionVector
	 * @return
	 */
	public Neighbor getNeighbor(int[] positionVector) {
		for (Neighbor n : neighborSet) {
			if (Arrays.equals(n.getPositionVector(), positionVector)) {
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Gets the neighbor with the specified network address, or null if no 
	 * such neighbor exists.
	 * 
	 * @param networkAddress
	 * @return
	 */
	public Neighbor getNeighbor(URI networkAddress) {
		for (Neighbor n : neighborSet) {
			if (n.getNetworkAddress().equals(networkAddress)) {
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Gets the Least Recently Used (LRU) neighbor of this node. If there are 
	 * two or more LRU neighbors, the one in the lowest dimension is returned.
	 * 
	 * @return
	 */
	public Neighbor getLRUNeighbor() {
		long minLastUsed = System.currentTimeMillis();
		int minDimension = Hypercube.MAX_NUMBER_OF_DIMENSIONS;
		Neighbor LRU = null;
		for (Neighbor n : this.getNeighborSet()) {
			int dimension = Hypercube.getLinkDimensionality(positionVector, 
					n.getPositionVector());
			if (n.getLastUsed() < minLastUsed) {
				LRU = n;
				minLastUsed = n.getLastUsed();
				minDimension = dimension;
			} else if (n.getLastUsed() == minLastUsed) {
				if (dimension < minDimension) {
					LRU = n;
					minLastUsed = n.getLastUsed();
					minDimension = dimension;
				}
			}
		}
		return LRU;
	}
	
	/**
	 * Removes the neighbor with the specified position vector from the neighbor
	 * set of this node.
	 * 
	 * @param positionVector the specified position vector
	 * @return 1 in case the neighbor is removed, or 0 in case the neighbor is 
	 * not found
	 */
	public int removeNeighbor(int[] positionVector) {
		for (Neighbor n : neighborSet) {
			String Pn = Hypercube.vectorAsString(positionVector);
			String Pv = Hypercube.vectorAsString(n.getPositionVector());
			
			if (Pn.equals(Pv)) {
				neighborSet.remove(n);
				return 1;
			}
		}
		return 0;
	}

	/**
	 * @return the bootstrapURI
	 */
	public URI getBootstrapURI() {
		return bootstrapURI;
	}

	/**
	 * @param bootstrapURI the bootstrapURI to set
	 */
	public void setBootstrapURI(URI bootstrapURI) {
		this.bootstrapURI = bootstrapURI;
	}

	/**
	 * @return the lastUsed
	 */
	public long getLastUsed() {
		return lastUsed;
	}

	/**
	 * @param lastUsed the lastUsed to set
	 */
	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}

	@Override
	public P2PNodeDB getNodeDB() {
		return null;
	}

	@Override
	protected void join() throws Exception {
		
		if (bootstrapURI == null) {
			return;
		}
		
		getLog().info("Sending join request.");
		
		JoinRequest joinRequest = new JoinRequest();
		joinRequest.setJoiningNodeNetworkAddress(this.getAddress());
		
		P2PEndpoint bootstrapEndpoint = new P2PEndpoint();
		bootstrapEndpoint.setAddress(bootstrapURI);
		this.invokeOneWayService(bootstrapEndpoint, joinRequest);
	}
	
	/**
	 * This method is called by {@link #leave()} and is used to return the 
	 * buffering dimension, i.e. the dimension along which this node will 
	 * choose nodes that will take over its positions.
	 * 
	 * @return
	 */
	private int selectBufferingDimension() {
		
		int bufferingDimension = 0;
		
		Set<Neighbor> neighborSet = this.getNeighborSet();
		for (Neighbor n : neighborSet) {
			int lcd = Hypercube.getLinkDimensionality(positionVector, 
					n.getPositionVector());
			if (lcd > bufferingDimension) {
				bufferingDimension = lcd;
			}
		}
		
		return bufferingDimension;
	}
	
	/**
	 * This method is called by {@link #leave()}. It is use to assemble and 
	 * returns the buffering nodes for the given buffering dimension.
	 * 
	 * @param bufferingDimension
	 * @return
	 */
	private Set<Neighbor> assembleBufferingNodes(int bufferingDimension) {
		
		Set<Neighbor> bufferingNodes = new HashSet<Neighbor>();
		Set<Neighbor> neighborSet = getNeighborSet();
		
		for (Neighbor n : neighborSet) {
			int lcd = Hypercube.getLinkDimensionality(positionVector, 
					n.getPositionVector());
			if (lcd == bufferingDimension) {
				getLog().debug("Adding node " + 
						Hypercube.vectorAsString(n.getPositionVector()) + 
						" in buffering nodes");
				bufferingNodes.add(n);
			}
		}
		
		return bufferingNodes;
	}
	
	/**
	 * Computes the cover map vectors of the specified buffering nodes. This 
	 * method is called by {@link #leave()}.
	 * 
	 * @param bufferingNodes
	 * @param bufferingDimension
	 * @return
	 */
	private HashMap<String, int[]> computeBufferingNodesCoverMaps(
			Set<Neighbor> bufferingNodes, int bufferingDimension) {
		
		// Initialize the resulting hash map.
		HashMap<String, int[]> bufferingNodesCoverMaps = new HashMap<String, int[]>();
		
		// Load the set of buffering nodes into a list for easier handling
		List<Neighbor> Lbuf = new ArrayList<Neighbor>(bufferingNodes);
		
		// Initialize the cover map vectors of all buffering nodes
		for (int i=0; i<Lbuf.size(); i++) {
			int[] c = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
			for (int d=0; d<Hypercube.MAX_NUMBER_OF_DIMENSIONS; d++) {
				if (d > bufferingDimension) {
					c[d] = 1;
				} else {
					c[d] = getCoverMapVector()[d];
				}
			}
			Neighbor bufferingNode = Lbuf.get(i);
			int[] p = bufferingNode.getPositionVector();
			bufferingNodesCoverMaps.put(Hypercube.vectorAsString(p), c);
		}
		
		// Now, compute the cover maps for dimensions d > bufferingDimension
		for (int i=0; i<Lbuf.size(); i++) {
			Neighbor ni = Lbuf.get(i);
			for (int j=i+1; j<Lbuf.size(); j++) {
				Neighbor nj = Lbuf.get(j);
				int[] pi = ni.getPositionVector();
				int[] pj = nj.getPositionVector();
				int lcd = Hypercube.getLinkDimensionality(pi, pj);
				bufferingNodesCoverMaps.get(Hypercube.vectorAsString(pi))[lcd] = 0;
				bufferingNodesCoverMaps.get(Hypercube.vectorAsString(pj))[lcd] = 0;
			}
		}
				
		return bufferingNodesCoverMaps;
	}
	
	/**
	 * Computes and returns the covered positions of all buffering nodes.
	 * 
	 * @param bufferingNodes the buffering nodes
	 * @param bufferingDimension the buffering dimension
	 * @param bufferingNodesCoverMaps the pre-computed cover maps of the 
	 * buffering nodes
	 * @return a set of mappings from the positions of buffering nodes to their 
	 * respective list of covered positions
	 */
	private HashMap<String, List<int[]>> computeBufferingNodesCoveredPositions(
			Set<Neighbor> bufferingNodes, int bufferingDimension, 
			HashMap<String, int[]> bufferingNodesCoverMaps) {
		
		HashMap<String, List<int[]>> coveredPositions = new HashMap<String, List<int[]>>();
		
		for (Neighbor n : bufferingNodes) {
			
			// First configure the root position (p_cover)
			int[] p = n.getPositionVector();
			int[] p_cover = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
			for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
				if (i == bufferingDimension) {
					p_cover[i] = p[i]==0?1:0;
				} else {
					p_cover[i] = p[i];
				}
			}
			
			// Now compute the covered positions of buffering node n
			int[] c = bufferingNodesCoverMaps.get(Hypercube.vectorAsString(p));
			List<int[]> L = Hypercube.getCoveredPositions(p_cover, c);
			coveredPositions.put(Hypercube.vectorAsString(p), L);
		}
		
		return coveredPositions;
	}

	@Override
	protected void leave() throws Exception {
	
		if (getNeighborSet().size() == 0) {
			return;
		}
		
		getLog().info("Preparing to leave the hypercube.");
		
		// Select the buffering dimension
		int bufferingDimension = selectBufferingDimension();
		getLog().debug("Buffering dimension: " + bufferingDimension);
		
		// Assemble the buffering nodes
		Set<Neighbor> bufferingNodes = assembleBufferingNodes(
				bufferingDimension);
		
		// Compute the cover map vectors of all buffering nodes
		HashMap<String, int[]> bufferingNodesCoverMaps = 
				computeBufferingNodesCoverMaps(bufferingNodes, 
						bufferingDimension);
		for (String key : bufferingNodesCoverMaps.keySet()) {
			getLog().debug("Cover map of " + key + ": " + 
					Hypercube.vectorAsString(bufferingNodesCoverMaps.get(key)));
		}
		
		// For all buffering nodes, compute their covered positions
		HashMap<String, List<int[]>> bufferingNodesCoveredPositions = 
				computeBufferingNodesCoveredPositions(bufferingNodes, 
						bufferingDimension, bufferingNodesCoverMaps);
		
		// Loop for each buffering node
		for (Neighbor bufferingNode : bufferingNodes) {
			
			int[] p = bufferingNode.getPositionVector();
			String p_s = Hypercube.vectorAsString(p);
			
			int[] c = bufferingNodesCoverMaps.get(p_s);
			
			// Loop for each covered position of the currently processing 
			// buffering node
			List<int[]> L = bufferingNodesCoveredPositions.get(p_s);
			for (int[] coveredPosition : L) {
				
				// Compute all neighbor positions along dimensions 
				// d < Lcd(p, coveredPosition) where c[d] == 0
				int Lcd = Hypercube.getLinkDimensionality(p, coveredPosition);
				for (int d=0; d<Lcd; d++) {
					
					if (c[d] == 1) {
						continue;
					}
					
					int[] dVector = new int[Hypercube.MAX_NUMBER_OF_DIMENSIONS];
					for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
						dVector[i] = i==d?1:0;
					}
					
					int[] neighborPosition = Hypercube.XOR(coveredPosition, 
							dVector);
					
					// Identify the node in my neighbor set which is closest to
					// the neighborPosition
					int minDistance = Hypercube.MAX_NUMBER_OF_DIMENSIONS - 1;
					Neighbor closestNeighbor = null;
					for (Neighbor n : neighborSet) {
						int distance = Hypercube.getDistance(
								n.getPositionVector(), neighborPosition);
						if (distance < minDistance) {
							minDistance = distance;
							closestNeighbor = n;
						}
					}
					
					if (closestNeighbor == null) {
						continue;
					}
					
					StringBuilder sb = new StringBuilder();
					sb.append("The closest neighbor to neighbor position ");
					sb.append(Hypercube.vectorAsString(neighborPosition));
					sb.append(" is ");
					sb.append(Hypercube.vectorAsString(
							closestNeighbor.getPositionVector()));
					getLog().debug(sb.toString());
					
					// The closestNeighbor will become a neighbor of the 
					// currently processing buffering node
					
					// Ask the bufferingNode to add closestNeighbor as its new
					// neighbor...
					AddNeighborRequest addNeighborRequest = 
							new AddNeighborRequest();
					addNeighborRequest.setNewNeighbor(closestNeighbor);
					
					P2PEndpoint bufferingNodeEndpoint = new P2PEndpoint();
					bufferingNodeEndpoint.setAddress(
							bufferingNode.getNetworkAddress());
					invokeOneWayService(bufferingNodeEndpoint, 
							addNeighborRequest);
					
					// ... and vice versa
					addNeighborRequest = new AddNeighborRequest();
					addNeighborRequest.setNewNeighbor(bufferingNode);
					
					P2PEndpoint closestNeighborEndpoint = new P2PEndpoint();
					closestNeighborEndpoint.setAddress(
							closestNeighbor.getNetworkAddress());
					invokeOneWayService(closestNeighborEndpoint, 
							addNeighborRequest);
				}
			}
			
			// Ask from the currently processing buffering node to update its 
			// cover map
			int[] bufferingNodeCoverMap = bufferingNodesCoverMaps.get(p_s);
			bufferingNodeCoverMap[bufferingDimension] = 1;
			UpdateCoverMapVectorRequest updateCoverMapVectorRequest = 
					new UpdateCoverMapVectorRequest();
			updateCoverMapVectorRequest.setNewCoverMapVector(
					bufferingNodeCoverMap);
			
			P2PEndpoint bufferingNodeEndpoint = new P2PEndpoint();
			bufferingNodeEndpoint.setAddress(bufferingNode.getNetworkAddress());
			
			invokeOneWayService(bufferingNodeEndpoint, 
					updateCoverMapVectorRequest);
			
		} // End loop for each buffering node
		
		// Finally, ask from all my neighbors to remove me from their neighbor 
		// sets
		List<P2PEndpoint> neighborEndpoints = new ArrayList<P2PEndpoint>();
		for (Neighbor n : neighborSet) {
			P2PEndpoint p2pEndpoint = new P2PEndpoint();
			p2pEndpoint.setAddress(n.getNetworkAddress());
			neighborEndpoints.add(p2pEndpoint);
		}
		
		RemoveNeighborRequest removeNeighborRequest = 
				new RemoveNeighborRequest();
		removeNeighborRequest.setNeighborPositionVector(positionVector);
		
		broadcast(neighborEndpoints, removeNeighborRequest);
	}
	
	/**
	 * Resolves and returns the list of P2P endpoints-targets for a broadcast.
	 * 
	 * @param minDimension the minimum dimension for the broadcast
	 * @return
	 */
	public List<P2PEndpoint> resolveBroadcastTargets(int minDimension) {
		
		List<P2PEndpoint> broadcastTargets = new ArrayList<P2PEndpoint>();
		
		List<Integer> dimensionsCovered = new ArrayList<Integer>();
		
		Set<Neighbor> myNeighborSet = getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			int linkDimensionality = Hypercube.getLinkDimensionality(
					getPositionVector(), n.getPositionVector());
			if (linkDimensionality > minDimension) {
				if (dimensionsCovered.contains(linkDimensionality)) {
					continue;
				}
				getLog().debug("Broadcast target: " + Hypercube.vectorAsString(
						n.getPositionVector()));
				dimensionsCovered.add(linkDimensionality);
				P2PEndpoint p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(n.getNetworkAddress());
				broadcastTargets.add(p2pEndpoint);
			}
		}
		
		return broadcastTargets;
	}
	
	/**
	 * Initiates a broadcast message containing the specified service request.
	 * It is expected that all nodes receiving the broadcast message will also 
	 * process the service request by means of the appropriate p2p service.
	 * 
	 * @param serviceRequest
	 */
	public void initiateBroadcast(P2PRequest serviceRequest) {
		
		BroadcastRequest broadcastRequest = new BroadcastRequest();
		broadcastRequest.setMinDimension(0);
		broadcastRequest.setServiceRequest(serviceRequest);
		
		int minDimension = -1;
		
		List<Integer> dimensionsCovered = new ArrayList<Integer>();
		Set<Neighbor> myNeighborSet = getNeighborSet();
		for (Neighbor n : myNeighborSet) {
			
			String p_s = Hypercube.vectorAsString(n.getPositionVector());
			
			int linkDimensionality = Hypercube.getLinkDimensionality(
					getPositionVector(), n.getPositionVector());
			if (linkDimensionality > minDimension) {
				if (dimensionsCovered.contains(linkDimensionality)) {
					continue;
				}
				dimensionsCovered.add(linkDimensionality);
				P2PEndpoint p2pEndpoint = new P2PEndpoint();
				p2pEndpoint.setAddress(n.getNetworkAddress());
				broadcastRequest.setMinDimension(linkDimensionality);
				try {
					getLog().debug("Forwarding broadcast message to " + p_s);
					invokeOneWayService(p2pEndpoint, broadcastRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Gets the next neighbor of this node in the shortest path to the specified 
	 * destination position.
	 * 
	 * @param destinationPosition the position vector of the destination node
	 * @return
	 */
	public Neighbor getNextNeighborInShortestPath(int[] destinationPosition) {
		
		if (isPositionCovered(destinationPosition)) {
			return null;
		}
		
		int[] xor = Hypercube.XOR(positionVector, destinationPosition);
		
		// The dimension of the next neighbor in the shortest path equals to the
		// position of the first '1' found in the xor vector.
		for (int i=0; i<Hypercube.MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (xor[i] == 1) {
				// We found the dimension of the next neighbor in the shortest path
				for (Neighbor n : neighborSet) {
					int dim = Hypercube.getLinkDimensionality(positionVector, n.getPositionVector());
					if (dim == i) {
						// We found the next neighbor in the shortest path
						return n;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns true if the specified position is covered by this node.
	 * 
	 * @param position the specified position vector
	 * @return true or false
	 */
	public boolean isPositionCovered(int[] position) {
		String positionAsString = Hypercube.vectorAsString(position);
		List<int[]> coveredPositions = Hypercube.getCoveredPositions(positionVector, coverMapVector);
		for (int[] p : coveredPositions) {
			String pAsString = Hypercube.vectorAsString(p);
			if (pAsString.equals(positionAsString)) {
				return true;
			}
		}
		return false;
	}

}
