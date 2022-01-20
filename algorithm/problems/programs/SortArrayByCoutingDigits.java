package com.test.sample.itrv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/*
 * Problem: Given array of numbers, sort it based on the sum of digits
 *          Input: { 10,33,20,5,15,100}
 *          Output: {10, 100, 20, 5, 33, 15}
 *          Note: The second degree sort is not applied, last two are not value sorted
 */
public class SortArrayBasedOnDigitSum {

	
	public static void main(String[] args) {
		
		SortArrayBasedOnDigitSum sortSum = new SortArrayBasedOnDigitSum();
		int input[] = { 10,33,20,5,15,100}; // {12,10,11,102,8,46};
		System.out.println("Input- "+Arrays.toString(input));
		System.out.println(Arrays.toString(sortSum.sortIt(input)));
		System.out.println(Arrays.toString(sortSum.sortItWithSortedMap(input)));
	}
	
	public int[] sortIt(int[] input) {
		
		int [] result = new int[input.length];
		Map<Integer,List<Integer>> idxStore = new HashMap<>();
		for(int i=0;i< input.length;i++) {
			int output=sumdigit(input[i]);

			if(!idxStore.containsKey(output)) {
				//System.out.println(output+" - "+ input[i]);
				 List<Integer> value = new ArrayList<>();
				 value.add(input[i]);
				idxStore.put(output,value);
			}else {
				idxStore.get(output).add(input[i]);
			}
			result[i]=output;
		}
		//idxStore.keySet().stream().forEach(itm -> System.out.println("key: "+itm+" v = "+idxStore.get(itm).toString()));
		//idxStore.keySet().stream().forEach(System.out::println);
		Arrays.sort(result);
		//System.out.println(Arrays.toString(result));
		//if(false)
		for(int i = 0; i < result.length; ) {
			//System.out.println("key: " + result[i]+" - " +idxStore.get(result[i]).toString());
			if(idxStore.get(result[i]).size() > 1) {
				
				int len = idxStore.get(result[i]).size();
				for(int k=0 ; k <len ; k++) {
				   result[i]= idxStore.get(result[i]).get(k);
				   i=i+1;
				}
			}else {
				//System.out.println("key: " + result[i]+" - " +idxStore.get(result[i]).toString());
			   result[i]=idxStore.get(result[i]).get(0);
			   i=i+1;
			}
		}
		return result;
	}
	
	/*
	 Using a sortedMap, to avoid inserting to the resutling array and perform a sorting.
	*/
	public int[] sortItWithSortedMap(int[] input) {
		
		int [] resultWithMap = new int [input.length];
		SortedMap<Integer,List<Integer>> idxStore = new TreeMap<>();
		for(int i=0;i< input.length;i++) {
			int output=sumdigit(input[i]);

			if(!idxStore.containsKey(output)) {
				 List<Integer> value = new ArrayList<>();
				 value.add(input[i]);
				idxStore.put(output,value);
			}else {
				idxStore.get(output).add(input[i]);
			}
		}
		//idxStore.keySet().stream().forEach(itm -> System.out.println("key: "+itm+" v = "+idxStore.get(itm).toString()));
		int idx = 0;
		for(Integer key : idxStore.keySet()) {
			if(idxStore.get(key).size() > 1) {
				for(int k=0 ; k <idxStore.get(key).size() ; k++) {
				   resultWithMap[idx]= idxStore.get(key).get(k);
				   idx++;
				}
			}else {
				resultWithMap[idx++]=idxStore.get(key).get(0);
			}
		}
		return resultWithMap;
	}


	private int sumdigit(int i) {
		int sum = 0;
		while(i>0) {
			sum = sum+i%10;
			i = i/10;
		}
		return sum;
	}
}
