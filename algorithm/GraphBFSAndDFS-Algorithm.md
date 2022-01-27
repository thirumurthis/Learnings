## For an un-directed graph (no direction), we use visited node to avoid visting the same node again

### DFS algorithm Iterative and Recurisive

```
    DFS-iterative (G, s):                                   //Where G is graph and s is source vertex
      let S be stack
      S.push( s )            //Inserting s in stack 
      mark s as visited.
      while ( S is not empty):
          //Pop a vertex from stack to visit next
          v  =  S.top( )
         S.pop( )
         //Push all the neighbours of v in stack that are not visited   
        for all neighbours w of v in Graph G:
            if w is not visited :
                     S.push( w )         
                    mark w as visited
```
```
    DFS-recursive(G, s):
        mark s as visited
        for all neighbours w of s in Graph G:
            if w is not visited:
                DFS-recursive(G, w)
                
```
### BFS Iterative uses Queue
```
BFS (G, src)                   //Where G is the graph and s is the source node
      let Q be queue.
      Q.enqueue( src ) //Inserting src in queue 

      mark src as visited.
      while ( Q is not empty)
           //Removing vertex from queue,whose neighbour will be visited now
           v  =  Q.dequeue( )

          //processing all the neighbours of v  
          for all neighbours w of v in Graph G
               if w is not visited 
                        Q.enqueue( w )             //insertes/stores w in Q to further visit its neighbour
                        mark w as visited.
```
