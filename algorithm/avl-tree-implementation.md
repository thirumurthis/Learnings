```
     * Height of the tree :- is number of edges from  that node to the furthest leaf.
     * Or From the root node to the leaf node.
     *  HEIGHT - ROOT ----> LEAF NODE

     * Depth of the tree :- is number of edges from that  node to the root node.
     *  DEPTH - THAT NODE ----> ROOT

     * Double Rotation -
     * LEFT - RIGHT ROTATION
     * Note: adding a 2 to the below tree make unbalance
     * and since left heavy we do right rotation will lead to right heavy
     *  (2-0=2) 3                               1
     *        /                                  \
     *  (-1) 1        => Right rotation =>        3
     *       \                                  /
     *   (0)  2                                2
     * So we need to do left-right rotation
     * LR- left-rotation; RR - right rotation
     *  So if the left subtree balance is 1 then we do left
     *        3               3                2
     *       /              /                /   \
     * (-1) 1   -> LR ->  2     -> RR ->    1     3
     *       \           /
     *        2         1
     * RIGHT - LEFT ROTATION
     *       1 (0-2=-2)             1                  2
     *        \                      \               /  \
     *         3 (1-0=1)  -> RR ->    2  -> LR ->   1    3
     *        /                        \
     *       2 (0)                      3
 ```

```java
package algorithm.avl;

public interface Tree<T extends Comparable<T>> {

    Node<T> insert(T data, Node<T> node);
    Node<T> delete(T data, Node<T> node);
    //same as BST implementation
    Node<T> traverse(Node<T> node);
    //same as BST implementation
    T getMax(Node<T> node);
    //same as BST implementation
    T getMin(Node<T> node);
    //same as BST implementation
    boolean isEmpty();
}
```

```java
package algorithm.avl;

public class TreeOperation<T extends Comparable<T>> implements Tree<T>{


    @Override
    public Node<T> insert(T data, Node<T> node) {
        //System.out.println("data:- "+data+" - node:- "+((node!=null)?node.getData():"Null"));
        //System.out.println("start -- ");
        //traversePreOrder(node);
        //System.out.println("end  -- ");
        if( node == null) {
            System.out.println("node is null to set >> "+data);
            return new Node<>(data);
            //node = new Node<>(data);
        }
        if (data.compareTo(node.getData()) < 0){
            //System.out.println("LEFT - " );
            //traversePreOrder(node);
            node.setLeftChild(insert(data,node.getLeftChild()));
        }else if (data.compareTo(node.getData())>0){
            //System.out.println("RIGHT");
            //traversePreOrder(node);
            node.setRightChild(insert(data,node.getRightChild()));
        }else{
           // System.out.println("node "+node.getData());
            //System.out.println("ELSE");
            return node;
        }

        updateHeight(node);
        System.out.println("Before rotation -\n"+traversePreOrder(node));
        //return applyRotation(node);

        Node<T> rotated = applyRotation(node);
        System.out.println("After rotation -\n"+traversePreOrder(rotated));
        return rotated;
       // return node;
    }

    @Override
    public Node<T> delete(T data, Node<T> node) {

        if(node == null){
            return null;
        }

        if(data.compareTo(node.getData()) <0 ){
            node.setLeftChild(delete(data,node.getLeftChild()));
        }else if(data.compareTo(node.getData()) >0 ){
            node.setRightChild(delete(data,node.getRightChild()));
        }else{
            if(node.getLeftChild() == null){
                return  node.getRightChild();
            }else if(node.getRightChild()==null){
                return node.getLeftChild();
            }
            node.setData(getMax(node.getLeftChild()));
            node.setLeftChild(delete(node.getData(),node.getLeftChild()));
        }

        updateHeight(node);
        Node<T> rotated = applyRotation(node);
        traversePreOrder(rotated);
        return  rotated;
    }

    private Node<T> applyRotation(Node<T> node) {
        int balance = balance(node);
        System.out.println("balance :- "+balance);
        if(balance>1){ //left heavy
            // if the balance of the left node/subtree is negative
            // we perform left rotation and then right rotation
            if(balance(node.getLeftChild()) <0 ){
                node.setLeftChild(rotateLeft(node.getLeftChild()));
            }
           // System.out.println("apply rotation BFR :- \n"+traversePreOrder(node));
            //for left heavy perform right rotation
            Node<T> rtd = rotateRight(node);
           // System.out.println("apply rotation AFT :- \n"+traversePreOrder(rtd));
          //  return rotateRight(node);
            return rtd;
        }
        if(balance<-1){ //right heavy
            // if the balance of the RIGHT node/subtree i positive
            // we perform right rotation and then left rotation
            if(balance(node.getRightChild()) >0 ){
                node.setRightChild(rotateRight(node.getRightChild()));
            }
            //for right heavy perform left rotation
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Height of the tree :- is number of edges from
     * that node to the furthest leaf.
     * Or From the root node to the leaf node.
     *  HEIGHT - ROOT ----> LEAF NODE
     * Depth of the tree :- is number of edges from that
     * node to the root node.
     *  DEPTH - THAT NODE ----> ROOT
     * Double Rotation -
     * LEFT - RIGHT ROTATION
     * Note: adding a 2 to the below tree make unbalance
     * and since left heavy we do right rotation will
     * lead to right heavy
     *  (2-0=2) 3                               1
     *        /                                  \
     *  (-1) 1        => Right rotation =>        3
     *       \                                  /
     *   (0)  2                                2
     * So we need to do left-right rotation
     * LR- left-rotation; RR - right rotation
     *  So if the left subtree balance is 1 then we do left
     *        3               3                2
     *       /              /                /   \
     * (-1) 1   -> LR ->  2     -> RR ->    1     3
     *       \           /
     *        2         1
     * RIGHT - LEFT ROTATION
     *       1 (0-2=-2)             1                  2
     *        \                      \               /  \
     *         3 (1-0=1)  -> RR ->    2  -> LR ->   1    3
     *        /                        \
     *       2 (0)                      3
     */
    /**
     * rotate right  (LN - left node; N - node; CN - CenterNode)
     *                  after rotation
     *        N  10 h(2-0=2)                6  LN  h(1-2=-1 balance)
     *          /                         /  \
     *    LN   6 h(0)          =>        4    10    N
     *       /  \                            /
     * h(0) 4    8 h(0) CN                  8  CN
     */
    private Node<T> rotateRight(Node<T> node){
        //store the node in a tmp variable
        // node is the root
        Node<T> leftNode = node.getLeftChild();
        Node<T> centerNode= leftNode.getRightChild();
        // after the rotation, the left node's right node will be the node
        leftNode.setRightChild(node);
        node.setLeftChild(centerNode);
        updateHeight(node);
        updateHeight(leftNode);
        return leftNode;
    }

    private Node<T> rotateLeft(Node<T> node) {
        //store the node in a tmp variable
        // node is the root
        Node<T> rightNode = node.getRightChild();
        Node<T> centerNode= rightNode.getLeftChild();

        rightNode.setLeftChild(node);
        node.setRightChild(centerNode);
        updateHeight(node);
        updateHeight(rightNode);
        return rightNode;

    }

    private  int balance(Node<T> node){
        return node !=null? height(node.getLeftChild())-height(node.getRightChild()):0;
    }

    //Pre-order visit Root-Left-Right
    @Override
    public Node<T> traverse(Node<T> node) {
        if(node == null){
            return null;
        }
        System.out.print(node.getData()); 
        traverse(node.getLeftChild());
        traverse(node.getRightChild());
        return node;
    }


    @Override
    public T getMax(Node<T> node) {
        return null; //not implemented
    }

    @Override
    public T getMin(Node<T> node ) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private void updateHeight(Node<T> node){
        int maxHeight = Math.max(height(node.getLeftChild()),
                height(node.getRightChild()));
        node.setHeight(maxHeight+1);
    }

    private int height(Node<T> node){
        return node != null? node.getHeight():0;
    }

    public void traverseNodes(StringBuilder sb, String padding, String pointer, Node<T> node,
                              boolean hasRightSibling) {
        if (node != null) {
            sb.append("\n");
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getData());

            StringBuilder paddingBuilder = new StringBuilder(padding);
            if (hasRightSibling) {
                paddingBuilder.append("│ ");
            } else {
                paddingBuilder.append("  ");
            }

            String paddingForBoth = paddingBuilder.toString();
            String pointerRight = "└──";
            String pointerLeft = (node.getRightChild() != null) ? "├──" : "└──";

            traverseNodes(sb, paddingForBoth, pointerLeft+"L:- ", node.getLeftChild(), node.getRightChild() != null);
            traverseNodes(sb, paddingForBoth, pointerRight+"R:- ", node.getRightChild(), false);
        }
    }

    public String traversePreOrder(Node<T> root) {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(root.getData());

        String pointerRight = "└──";
        String pointerLeft = (root.getRightChild() != null) ? "├──" : "└──";

        traverseNodes(sb, "", pointerLeft+"L:- ", root.getLeftChild(), root.getRightChild() != null);
        traverseNodes(sb, "", pointerRight+"R:- ", root.getRightChild(), false);

        return sb.toString();
    }
}

```
