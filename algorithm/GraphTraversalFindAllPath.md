#### Finding path from source to destination

## BFS
 - BFS (uses Queue), Iterative
 - The time complexity is `O(N^2 * 2^N)`
 - Space complexity is `O(N^2)`
 
```java

public static List<List<Integer>> bfsPathsIterative(List<List<Integer>> graph, int src, int target ){
		
    // define a queue which holds a list of vertex
	Queue<List<Integer>> queue = new LinkedList<>();
    
    // Add the first node to the queue in this case it will be visited
	queue.add(Arrays.asList(src));
    
    // Define a new list to hold all the possible path
	List<List<Integer>> result = new ArrayList<>();
		
	while(!queue.isEmpty()) {
           // Like usual BFS algorithm, fetch the last node, here that is a list
	      List<Integer> pathList = queue.poll();
       
          // Since we are pushing a list of vertex visited in the Queue, 
          // the very last item will be the current node which will has adjacent node/vertex
	     Integer currentNode= pathList.get(pathList.size()-1);
       
          // get the very last node and compare if that is already the target
	     if(currentNode.equals(target)) {
		   result.add(new ArrayList<>(pathList));
	   }
	   
          // like in the BFS we iterate the neighbour node
	     for(Integer neighbor: graph.get(currentNode)) {
                 // we create a new List with the current visited path, and add the adjacent node to it.
		    List<Integer> neigbhoursPathList = new ArrayList<>(pathList);
		    neigbhoursPathList.add(neighbor);
         
	            // add this to the queue,
		     queue.add(neigbhoursPathList);
	       }
	   }
	return result;
   }
```

## DFS (uses Stack)
 - Below code uses Iterative approach to calcuate the paths

```java
public static List<List<Integer>> dfsPathsIterative(List<List<Integer>> graph, Integer src, Integer dest){
	Stack<List<Integer>> stack = new Stack<>();
	List<List<Integer>> result = new ArrayList<>();
	stack.push(Arrays.asList(src));

        while(!stack.empty()) {
			
		List<Integer> pathList = stack.pop();
		Integer currentNode = pathList.get(pathList.size()-1);
					
		if(currentNode.equals(dest)) {
			result.add(new ArrayList<>(pathList));
		}
		for(Integer neighbours: graph.get(currentNode)) {
			    List<Integer> neighboursPathList = new ArrayList<>(pathList);
			    neighboursPathList.add(neighbours);
				stack.push(neighboursPathList);
		}
	}
	return result;
}
```

 - Below code uses Recursion to find possible path from src to destination

```java
  /* The result will be in the pathTraverse variable.*/

public static void dfsRecPath(List<List<Integer>> graph, Integer vertex,Integer target,List<List<Integer>> pathTraverse, List<Integer> path) {
    //Add the path visited to the list
     path.add(vertex);
    
    // If the current vertex is equal to the target, then we have one possible path
    // add the visited path to the resultant list.
    if(vertex.equals(target)) {
	 pathTraverse.add(new ArrayList<>(path));
	return;
     }
 
    for(Integer neighbor : graph.get(vertex)) {
	  dfsRecPath(graph, neighbor, target, pathTraverse,path);
	  path.remove(path.size()-1);
     }
}
```


- Main program and utilities for using the BFS
```java
  
class GraphTraversal{
  
   public static void main(String[] args) {
	int [][] edges =  {{1,2},{3},{3},{}}; //output:[[0,1,3],[0,2,3]]
	//int [][] edges = {{4,3,1},{3,2,4},{3},{4},{}};  //Output: [[0,4],[0,3,4],[0,1,3,4],[0,1,2,3,4],[0,1,4]]
				
	List<List<Integer>> graph = buildGraph(edges);
	printGraph(graph);
		
	List<List<Integer>> dfsPathTraverse = new ArrayList<>();
        System.out.println("DFS Iterative: ");
	dfsPathTraverse = dfsPathsIterative(graph, 0, graph.size()-1);
	System.out.println(dfspathTraverse);
	
	System.out.println("BFS iterative ");
	List<List<Integer>> bfsPathsTraverse = new ArrayList<>();
	bfsPathTraverse= bfsPathsIterative(graph, 0, graph.size()-1);
	System.out.println(bfsPathTraverse);   
  }

  public static List<List<Integer>> buildGraph(int[][] edges){
	List<List<Integer>> graph = new ArrayList<>();
		
	for(int i=0; i<edges.length; i++) {
		graph.add(new ArrayList<>());
		for(int j=0;j<edges[i].length;j++) {
		    graph.get(i).add(edges[i][j]);
		}
	}
	return graph;
}
	
public static void printGraph(List<List<Integer>> graph) {
		
     for(int i=0;i<graph.size();i++) {
	  System.out.println(i+" -> "+graph.get(i));
	}
   }
}
```
