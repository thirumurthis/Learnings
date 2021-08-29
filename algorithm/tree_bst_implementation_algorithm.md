
#### Depth first search  (uses stack datastructure)
   - In-Order without recursion 
   - Vist Left Node - Root Node - Right node (LDR)

- Algorithm

```
1. Initialize an empty stack.
2. Intialize variable currentNode as rootNode (passed in function)
3. If currentNode is not null,
     1. then push currentNode to stack
     2. set currentNode = current.left
4. If currentNode == null AND stack is not empty
     1. then pop top item from stack (set it to currentNode)
     2. print the popped item/ add the popped item to list
     3. set currentNode = popped item.right ( set currentNode= currentNode.right, since setp 4.1)
5. If currentNode is null and stack is empty, stop or return the list.
```
- code
```java 
//method to print the tree item in in-order without recursion
List<Integer> inOrderTraverseIteration(Node tree){
        List<Integer> result = new ArrayList<>();
        
        Stack<Node> stack = new Stack<>();
        Tree traverseNode = tree;
        while(true) {
            if(traverseNode!=null) {
                stack.push(traverseNode);
                traverseNode = traverseNode.getLeftNode();
            }
            if(traverseNode == null && !stack.isEmpty()){
                traverseNode = stack.pop();
                result.add(traverseNode.getData());
                traverseNode = traverseNode.getRightNode();
            }
            if(traverseNode == null && stack.isEmpty()){
                return result;
            }
        }
    }
//Node class
class Node{
    Node leftNode=null;
    int data;
    Node rightNode = null;
    boolean isLeafNode (){
        return this.leftNode == null && this.rightNode == null;
    }
  //getter setter methods
}
```

#### Finding a min and max algorithm
- with out recurrsion
  - Note: The minimum value of the BST tree will be the left most leaf node.
  - Note: The maxumum value of the BST tree will be the right most leaf node.
  
- To find Minimum value.
```
# rootNode is the passed node to the function
1. if rootNode is null, return error or some distinct value.
2. iterate till rootNode is not null.
    1. for each iteration set rootNode = rootNode.left
3. return rootNode.data. (step 2. breaks when the leaf node is reached, rootNode.left is null)
```
- code
```java
int findMin(Node rootNode){
  if (rootNode == null) return -1; // error 
  
  while( rootNode !=null){
     rootNode = rootNode.left;
  }
  return rootNode.data;
}
```
- To find Max value, same algorithm, traverse Right instead of Left
- code
```java
int findMin(Node rootNode){
  if (rootNode == null) return -1; // error 
  
  while( rootNode !=null){
     rootNode = rootNode.right; // right is the only change
  }
  return rootNode.data;
}
```
- Recursive approach for finding min in a subtree
```
int findMin(Node rootNode){
   if(rootNode == null ) {
      return -1; //return error or null
   }else if (rootNode.left == null){
      return rootNode.data;
   }
   return findMin(rootNode.left);//more like searching through subtree
}
```

#### Inserting data into BTS tree
- Algorithm
```
- if the rootNode is null, create a rootNode and add the data with left and right node with null
- if the rootNode is not null, then check if the data (to be inserted) is less than the rootNode.data
   - if it is less, then create the temp node and set it to LEFT of rootNode
   - if it is greater than rootNode, then create node and set to RIGHT of rootNode
```
- code recursive approach
```java 
Node insert(Node rootNode, int data){
        if(rootNode== null){
            Node temp = new Node();
            temp.setData(data);
            temp.setRightNode(null);
            temp.setLeftNode(null);
            rootNode = temp;
        } else if( data <= rootNode.getData()){
            rootNode.setLeftNode(insert(rootNode.getLeftNode(),data));
        }else {
            rootNode.setRightNode(insert(rootNode.getRightNode(),data));
        }
        return rootNode;
    }
```
- code non recursive approach
```java
//The Node node = null; is class level variable. 
void insert (int data){
        // Create the Temp node at the start
        Node temp = new Node();
        temp.setData(data);
        temp.setRightNode(null);
        temp.setLeftNode(null);
        Node current, parent ;
        // If the node is null then temp needs to be added (very first node to be added to tree) 
        if (this.node == null){
            this.node = temp;
        }else {
            current = this.node;  // this node variable for holding the current node
            parent = null;        // this node variable for holding the parent node of current node.
            while(true){
                parent = current;
                if (data < parent.getData()){ // 
                    current = current.getLeftNode(); // if the current node leftnode is null, then add the temp node and retrun
                    if(current == null) {
                        parent.setLeftNode(temp);
                        return;
                    }
                }else{
                    current = current.getRightNode();
                    if(current == null){
                        parent.setRightNode(temp);
                        return;
                    }
                }
            }
        }
     }

```

-Deleting:
   - Check the other [link](https://github.com/thirumurthis/Learnings/blob/master/algorithm/tree_notes.md) for the detail algorithm
```java
Node deleteNode(Node node,int data){
        if(node == null){
            return null;
        }else if(data < node.getData()){
            node.setLeftNode(deleteNode(node.getLeftNode(),data));
        }else if(data > node.getData()){
            node.setRightNode(deleteNode(node.getRightNode(),data));
        }else{
            //Scenario 1: no child
            if(node.getLeftNode()==null && node.getRightNode()==null){
                 return null;
            }
            //Scenario 2: only either Right or Left node of node to be removed
            if(node.getLeftNode() == null){
                return node.getRightNode();
            }
            else if(node.getRightNode() == null){
                return node.getLeftNode();
            }
            // scenario 3: if there are both Right and Left node of the node to be removed
            else{
                Node tempNode = findMin(node.getRightNode());
                node.setData(tempNode.getData());
                node.setRightNode(deleteNode(node.getRightNode(),tempNode.getData()));
            }
        }
       return tree;
    }
```
