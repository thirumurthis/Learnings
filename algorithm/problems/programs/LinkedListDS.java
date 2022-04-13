package com.algo.sort.ll;

public class LinkedListDS {
	
	// Find the MIDDLE with 1 pass
	// Reverse the Linked List with 1 pass
	public static void main(String[] args) {
		
		int[] input = {10,11,9,1,4};
		Node root = null;
		LinkedListDS linkedList = new LinkedListDS();
    
		for(int i =0 ; i< input.length;i++) {
			root = linkedList.createLinkedList(root, input[i]);
		}
		linkedList.printLinkedList(root);
		
		System.out.println("Mid Value Odd :="+linkedList.findMid(root));
		root = linkedList.createLinkedList(root, 8);
		linkedList.printLinkedList(root);
		System.out.println("Mid Value Even :="+linkedList.findMid(root));
		
		//remove the middle value
		Node midNodeAddr = linkedList.findMidAddr(root);
		System.out.println("After deleting middle node with data value = "+midNodeAddr.getData());
		// pass the middle node address, to delete it
		linkedList.deleteNodeAddress(midNodeAddr);

		linkedList.printLinkedList(root);
		
	}
	
	/*
	 * 10|addr0 -> 11| addr1 -> 9 | addr2 -> 1 | addr3 -> 4| null
	 * 	  
	 * Keep two pointer,
	 *   - Slow   => hops one node at a time
	 *   - Fast   => hops two node at a time
	 *  
	 *  When the fast node reaches the end, return the slow pointer 
	 *  since it will be at the middle element
	 *  
	 *  Consider odd and even cases
	 */
	public int findMid(Node root) {
		
        // Let both the slow and fast pointer start from root that is head 
		Node slowPtr = root;
		Node fastPtr = root;
		// if fast ptr is null then we need to stip 
		while (fastPtr != null && fastPtr.getNext() != null) {
			slowPtr = slowPtr.getNext();
			fastPtr = fastPtr.getNext();
			if(fastPtr!= null) {
				fastPtr = fastPtr.getNext();
			}
		}
		//slow pointer will return the mid value
		return slowPtr.getData();
	}
	
	/*
	 * Below is the logic to delete the node in Linked list when address is provided.
	 * 
	 * The idea is to get the next node data and address and set it to the current node to be deleted
	 * 
	 * For example 
	 * 
	 *  10|adr1 -> 11|adr2 -> 12|adr3 -> 9|adr4 -> 7|null
	 *  
	 *  provided address of node 3 which is (adr2) ie. 12|addr3
	 *  
	 *  Logic:
	 *    use the address, adr3 to get the data =9 and address adr4
	 *    set that data to the adr2 itself.
	 *    then de-link the addr3
	 *   
	 *   10|adr1 -> 11|adr2 -> 9|adr4 -> 7|null
	 */
	public void deleteNodeAddress(Node deleteNode) {
		if (deleteNode != null && 
			deleteNode.getNext() != null) {
			//fetch the next node, since we are going to override the 
			// delete node
			Node nextNode = deleteNode.getNext();
			deleteNode.setData(nextNode.getData());
			deleteNode.setNext(nextNode.getNext());
			//de-link the node
			nextNode = null;
		}
	}
	
	/*
	 * Below function will identify the Address of the middle node
	 * in one pass, similar to the logic to find the middle data this 
	 * also uses the same logic
	 */
	
	public Node findMidAddr(Node root) {
        // Let both the slow and fast pointer start from root that is head 
		Node slowPtr = root;
		Node fastPtr = root;
		// if fast ptr is null then we need to stip 
		while (fastPtr != null && fastPtr.getNext() != null) {
			slowPtr = slowPtr.getNext();
			fastPtr = fastPtr.getNext();
			if(fastPtr!= null) {
				fastPtr = fastPtr.getNext();
			}
		}
		//slow pointer will return the mid value
        	return slowPtr;
	}
	/*
	  10 | null
	  10 | addr0 , 11 | null
	  10 | addr0 , 11 | addr1 , 9 | null;
	  
	  To create a linked list just use a temp node, and traverse to 
	  identify the last node, and create a new one associate the address to it
	 */
	public Node createLinkedList(Node root, int data) {
		// When the root node is null create a new one
        	if(root == null) {
		  return new Node(data,null);
		}else{
			// assing the root to temp,
			// this is used for traversing
			Node nextNode = root;
			// the while loop traverse to the last node,
			while(nextNode.getNext() != null) {
				nextNode = nextNode.getNext();
		        }
			// below will create an new node and associate it
			// to the root node reference
			if(nextNode.getNext() == null) {
				Node newNode = new Node(data,null);
				nextNode.setNext(newNode);
			}
		  return root;
		}
	}
	
	public void printLinkedList(Node root) {
		if(root == null) {
			System.out.println();
			return;
		}else {
			//System.out.print(root.getData()+ " | "+ root+ " ; ");
			System.out.print(root.getData()+ " -> ");
			printLinkedList(root.getNext());

		}
	}
	
		// 10,11,9,1,4
	// 1. c = 10, temp = 11,c.n = null,c =11 -> 11,
	public Node reverseLinkedList (Node root) {
		Node prev = null;
		Node current = root;
		Node temp4Next = null;
		
	                                        //10,11,9,1,4
		while(current!=null){               // Iteration1             // Iteration2       // Iteration3    //Iteration4
			temp4Next = current.getNext();  // 1. temp = 11|a(9)      temp= 9|a(1)        temp=1|a(4)      temp=4|NULL
			current.setNext(prev);          // 2. curr.next=NULL      curr.next=a(10)     curr.next=a(9)   curr.next=a(1)
			prev=current;                   // 3. prev = 10| NULL     prev = 11|a(9)      prev = 9|a(1)    prev=1|a(4)
			current=temp4Next;              // 4. curr = 11| a(9)     curr = 9|a(1)       curr = 1|a(4)    curr=4|NULL
		}
		root = prev;                 
		return root;
	}

}

/* Linked list data structure to store data and next node address
*/
class Node {
	
	private int data;
	private Node next;

	public Node (int data, Node next) {
		this.data= data;
		this.next = next;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}
	
	
}
