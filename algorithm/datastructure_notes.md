### Starting to learn data structures

In most of the data structures we will be performing below operations

- Inserting data 
- Deleting data 
- Searching for data
- Traversing the data

Before starting this article, better to have a basic understanding of one or two data structure.

#### Array:
 Array is a continous memory location of the data.

  - **Inserting data** to an array, 
    - In any programming language is either decaling an array and storing values to it.
  - **Deleting data** in an array, 
    - Since Arrays are declared with fixed size, we probably need to create another array with current array lenght -1 and copy the remaing data.
  - **Searching for data** in an array, 
    - We can achieve this using for or while loop since the array are index from 0.
  - **Traversing** in an array,
    - Is similar to searching, but we traverse till the last element.


#### Binary Search Tree:
Binary Search tree are node based structure. Contains Root node and two children nodes (left and right). This is a sorted hierarical data structure.

 - **Inserting data** to an BST tree,
   - If the data to be inserted is very first node then insert it as root node.
   - If the data to be inserted is less than the root node, insert it as left node.
   - If the data to be inserted is greater than the root node, insert it as right node.
  
- **Deleting data** in an BST tree,
  - If the data to be deleted is `a leaf node` (both right and left child is null), delete the node and set the parent pointer (left or right reference) to null.
  - If the data to be deleted is `a non-leaf node`, in this case find the correct child node from sub-tree and replace the child node with node to be deleted.
    - Deleteing non-leaf node, has different scenarios:
       - i. Removing data node that has only one child (either right or left):
         - If the node to be deleted has only Left child, simply copy the left reference of that node to its parent and delete that node.
         - If the node to be deleted has only Right child, simple copy the right reference of that node to its parent and delete that node.

       - ii. Removing data node that has both right and left child:
         - If the node to be deleted has both right and left child, we can do either of the below
            - 1. Find the `minimum in the RIGHT sub-tree` and replace that node with the node to be deleted.
            - OR
            - 2. Find the `maximum in the LEFT sub-tree` and replace that node with the node to be deleted.

- **Traversing data** in a BST,
  - _DEPTH FIRST SEARCH (DFS):_ This can be acheived using recursion or STACK.
    - `Pre-Order` search  - visiting order => <root><left><right> (D L R)
    - `In-Order` search   - visiting order => <left><root><right> (L D R) __Note: This will render the data in sorted order__
    - `Post-Order` search - visiting order => <left><right><root> (L R D)   D- data, L- left, R - Right
  
  Example use case, if the operation are stored as tree in a compiler we can compute runtime behaviour, like step 1 should operate before step 2
  
  - _BREADTH FIRST SEARCH (BFS):_ This can be acheived using QUEUES
    
 Similarly the same approach can be used for other data structures like Linked List, Heaps (min/max heap), Tries, Graphs, etc.
