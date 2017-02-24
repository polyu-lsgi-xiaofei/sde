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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Implements various utility methods that are needed by the hypercube protocols.
 * 
 * @author Michael Pantazoglou
 *
 */
public class Hypercube {
	
	/**
	 * Maximum number of dimensions in the hypercube.
	 */
	public static final int MAX_NUMBER_OF_DIMENSIONS = 3;
	
	/**
	 * Gets the distance between two positions on the hypercube, i.e. between 
	 * two nodes located at these positions, by calculating the Hamming distance
	 * between their position vectors.
	 * 
	 * @param p1 the position vector of the first node
	 * @param p2 the position vector of the second node
	 * @return the distance between the two nodes
	 */
	public static int getDistance(int[] p1, int[] p2) {
		
		assert p1.length == MAX_NUMBER_OF_DIMENSIONS;
		assert p2.length == MAX_NUMBER_OF_DIMENSIONS;
		
		int distance = 0;
		for (int i=0; i<MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (p1[i] != p2[i]) {
				distance++;
			}
		}
		
		return distance;
	}
	
	/**
	 * Gets the <i>dimensionality</i> of the link between two nodes.
	 * 
	 * @param p1 the position vector of the first node
	 * @param p2 the position vector of the second node
	 * @return the link dimensionality of the two nodes
	 */
	public static int getLinkDimensionality(int[] p1, int[] p2) {
		
		assert p1.length == MAX_NUMBER_OF_DIMENSIONS;
		assert p2.length == MAX_NUMBER_OF_DIMENSIONS;
		
		for (int i=0; i<MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (p1[i] != p2[i]) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Returns TRUE if the nodes with the two specified position vectors are 
	 * immediate neighbors on the hyercube.
	 * 
	 * @param p1 the position vector of the first node
	 * @param p2 the position vector of the second node
	 * @return a boolean value
	 */
	public static final boolean areImmediateNeighbors(int[] p1, int[] p2) {
		
		assert p1.length == MAX_NUMBER_OF_DIMENSIONS;
		assert p2.length == MAX_NUMBER_OF_DIMENSIONS;
		
		return (getDistance(p1, p2) == 1);
	}
	
	private static List<int[]> createPositions(List<int[]> currentPositions, 
			int d) {
		List<int[]> newPositions = new ArrayList<int[]>();
		for (int[] position : currentPositions) {
			newPositions.add(position);
			int[] newPosition = Arrays.copyOf(position, position.length);
			newPosition[d] = position[d]==0?1:0;
			newPositions.add(newPosition);
		}
		return newPositions;
	}
	
	/**
	 * Returns the list of positions currently covered by the node with the 
	 * specified position vector and cover map vector.
	 * 
	 * @param positionVector
	 * @param coverMapVector
	 * @return
	 */
	public static final List<int[]> getCoveredPositions(int[] positionVector, 
			int[] coverMapVector) {
		
		// Initialize the list of covered positions
		List<int[]> coveredPositions = new ArrayList<int[]>();
		coveredPositions.add(positionVector);
		
		for (int i=0; i<MAX_NUMBER_OF_DIMENSIONS; i++) {
			if (coverMapVector[i] == 0) {
				continue;
			}
			coveredPositions = createPositions(coveredPositions, i);
		}
		
		return coveredPositions;
	}
	
	/**
	 * Applies the XOR operator to the two specified vectors and returns the 
	 * result, which is another vector.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static int[] XOR(int[] vector1, int[] vector2) {
		
		assert vector1.length == vector2.length;
		
		int[] result = new int[vector1.length];
		for (int i=0; i<result.length; i++) {
			if (vector1[i] == vector2[i]) {
				result[i] = 0;
			} else {
				result[i] = 1;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the specified vector as string.
	 * 
	 * @param vector
	 * @return
	 */
	public static String vectorAsString(int[] vector) {
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<vector.length; i++) {
			sb.append(vector[i]);
		}
		
		return sb.toString();
	}
	
	/**
	 * Generates and returns a random position vector.
	 * 
	 * @return a random position vector as an int array
	 */
	public static int[] getRandomPositionVector() {
		
		Random random = new Random();
		
		int[] p = new int[MAX_NUMBER_OF_DIMENSIONS];
		for (int i=0; i<MAX_NUMBER_OF_DIMENSIONS; i++) {
			p[i] = random.nextBoolean()?1:0;
		}
		
		return p;
	}
	
	/**
	 * For testing purposes.
	 * 
	 * @param strings
	 */
	public static void main(String...strings) {
		
		int[] positionVector = {0,1,1};
		int[] coverMapVector = {1,0,1};
		
		List<int[]> coveredPositions = getCoveredPositions(positionVector, 
				coverMapVector);
		for (int[] coveredPosition : coveredPositions) {
			for (int i=0; i<MAX_NUMBER_OF_DIMENSIONS; i++) {
				System.out.print(coveredPosition[i]);
			}
			System.out.println();
		}
	}

}
