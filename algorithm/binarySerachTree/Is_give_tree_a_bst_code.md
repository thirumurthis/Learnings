## Check if Given Tree is a binary Search Tree or not?
  - Logic, 
    - Value of Left node should be less than the Root node
    - Value of Right node should be greater than the Root node.

We need to check the above conditions is satisfied. 
  - If we write a sperate function to check for the first item in recursion it will be performing the comparision on the same node repeatedly

Efficient way is to assume Min = -INFINITY and Max = +INFINITY,
  - When checking the left node we set the max value to be Root node value, so the left node value should be within -INFINITY and ROOT node value.
  - When checking the right node we set the min value to be Root node value, so the right node value should be within ROOT node value and +INFINITY

- Below is recurssive approach of the implementation
```java
    public boolean isValidBST(Node node, int min, int max,String str ) {
    	
    	if(node == null) return true;
      //printing the node with Left or Right indicator stored in the string
    	System.out.println(str+" "+node.data +" min:  "+min+" max: "+max);
    	if( (node.data > min && node.data < max) && isValidBST(node.left, min, node.data, "L")
    			&& isValidBST(node.right, node.data, max,"R")) {
    		return true;
    	}
    	return false;
    }
```
- Complete driver code, for two secnarios.

```java
package com.test1.algos;

import java.util.LinkedList;
import java.util.Queue;

public class TestTreeCode {

    public static void main(String ... args){

        //TestTreeCode treeCode = new TestTreeCode();


        Node node = new Node(10);
        Node node1 = new Node(11);
        Node node2 = new Node(9);
        Node node3 = new Node(7);
        Node node4 = new Node (15);
        Node node5 = new Node(8);
    	/*
    	      10
    	    /    \
    	   11     9 
    	 /
    	7
       / \
      15  8  
    	 */
      node.left = node1; //11
      node.right = node2; //9
      node1.left = node3; //7
      node3.left = node4; //15
      node3.right = node5;//8
 
 	   Node node00 = new Node(10);
 	   Node node01 = new Node(8);
 	   Node node02 = new Node(12);
 	   Node node03 = new Node(6);
 	   Node node04 = new Node(9);
 	   Node node05 = new Node(14);
    	   /*
    	         10
    	        /  \
    	       8    12
    	      / \     \
    	     6   9     14
    	    */
 	   node00.left=node01;
     node00.right=node02;
     node01.left = node03;
     node01.right=node04;
     node02.right=node05;
     	   
     TestTreeCode testTree = new TestTreeCode();
     testTree.printTree(node, "D ");
     // invalid tree 
     System.out.println(testTree.isValidBST(node, Integer.MIN_VALUE, Integer.MAX_VALUE,"D"));
     // valid tree
     System.out.println(testTree.isValidBST(node00, Integer.MIN_VALUE, Integer.MAX_VALUE,"D"));
     }

   // THE LOGIC FOR VALIDATING IF IT IS A BINARY TREE
    public boolean isValidBST(Node node, int min, int max,String str ) {
    	
    	if(node == null) return true;
    	System.out.println(str+" "+node.data +" min:  "+min+" max: "+max);
       /*
    	         10
    	        /  \
    	       8    12
    	      / \     \
    	     6   9     14
    	*/
      // 1st iteration - ROOT NODE : 10 > -INF  && 10 < +INF
      // once the min max is validated, the below will called, since && condition usage
      // 2nd iteration - node.left= 8; isValidBST( 8, -INF, 10) called which will check- 8 > -INF && 8 < 10 - pushed to stack
      // 3rd iteration - node.left= 6; isValidBST( 6, -INF, 8 ) called which will check- 6 > -INF && 6 < 8 - pushed to stack
      // since no further left node,base condition node==null will be checked and returns true
      // now the 3rd iteration will be validated, which will return true
      // now the 2nd iteration will be retrived and validated, since its true flow goes to right side (node at this point is 8)
      // 4th iteration - node.right= 9 isValidBST( 9, 8, +INF) 
      // now there is no right or left so returns true,
      // the 4th iteration is fetched from stack and evaluated to return true
      // the flow then checks root node 10, and its right.. and goes on..
      // DATA in the node should be GREATER than the min for LEFT NODE
      // DATA in the node should be LESS than the max for RIGHT NODE
    	if( (node.data > min && node.data < max) && isValidBST(node.left, min, node.data, "L")
    			&& isValidBST(node.right, node.data, max,"R")) {
    		return true;
    	}
    	return false;
    }
  
  // IN ORDER PRINTING of DATA 
	public void printTree(Node root,String str) {
		
		if(root == null) {
			return;
		}

		printTree(root.left,"L ");
		System.out.println(str+" - "+root.data);
		printTree(root.right,"R ");
	}
}

class Node{
    int data;
    Node right;
    Node left;
    Node(int data){
        this.data = data;
        this.right = null;
        this.left = null;
    }
}
```
