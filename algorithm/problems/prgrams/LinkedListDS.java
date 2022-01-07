package com.algo.sort.ll;

public class LinkedListDS {
	
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
		
		Node slowPtr = root.getNext();
		Node fastPtr = root.getNext();
		
		while (fastPtr.getNext() != null && fastPtr.getNext().getNext() != null) {
			if(fastPtr.getNext().getNext() == null) {
				return slowPtr.getData();
			}
			slowPtr = slowPtr.getNext();
			fastPtr = fastPtr.getNext().getNext();
		}
		
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
		
		Node slowPtr = root.getNext();
		Node fastPtr = root.getNext();
		
		while (fastPtr.getNext() != null && fastPtr.getNext().getNext() != null) {
			if(fastPtr.getNext().getNext() == null) {
				return slowPtr;
			}
			if(fastPtr.getNext()==null) {
				return slowPtr;
			}
			slowPtr = slowPtr.getNext();
			fastPtr = fastPtr.getNext().getNext();
		}
		
		return slowPtr;
	}
	/*
	  10 | null
	  10 | addr0 , 11 | null
	  10 | addr0 , 11 | addr1 , 9 | null;
	  
	  To create a linked list we need to keep track of the previous node,
	  the newly created nodes address needs to be set to previous node
	 */
	public Node createLinkedList(Node root, int data) {
		
		if(root == null) {
		  return new Node(data,null);
		}else{
			Node nextNode = root;
			Node previousNode = null;
			while(nextNode != null) {
				// holds the previous node
				 previousNode = nextNode;
				// fetch the next node
				nextNode = nextNode.getNext();
				// if the next node is null, that is where we 
				// need to update the last data
				if(nextNode == null) {
					Node newNode = new Node(data,null);
					previousNode.setNext(newNode);
				}
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

}


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
