#### What is the output of `"test".getClass().getSimpleName()` in java?
  - This prints `String`. (refer code snipper in next section)
  
#### List maintains the insertion order.
  - Sorting the list containing with String and String integers. (the integer string will be printed first"
   
Refer below code:
 ```java
 public static void main(String[] args) {
		List<String> strList = new ArrayList<>();
		strList.add("100");
		strList.add("abc");
		strList.add("0");
		strList.add("10");
		strList.add("12");
		strList.add("10");
		System.out.println("Print the inputs - Insertion order is maintained: " +strList.stream().collect(Collectors.joining(", ")));
		
		Collections.sort(strList);
		System.out.println("Sorting performed now: "+strList.stream().collect(Collectors.joining(", ")));
		
		Collections.reverse(strList);
		System.out.println("Sorting perfromed in Reverse order :"+strList.stream().collect(Collectors.joining(", ")));
		
		// print the class name of the variable
		System.out.println("To find the String class name for a string variable: "+"test".getClass().getSimpleName());
	}
 ```
 
 - output
```
Print the inputs - Insertion order is maintained: 100, abc, 0, 10, 12, 10

Sorting performed now: 0, 10, 10, 100, 12, abc

Sorting perfromed in Reverse order :abc, 12, 100, 10, 10, 0

To find the String class name for a string variable: String
```
