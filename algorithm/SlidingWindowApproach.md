### Sliding window:

Given an array, find the max sum of K-th subarray

```
 [8, 9, 7, 1, 3, 4]  K=3
 
 output: 24 
 8,9,7 - first 3 element sum is the max
```

- Code logic:
  - Declare a maxSum -> -INFINITY
  - Declare a currentRunningSum -> 0
  - For i -> 0 to K,
     - currentRunningSum = Array[i]
  - For i -> 0 to Array.length - K 
     - maxSum = Math.max(maxSum, currentRunningMax)
     - currentRunningMax -= Array[i]
     - currentRunningMax += Array[i+K]
  - return maxSum 

```java
public int sumBySlidingWindow(int input[], int k) {
		int maxSum = Integer.MIN_VALUE;
		int currentRunningSum = 0;
    for(int i= 0;i<k;i++) {
			currentRunningSum +=input[i];
		}
		
    for(int i=0;i <input.length-k;i++) {
			maxSum = Math.max(maxSum, currentRunningSum);
			currentRunningSum -=input[i];
			currentRunningSum +=input[i+k];
		}
		return maxSum;
	}

// Another variation of the same:

public int sumBySlidingWindow(int input[], int k) {
		int maxSum = Integer.MIN_VALUE;
		int currentRunningSum = 0;
    for(int i= 0;i<k;i++) {
			currentRunningSum +=input[i];
		}
		
    for(int i=k;i <input.length;i++) {
			maxSum = Math.max(maxSum, sum);
      //add the last element and substract the first element when traversing
			sum +=input[i];  // i in this case starts from k,k+1,....Array.length
			sum -=input[i-k];  // i-k in this case would start with (k)-k =0; (k+1)-k= 1 ; (k+2)-k=2... till i = Array.length-1, in that case (Array.length-1)-K
		}
		return maxSum;
	}
```

- Another approach with the single iteration approach

```java
public int maxWithSlideWindowWithSummingItself(int[] input, int k) {
		
	int currentRunningSum =0;
	int max =Integer.MIN_VALUE;
   
	for(int i=0;i<input.length;i++) {
	    // Sum the last element 
	    currentRunningSum += input[i];
      
      // 1st iteration 0>= (3-1); currentRunningSum = A[0]
      // 2nd iteration 1>= (3-1); currentRunningSum = A[0] + A[1]
      // 3rd iteration 2>= (3-1); currentRunningSum = A[0] + A[1] + A[2]; 
      //                          loop will be true; check max;  max = currentRunningSum
      //                          currentRunningSum = A[0] + A[1] + A[2] - A[0] ; 
      // 4th iteration 3>= (3-1); currentRunningSum = A[1] + A[2] + A[3]; // 3 element gets added
      //                          loop will be true; check max;  max = currentRunningSum
      //                          currentRunningSum = A[1] + A[2] + A[3] - A[1] ; // subract first element
      // goes on
     
	if(i>=k-1) { // since array starts from 0th index, we perform k-1; 

             //determine the max first
	     max = Math.max(max,currentRunningSum);
	 
             // then decretment the very first element
	      currentRunningSum -= input[i-(k-1)]; // when i =2
	   }
	}
	return max;
}
```


### Find the minumum subrray with sum `greater than equal to` K.
- For example, for intput = {4,2,1,8,5,7}, find the max subarray length to get the sum K >= 9
   - in this case, 1,8 gives sum 9, so the min size is 2.
- For example, for same input {4,2,1,8,5,7}, find the max subarray length to get the sum k>= 7 will be 1
   - in this case, element 8 is greater than the 7 and its one subarray so should return 1

Logic:
  - When the sum K is reached we will shrink the window by using another pointer.

```java
	/*
	 * In this case 
	 * first when we reach the targetsum, 
	 * we need to dynamically shrink the windows size from the start 
	 * to check if we can do get some smallest sum
	 */
	public static int findSmallestSubarraySum(int[] input, int targetSum) {
		
		int windowLazyStart = 0;
		int currentWindowSum = 0;
		int minWindowSize = Integer.MAX_VALUE;
		for(int windowEnd =0; windowEnd < input.length; windowEnd++) {
			
			currentWindowSum +=input[windowEnd]; //add the element to get the sum
			
			while(currentWindowSum >= targetSum) {
				minWindowSize = Math.min(minWindowSize, windowEnd - windowLazyStart +1);
				currentWindowSum -= input[windowLazyStart];  //substract the value
				windowLazyStart++;
			}
		}
		return minWindowSize;
	}
```
