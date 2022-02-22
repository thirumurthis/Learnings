package com.java.test.graph;

import java.util.Arrays;
/*
* Min heap flow
*/
public class MinHeap {

	public static void main(String[] args) {
		
        MinHeapHR minHeap = new MinHeap();
		minHeap.insert(13);
			minHeap.printArray();
		minHeap.insert(14);
			minHeap.printArray();
		minHeap.insert(15);
			minHeap.printArray();
		minHeap.insert(12);
			minHeap.printArray();
		minHeap.insert(10);
			minHeap.printArray();
		minHeap.insert(4);
			minHeap.printArray();
		System.out.println("---------------------");
		minHeap.poll();
			minHeap.printArray();
		System.out.println("---------------------");
		minHeap.poll();
			minHeap.printArray();
		minHeap.insert(20);
			minHeap.printArray();
	}
	private int CAPACITY = 10;
	private int size=0;
	int  items[] = new int[CAPACITY];
	
	public void printArray() {
		for(int i=1; i < size; i++) {
			//if(Heap[i] ==Integer.MIN_VALUE ) break;
			if(items[i] == 0 ) break;
			System.out.print(items[i]+(i==size-1?"":" , "));
		}
		System.out.println();
	}
	
	// left child = 2*N+1
	// rightchild = 2*N+2
	// parent = (childIndex -1)/2
	//  value: 12,10,14,13,15 
	//  index:  0, 1, 2, 3, 4
	// parent index >=0 
	//               12
	//              /  \
	//            10    14
	//           /  \ 
	//         13    15
	// Note: for heap, the leafs should start from left right
	int getParentIndex(int childIndex) {return  (childIndex -1)/2;}
	int getLeftChildIndex (int parentIndex) {return 2*parentIndex +1;}
	int getRightChildIndex (int parentIndex) {return 2*parentIndex +2;}
	
	boolean hasLeftChild(int index) {return getLeftChildIndex(index) < size;}
	boolean hasRightChild(int index) {return getRightChildIndex(index) <size;}
	boolean hasParent(int index) {return getParentIndex(index) >=0;}
	
	int getLeftChild(int index) {return items[getLeftChildIndex(index)];}
	int getRightChild(int index) {return items[getRightChildIndex(index)];}
	int getParent(int index) { return items[getParentIndex(index)];}
	
	/*
	 * During insertion or deletion we need this function
	 */
	void swap(int fromIndex, int toIndex) {
		int temp = items[fromIndex];
		items[fromIndex] = items[toIndex];
		items[toIndex] = temp;
	}
	
	/*
	 * To perform capacity check
	 */
	void capacityCheck() {
		if(size == CAPACITY) {
			items = Arrays.copyOf(items, CAPACITY*2);
			CAPACITY *=2;
		}
	}
	
	/*
	 * To fetch the parent or root node in minheap that will be the min value
	 */
    int peek() {
    	if(size ==0 ) throw new IllegalStateException();
    	return items[0];
    }
    
    /*
     * This will fetch and remove the parent node value and returns that element
     * while removed, we need to heapify the rest of the element from Root to bottom 
     */
    int poll() {
    	if(size ==0 ) throw new IllegalStateException();
    	int element = items[0];
    	items[0] = items[size-1];
    	size--;
    	heapifyElementsFromRoot();
    	return element;
    	
    }
    
    /*
     * When we insert the record, we will insert to the last elemet in the array
     * Then we will heapify up from the leaf
     */
    void insert(int element) {
    	capacityCheck();
    	items[size] = element;
    	size++;
    	heapifyUp();
    }

    /*
     * To heapfy or align the elements in the heap from leaf node
     */
	private void heapifyUp() {
		int index = size -1;
		while(hasParent(index) && getParent(index)>items[index]) {
			swap(getParentIndex(index),index);
			index = getParentIndex(index);
		}
	}

	/*
	 * Logic to heapify the elements starting from root node.
	 */
	private void heapifyElementsFromRoot() {
		int index =0;
		   //only check left side, if not left there is no right child
			while(hasLeftChild(index)) {
				int smallChildIndex = getLeftChildIndex(index);
				if(hasRightChild(index) && getRightChild(index) < getLeftChild(index)) {
					smallChildIndex = getRightChildIndex(index);
				}
				if(items[index] < items[smallChildIndex]) {
					break;
				}else {
					swap(index,smallChildIndex);
				}
				index = smallChildIndex;
			}
	}
} 
