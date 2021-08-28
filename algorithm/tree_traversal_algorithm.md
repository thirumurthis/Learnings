
#### Depth first search  (uses stack datastructure)
   - In-Order without recursion

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
class Tree{
    Tree leftNode=null;
    int data;
    Tree rightNode = null;
    boolean isLeafNode (){
        return this.leftNode == null && this.rightNode == null;
    }
  //getter setter methods
}
```
