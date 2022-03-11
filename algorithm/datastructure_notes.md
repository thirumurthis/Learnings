## Data structures

In most of the data structures we will be performing below operations

- Inserting data 
- Deleting data 
- Searching for data
- Traversing the data

Before starting this article, better to have a basic understanding of one or two data structures.

We will see two data structure Arrays and Binary Search Trees. This can be used as guide to understand other data structures.

### Array
 Array is a continuous memory location of the data.

#### Inserting data to an array
  - To insert data in an array, in any programming language is either declare the array and store the data in it.
```
# in java 
int[] arrayVariable = {1,2,3};
```

#### Deleting data in an array 
  - To delete data in an array, being a fixed size data structure. We need remove the item and copy rest of the element to another array which would be of size length -1 (if we delete one element).

#### Searching data in an array
  - To search for data in an array, being a 0 indexed data structure we can loop through the array using for or while loop in programming language.

#### Traversing an array
  - Traversing an array is similar to searching but in this case we will loop through last element in the array.


### Binary Search Tree (BST)
Binary Search tree are node based structure. Contains Root node and two children nodes (left and right). This is a sorted hierarchical data structure.

#### Inserting data in BST 

 - **Inserting data** to an BST tree,
   - If the data to be inserted is very first node then insert it as root node.
   - If the data to be inserted is less than the root node, insert it as left node.
   - If the data to be inserted is greater than the root node, insert it as right node.
  
##### Illustration of creating BST tree with sample data set
   
   - Sample data set of 4, 2, 1, 6, 7, 3 , 4 (Same value will be treated large, in this case two 4's)
   - Below is the way the data will be stored, if the value is greater than the value in that root node place it to the right else to the left.
   
   ```
                      4
                    /    \
                   2       6
                  /  \    /  \
                 1    3  4     7
                 
   ```

#### Deleting data in BST
- **Deleting data** in an BST tree,
  - If the data to be deleted is `a leaf node` (both right and left child is null), delete the node and set the parent pointer (left or right reference) to null.

  - If the data to be deleted is `a non-leaf node`, in this case find the correct child node from sub-tree and replace the child node with node to be deleted.

    - Deleting non-leaf node, has different scenarios: (`i.` and `ii.`)
       - `i.` Removing data node that has only one child (either right or left):
         - If the node to be deleted has only Left child, simply copy the left reference of that node to its parent and delete that node.
         - If the node to be deleted has only Right child, simple copy the right reference of that node to its parent and delete that node.

       - `ii.` Removing data node that has both right and left child:
         - If the node to be deleted has both right and left child, we can do either of the below
            - Find the `minimum in the RIGHT sub-tree` and replace that node with the node to be deleted.
            - OR
            - Find the `maximum in the LEFT sub-tree` and replace that node with the node to be deleted.
   
#####  Illustration of removing data node that has both left and right child (or non-leaf) node.
```
# Removing the node 6 from the tree
                  4                        4                            
                /   \                     /  \
               2      6                  2    7*
             /  \    /  \               / \  /  \ 
            1    3  5    8             1   3 5   8
                        /
                       7 

In simple terms:

  - if the node to be deleted has both right and left nodes, and that has to be removed
  Approach #1:
     - find the node with MINIMUM value from the RIGHT sub-tree (from the node to be removed).
     - Copy that (min right node value) to the node to be rmoved, and delete the duplicate.
  Approach #2:  
     - find the node with MAXIMIM value from the LEFT sub-tree (from the node to be removed).
     - Copy that (max left node value) to the node to be removed, and delete that duplicate.
```
- Properties considered on the above removal approach:
  - In a tree or sub-tree, if the node has a minimum value, it won't have a left child. (If there is a left child, that value will/should be lesser). 

### Traversing the BST 

  - **_DEPTH FIRST SEARCH (DFS):_** 
This can be achieved using recursion or STACK.

| Traversal | Visiting order of the nodes |
|--------|--------------|
| `Pre-Order` traversal | => {root}{left}{right} (Data - Left - Right) |
| `In-Order` traversal   |=> {left}{root}{right} (Left - Data - Right) |
| `Post-Order` traversal|=> {left}{right}{root} (Left - Right - Data) |  

_Note: **In-Order** traversal will produce items in **sorted order**_
    
  Example use case, if the operation are stored as tree in a compiler we can compute runtime behavior, like step 1 should operate before step 2, etc.
  
  - **_BREADTH FIRST SEARCH (BFS):_** 
This can be achieved using QUEUES.

 #### Illustration of DFS traversal with sample data set

 - Pre-order (Root node -> Left node -> Right node)

```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7
   
output:   4 2 1 3 6 5 7 
```

  - In-order  (Left node -> Root node -> right node) (SORT order)
     - visit left, its left until no more left, then visit its root then right.

```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7

output:   1 2 3 4  5 6 7
```
   
  - Post-order (Left node -> right node -> Root node)

```
         4
       /   \
      2      6
    /   \   /  \
   1     3  5    7

output:   1  3 2 5 7 6 4
```

 Similarly the same approach can be used for other data structures like Linked List, Heaps (min/max heap), Tries, Graphs, etc.

