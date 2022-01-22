package com.algo.sort.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/*
Given and matrix 2X2
  1,2,1
  1,2,1
  1,2,2
  
  - Calculate the item with max frequecy and sum those.
  - In above case the 1 occurs 5 times, 2 occurs 4 times
  - since 1 has max occurence, we sum all 1's and display the result as 5
  
  1,1,1
  2,3,3
  4,5,3
    - 1 and 3 - occurs 3 times.
    - 1+1+1 + 3+3+3 = 12.
*/
public class MatrixSumOfMaxFrequencyItem {
	
	public static void main(String[] args) {
		
		int[][] input = {{1,1,1},
				             {2,3,3},
				             {4,5,3}};
		System.out.println(maxSum(input));
	}
	
	public static int maxSum (int[][]input) {
		Map<Integer,Integer> freq = new HashMap<>();
		for(int i=0;i<input.length;i++) {
			for(int j=0;j<input[i].length;j++) {
			
        // using compulteIfAbsent - will create the key value pair. in this case very first entry would be <k,v> <1,0>
        // if the key already exists returns null
				freq.computeIfAbsent(input[i][j], k -> 0);
        // Since already the values is inserted to the map, if the key present 
        // (computeIfPresent) increment the value by 1. Now, <k,v> = <1,1>; 
        // if the same key comes again the value will incremented be <1,2> since map already has the key
				freq.computeIfPresent(input[i][j], (k,v) -> v+1);
			}
		}
    // From the Map, we are fetching the max using lambda.
    // since frequence is stored as values of the key, we are using values()
    // even if there are duplicate values max will be returned
		int max = freq.values().stream().mapToInt(item->item).max().orElse(-1);
		System.out.println(freq+ " :: "+max);
		int sum = freq.entrySet().stream()                               //entryset is <k,v>
                             .filter(e -> e.getValue() == max)       // filter only the data which is max
		                         .mapToInt(e -> e.getKey()*e.getValue()) // from entry set we compute the key times value for result and add it.
                             .sum();
		return sum;
	}
}
