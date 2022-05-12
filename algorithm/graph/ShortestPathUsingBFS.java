package com.graph.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ShortestPathUsingBFS {
	
	/*
	 Below is the graph path from 2 to 3 -> 2,5,4 
		1 ---->2 
	   /^     |
	  v |     |
  	 3  |     |
  	 ^  |     |
	  \ |     v
		4 <-- 5
		
		 Shortest path from 1 to 3 is 1,3
		 Shrotest path from 2 to 3 is 2,5,4,3
	 */
	
	//Create a Map for constructing the graph
	// Adjacency List representation
	static Map<Integer, List<Integer>> graph = new HashMap<>();
	
	public static void main(String[] args) {
		
		//create the graph adding the vertex and edges
		createGraph();
		printGraph();
		List<Integer> output = bfsShortDistance(2, 3);
		System.out.println(output.toString());
		output = bfsShortDistance(5, 3);
		System.out.println(output.toString());
		
		output = bfsShortDistance(1, 3);
		System.out.println(output.toString());
	}

	/*
	 * Variation of BFS
	 * Create Queue
	 * Create Map to hold the current node and its parent
	 * Add the fromNode to the map with the value as null
	 *    - the null value will be an indicator to stop backtracking
	 *   Iterate the queue until it is empty
	 *    - parent <- remove the item from the queue 
	 *    - check if that is the toNode 
	 *    - then break the loop
	 *    
	 *    - get the neighboring node of the current node 
	 *    - check if neighbor is already visited (use the Map created to hold current to parent mapping)
	 *        - Add the neighbor to the Queue 
	 *        - Add the neighbor as key and the parent as value to the list 
	 *        
	 * By now if the above is completed, the Map should contain the paths
	 *     - if the toNode is not present in the map then there is no path exists.
	 *     
	 * Create a List to hold the path since we are going to back track the path
	 *   - Since we know the toNode, use that to get the parent of it
	 *   - with the fetched value of the key fetch the parent and track till we reach null
	 *   
	 * For scenario (2, 3)
	 * the constructed map is Map :- {1=4, 2=null, 3=4, 4=5, 5=2}
	 * 
	 *    3 -> parent 4
	 *    4 -> parent 5
	 *    5 -> parent 2
	 *    2 -> parent null << stop
	 *    since we add at 0 index, the item will be pushed down in java
	 * 
	 */
	public static List<Integer> bfsShortDistance(int fromNode, int toNode) {
		
		Queue<Integer> queueToVisitNeigbhors = new LinkedList<>();
		Map<Integer,Integer> trackCurrentNodesParent = new HashMap<>();
		// Adding the null to the value or some unique key
		// this will be the indicator when we back track the flow from toVertex
		trackCurrentNodesParent.put(fromNode,null);
		queueToVisitNeigbhors.add(fromNode);
		while(!queueToVisitNeigbhors.isEmpty()) {
			int target = queueToVisitNeigbhors.poll();
			// if we reached the target node or toNode
			// we will stop there and break the loop
			if(target == toNode) {
				break;
			}
			for(int neighbor: graph.get(target)) {
				// We use the below condition to identify cycles and avoid it
				// if we already visited a node, we don't need to add it again.
				if(!trackCurrentNodesParent.containsKey(neighbor)) {
					queueToVisitNeigbhors.add(neighbor);
					trackCurrentNodesParent.put(neighbor, target); // current node to target (parent)
				}
			}
		}
		// If the target or toNode is not present in the Map then there is no path simply return null,
		if(trackCurrentNodesParent.get(toNode)==null) {
			return null;
		}	

		System.out.println("Map :- "+ trackCurrentNodesParent.toString());
		//From the Map where nodes and its parent are tracked, 
		// we need to back track from the key and parent till we reach null
		List<Integer> result = new ArrayList<>();
		// we set the toNode to the target, and use that to back track
		Integer target = toNode;
		while(target != null) {
			// adding the value at the 0 index will keep pushing the items 
			//previously added to the bottom
			result.add(0, target);
			target=trackCurrentNodesParent.get(target);
		}
		return result;
	}
	
	/*
	 * Separated method to create the graph represented as above
	 */
	public static void createGraph() {
		createVertexAndAddToGraph(1, 2);
		createVertexAndAddToGraph(2, 5);
		createVertexAndAddToGraph(5, 4);
		createVertexAndAddToGraph(4, 3);
		createVertexAndAddToGraph(1, 3);
		createVertexAndAddToGraph(4, 1);
	}
	/*
	 * Representation for the graph, adding the vertex and representing edges
	 */
	public static void createVertexAndAddToGraph(int fromVertex, int toVertex) {
		
		graph.putIfAbsent(fromVertex, new ArrayList<Integer>());
		graph.get(fromVertex).add(toVertex);
	}
	/*
	 * Print the graph 
	 */
	public static void printGraph() {
		
		for(Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
			System.out.println(entry.getKey() + " - "+entry.getValue().toString());
		}
	}
  
}
