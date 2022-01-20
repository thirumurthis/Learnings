package com.test.sample.algo;

/*
 * Problem: Draw a matrix with 1's so that it forms a symmetric diamond shape
 *  Input: 5
 *  Output: 
			            1              
			         1  1  1           
			      1  1  1  1  1        
			   1  1  1  1  1  1  1     
			1  1  1  1  1  1  1  1  1  
			   1  1  1  1  1  1  1     
			      1  1  1  1  1        
			         1  1  1           
			            1              
  Explanation:
     - for 5, the matrix would be 7 x 7 and filled with ones diagonally.
     - for 8, the matrix would be 15 x 15
     
     Logic,
       - Create the an array with  2n-1 x 2n-1 length. 
       - Iterate and include 1's in specific position based on the array length/2 value
       
       Space Complexity: O(n^2) 
       Time Complexity: O(n^2)
 */
public class DrawMatrix {
	
	public static void main(String[] args) {
		
		DrawMatrix mat = new DrawMatrix();
		mat.printMagicMatrix(5);
	}
	
	public void printMagicMatrix(int x) {
		int n = x;
		int input =2*n-1;
		char a[][] = new char[input][input];
		int temp =input/2;
		for(int i= 0; i<a.length ;i++) {
			for (int j=0;j<a[i].length;j++) {
				if(i==temp || j==temp ) {
					a[i][j] = '1';
				}else if( i > 0 && j > 0 && i < input -1 && j < input -1){
					a[i][j] = '1';
				}else {
					a[i][j] = ' ';
				}
				if(j<temp-i) { 
				    a[i][j]=' ';
				}else
				if(i > temp &&  j<=i-(temp+1)) { //
					a[i][j]=' ';
				}else
				if(j>temp+i && j<input-1){
					a[i][j]=' ';
				}else 
				if(i> temp && j < input && j > temp+input-1-i){
					a[i][j]=' ';
				}
			}
		}

   //Print the array of chars		
		for(int i=0;i< a.length;i++) {
			for(int j=0;j<a[i].length;j++) {
				System.out.print(a[i][j]+"  ");
			}
			System.out.println();
		}
	}
}
