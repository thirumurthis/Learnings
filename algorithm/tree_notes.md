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
      - **_3 scenarios_** exits in deleting non-leaf node, listed below
      
Scenario: 1

 - Remove node that has no right child
   - if the searched/matched node doesn't have child on the right, then
   - promote the left node to the identified node.
         
Removing the node 8 from the tree         
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

Scenario 2:

 - Removing a node that has right child NO left child or node.
 - Removing a node that has left child NO right child or node.

Removing the node 6 from the tree   
```
                  4                       4
                /   \                    /  \
               2      6                 2     7*
             /  \    /  \              / \   / \
            1    3  5    7            1   3  5  8
                           \
                            8 
    
 # removing 6, it has right child 7 which has no left child.
 # promote the right child (7) node to node at 6. (like linking the subtree of 6 to its parent).
 # Note 7 should become the parent.
 Doing so the invariant structure of the tree is not breaked.
 
```

Scenario 3:
  - The removing node that has both right and left node.

Removing the node 6 from tree.
```
                  4                        4                            
                /   \                     /  \
               2      6                  2    7*
             /  \    /  \               / \  /  \ 
            1    3  5    8             1   3 5   8
                        /
                       7 

The node 6 has a right child 8 which has left child 7

The right child's left most node will replace the searched/identified node.
In this case 7 will replace the 6 node.
Doing so the invariant structure is retained.

in simple terms:

  - if the node to be deleted has both right and left nodes, and that has to be removed
  Approach #1:
     - find the node with MINIMUM value from the RIGHT sub-tree from the node to be removed.
     - Copy that (min right node value) to the node to be rmoved, and delete the duplicate.
  Approach #2:  
     - find the node with MAXIMIM value from the LEFT sub-tress from the node to be removed.
     - Copy that (max left node value) to the node to be removed, and delete that duplicate.
```
- Properties:
    - In a tree or sub-tree, if the node has a minimum value, it won't have a left child. (If there is a left child, that value should be lesser). There may/may not be right child, if we had right child then this will be scenario 2.
    - 

#### Tree Traversals

 - Breadth First Search
 - Depth First Search ( Pre-Order, In-Order, Post-Order) - can be achived with recurrsion
    ```
     <root><left><right>  => D L R - pre-order  ( D - root node; L - Left node;  R - Right node)
     <left><root><right>  => L D R - in-order (sorted)
     <left><right><root>  => L R D - post-order
    ```
   - Pre-order (Root node -> Left node -> Right node)
   ```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7
   
   4 2 1 3 6 5 7 
   ```

  - In-order  (Left node -> Root node -> right node) (SORT order)
     - visit left, its left until no more left, then visit its root then right.
   ```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7
   1 2 3 4  5 6 7
   ```
   
  - Post-order (Left node -> right node -> Root node)
  ```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7
   1  3 2 5 7 6 4
 ```

Usage of Tree traversal:
  - Traversal (pre-order and post-order) is used in mathematical evaluation
  - used in runtime behaviour of in compiler. like step 1 should operate before step 2, where the operation is stored as tree.  
  
 - Program example to try using tree:
    - Try to get list of words from the user and sort using binary tree.


-----------------

### Height vs Depth:
  `Height` - is the number of edges in longest path from root to Leaf node. 
     - What is the height of the node? - the number of edges between that node to the leaf node.
     - Height of the leaf node is 0.
     - The root node will have the height.
  `Depth` - number of edges from root to that node or leaf node.
       - Root node the depth is 0
       - The leaf node will have max depth.
  
  Note: 
    - The max depth = height of the tree. 
    - Node connected by edges is called vertices.
    
- Algorithm to find the height of the tree,
   - Max of (Left sub-tree, Right sub-tree) + 1. 

```
Binary tree height = 4  for data = {4,2,1,6,7,9,3,5,8,};  - time complexity is O(n)
Explanation of determining the height 
                                    height of left sub-tree of 4 is 1            height of right sub-tree of 4 is 3    in order to consder the edges from root, we add 1
                  4                                2                                  6
                /    \                           /   \                              /   \     
               2       6                        1     3                            5      7
              /  \    /  \                                                                 \
             1    3   5   7                                                                 9
                           \                                                                  \
                             9                                                                 8
                              \
                                8
```


```
// height of the tree from leaf node (is number of edges from root to leaf node) [ Edges are lines connecting the nodes.]
// rightheight <- findHeight(tree.right)
// leftheight <- findHeight(tree.left)
// return max (rightheight,leftheight)+1

 int findHeight(Tree tree){
        if(tree == null){
            return -1;  
            // consider if there is only one node, if we return null the left and right sub tree will be 0 and max (0,0)+1, will be returning 1
            // so we need to return -1, since we are already including 1.
        }
         return Math.max(findHeight(tree.getLeftNode()),findHeight(tree.getRightNode()))+1;
    }
```
---------------------

## Hash Table
- Type of datastructure Associative Array
- Storage of key/Value pairs.
- Each key is unique.

The key type is mapped to an index.

Adding a value, 
  - get the index (some function) to store the object using the hash function.
  - store the information at the index in an array

Hashing process derives a fixed size result for any arbitary input.
  - Any string with any length will returns the fixed size result.
  - Hash function returns the same size.

Hash function should be `stable` (should be invariant)

- `Uniformity` the hash value should be uniformly distributed through available space.

- `Efficient` The cost of generating hash must be balanced with application needs.

- `Security` the cost of finding data that produced a given hash is prohibitive.

Sample hash function implementation
`Additive`
```
# this is just an example (Additive).
fo -> 102 111 = 324 (add each value since 32 bit)
of  -> will also yield same result.
```
`Folding`
```
# better one using folding 
use first 4 characters, with those bytes and scramble to 32 bit value. we have different values.

ram is helping people
ram -> hash
is h -> hash (then add to the above hash value)
..
..
```

Note: Use the available Hashing function or algorithm, it is already provied.


| Name | stable | Uniform | Efficient | Secure |
|---|---|---|---|---|
| Additive | Y | N | Y | N |
| Folding | Y | Y | Y | N |
| CRC32 | Y | Y | Y | N|
| MD5 | Y | Y | N | N |
| SHA-2 | Y | Y | N | Y |

