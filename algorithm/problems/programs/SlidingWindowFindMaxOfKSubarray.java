package com.algo.sort.question;

import java.util.Arrays;
/*
Problem: given an input array, find the max value of K-th subarray
Explanation, 
   given {1,3,4,5,7,2,8} and K=3
   1,3,4 = 8
   3,4,5 = 12
   4,5,7 = 16
   5,7,2 = 11
   7,2,8 = 17
   return 17
   
   Algorithm:
     previousMaxSum  <- -INFINITY
     currentRunningSum <- 0
     
     i -> 0 to N   (N - length of array)
       - currentRunningSum <- input[i] 
       - if i < K-1
           previousMaxSum <- max ( previousMaxSum,CurrentRunningSum)
           currentRunningSum <- currentRunningSum - input[i - (K-1)]
     end iteration;      
     return previousMaxSum;
     
     Time complexity O(n)
     Space Complexity O(1)
*/
public class SlidingWindowSumOfArray {
	
	public static void main(String[] args) {
		
		int input[]= {1,3,4,5,7,2,8};
		//int input[]= {1,3,4,5,6,2,7,9};
		//int input[] = {100,200,300,400};
		int window = 3;
		SlidingWindowSumOfArray a = new SlidingWindowSumOfArray();
		
		result = a.maxWithSlideWindowWithSummingItself(input, window);
		System.out.println(Arrays.toString(input)+" result with n :- "+result);
			
	}
	
	public int maxWithSlideWindowWithSummingItself(int[] input, int pivot) {
		
    //declare a variable for keeping the current running sum
		int currentRunningSum =0;
    //declare a variable to track the currentMax value
		int previousMax =Integer.MIN_VALUE;
		for(int i=0;i<input.length;i++) {
      // till K sub array we sum the value in array
			currentRunningSum += input[i];
      // if the K-sub array is reached, we need to check if the current sum is maximum of previousMax
			if(i>=pivot-1) { // 0 >= 3-1, 1 >= 3-1, 2 >= 3-1 (performing K-1 since array start with 0th index
				previousMax = Math.max(previousMax,currentRunningSum);
				currentRunningSum -= input[i-(pivot-1)]; // 2-(3-1)=0; 3 -(3-1)=1; 4 -(3-1)=2...
        // For every index, we need to subract the 0th, 1st, 2nd index of the current running sum.
			}
		}
		return previousMax;
	}
}
