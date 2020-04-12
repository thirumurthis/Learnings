## Tree data structure
 - root node
 - leaf node
 
 The fundamental rule is that there will be only one path exists between root to the any node in the tree.
 
 ### Binary Tree
   - Hierarchy of data
   - Root node
      - Left Child 
      - Right Child
   - Each child itself a tree.
 
 #### Binary Search Tree
 
   - **`Same as binary tree`**
   - Sorted Hierarchy of Data 
   - There are no structural change this as same as binary tree, impose additional data rule.
       - All the data are sorted
       - `Smallest` value on the `left`
       - `Largest` value on the `right`
        
 ###### Adding Data to Binary search tree
   
   Assume data set of 4, 2, 1, 6, 7, 3 , 4
   - Same value will treat it as large.
   
   Below is the way the data will be stored, if the value is greater than the value in that node place it to the right else to the left.
   
   ```
                      4
                    /    \
                   2       6
                  /  \    /  \
                 1    3  4     7
                 
   ```

  ###### Searching (using recursive)
  
  ```
   findNode (Node current, Data value){
     if (current  == null) return null;
     if (current.value == value) return current;
     
     if(value < current.value) return findNode(current.left,value);
     return find (current.right, value);
     }
  ```
  For searching a value on the sample tree structure above, doesn't require to go through all the nodes. 
  Only a subset of the data is visited.
  
  ###### Remove (complex algorithm) \[No bi-directional links between nodes \]
   - Find the node to be deleted
      -if node does not exists, exit
      
   - Leaf node
      - remove parent's node pointer is set to null (so it is deleted)
      
   - non-leaf node
      - find the correct child, to replace the node that we are deleting.
      - 3 scenarios
      
Scenario: 1     
          - Remove node that has no right child
             - the serached/matched node doesn't has no child on the right
             - promote the left node to the identified node.
         
Removing the node 8, from the tree         
 ```
 
                       4                                    4
                    /     \                               /    \ 
                   2        8                            2       6
                 /   \     /                           /   \    /  \
                1     3   6                           1     3   5   7
                         / \
                        5   7
    # once the node 8 is identified
    # promote the node 6 (on the left) to that identified node
 ```
