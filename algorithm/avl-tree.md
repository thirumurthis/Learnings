
AVL - is `Balanced binary tree` invented by Adelson-Velsky & Landis.

 - Follows all binary tree structural constraints.
 - Search an enumeration are identical to Binary tree.
 - In AVL, Insertion and Deletion differ only in using the Balance algorithm if necessary  (self- balancing)
 
Concepts:
   - Self-balancing.
   - Height of tree.
   - Balance factor.
   - Right/Left heavy.

Unbalanced Binary Tree can become a linked list (eg: add 1,2,3,4 in tree)
  - cost or performance is `O(n)` in this case.

Balanced Binary Tree:
  - cost in this case is `O(log n)`
  
**Rule for a tree to be balanced:** 
   - `Height of left and right tree differ by at most 1.`

Unbalanced tree example:
```
  1
    \
      2
       \ 
         3 
          \
           4
  - Root node has no childrens on left (so left height of root node is 0 )
     - On the right there is a children 2 (Height of this node relative to root is 1)
     - Value of node 2 has right child 3. (Height of this node relative to root is 2)
     - Value of node 3 has right child 4. (Height of this node relative to root is 3)
  - Height of the root node on the right is 3
  
  The difference between her is 0-3 ~ 3 (this is unbalanced)
  To be balanced the difference to be at most 1
```

Balanced Tree example: (by adding 1,2,3,4 values)
```
1
  \
   2
    \
     3
 
 1 becomes the root node
 2 ( 2>1 - goes to the right)
 3 (3>1, check right 3>2 goes to the right)
 Now the tree is not balanced (since left height 0, right height 2 (difference 2)
 
 
 Self Balancing (Run the Balancing algorithm) and repersentation will be like :
 
       2
      /  \
     1    3
 
   Now the tree is balanced now, difference of left and right height is 0 (so balanced)
   Note: now 2 became the root. 
 
 Adding 4 to the node structure now
 
        2
       / \
      1    3
             \
              4
 This would be the final balanced tree
```
So balancing algorithm runs on every insertion if necessary, to satisfy the difference between left and right tree be atmost 1.

Example when the balancing happens, insert 4,2,3 data

```
       4
      /
     2
      \
       3
      
   - parent of node value 3 is balanced
   - parent node value of 4 is not balanced.
   - now the balance operation executes, after balancing
   
       3
      /  \
     2    4
```

Deletion:
  - This is similar to the binary deletion
     - find the node
     - Child node moved to retain tree rules
  - Balancing algorithm runs for every parent node after deletion
  
```
                  5
                /  \
               4     7
                    / \
                   6   8

Delete the node 4:
    - the value of the node is found, node is deleted
    - node the node is not balanced (left and right height difference is 0-2 ~2)
    - run balancing algorithm
After rotation,
         7
        /  \
       5    8
        \
          6
  Now, left height of tree (root node) is 2 and right is 1, difference is ~ 1
```

Balancing Algorithm Details:

  - Balancing is done using node rotation
  - Rotation occurs at the time of insertion and deletion point
  - Rotation changes the physical structure of the tree following the tree constraints.
       - Smaller values on left, greater/equal on the right
  
  Different type of rotation:
   Right rotation
   Left rotation
   Right Left rotation
   Left right rotation
