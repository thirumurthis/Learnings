package com.test.sample.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Problem: Given array of numbers, sort it based on the sum of digits
 *          Input: { 10,33,20,5,15,100}
 *          Output: {10, 100, 2, 5, 33, 15}
 *          Note: The second degree sort is not applied, last two are not value sorted
 * 
 *   LOGIC:
 *       - Iterate thorugh array, count the sum of each digit, push it to a Map with sum of digit as key. 
         - If duplicate digits count, add to the map, since the value is List
         - also add the summed digit to an array so we can use Java in built Sort, 
         - Iterate the array of sum of digit again and fetche values from the map,
            - If there are more number of items in the List, add it to consecutive array index
            
        Space Complexity: ~O(2n)
        Time Complexity : O(n)
 */

public class SortArrayBasedOnDigitSum {

	
	public static void main(String[] args) {
		
		SortArrayBasedOnDigitSum sortSum = new SortArrayBasedOnDigitSum();
		int input[] = { 10,33,20,5,15,100}; // {12,10,11,102,8,46};
		System.out.println(Arrays.toString(sortSum.sortIt(input)));
	}
	
  /*
  Method to caculate the sum of digit and sort it
  */
	public int[] sortIt(int[] input) {
		
		int [] result = new int[input.length];
		Map<Integer,List<Integer>> idxStore = new HashMap<>();
		for(int i=0;i< input.length;i++) {
			int output=sumdigit(input[i]);
			 List<Integer> value = new ArrayList<>();
			 value.add(input[i]);
			if(!idxStore.containsKey(output)) {
				idxStore.put(output,value);
			}else {
				idxStore.get(output).add(input[i]);
			}
			result[i]=output;
		}
		Arrays.sort(result);
		System.out.println(Arrays.toString(result));
		for(int i = 0; i < result.length; i++) {
			if(idxStore.get(result[i]).size() > 1) {
				int len = idxStore.get(result[i]).size();
				for(int k=0 ; k <len ; k++) {
				   result[i]= idxStore.get(result[i]).get(k);
				   i=i+1;
				}
			}else {
			   result[i]=idxStore.get(result[i]).get(0);
			}
		}
		return result;
	}

  /*
  * function to sum the digits
  */
	private int sumdigit(int i) {
		int sum = 0;
		while(i>0) {
			sum = sum+i%10;
			i = i/10;
		}
		return sum;
	}
}
