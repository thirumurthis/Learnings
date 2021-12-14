#### 1. What is the output of `"test".getClass().getSimpleName()` in java?
  - This prints `String`. (refer code snipper in next section)

-----------------------
#### 2. List maintains the insertion order and sort integer string first since ascii representation.
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
  - Output
```
Print the inputs - Insertion order is maintained: 100, abc, 0, 10, 12, 10

Sorting performed now: 0, 10, 10, 100, 12, abc

Sorting perfromed in Reverse order :abc, 12, 100, 10, 10, 0

To find the String class name for a string variable: String
```
-----------------------

#### 3. Method overloading in java, when overloaded method with `Object` and `String` method, java will try to assing to the least level object.
 - in this case the Object is the top level, String is at the least level.

NOTE: `Compilation error occurs, if we include another method with parameter type of StringBuffer. This because string and stringbuffer are at the same level.`

```java
public class OverloadingExampler {
	public static void main(String[] args) {	
		OverloadingExampler over = new OverloadingExampler();
		over.input(null);
	}
	public void input (Object str) {
		System.out.println("input of object: "+str);
	}
	public void input(String str) {
		System.out.println("Input of String: "+str);
	}
}
```
- Output:
```
Input of String: null
```
 - Compilation error
 - 
![image](https://user-images.githubusercontent.com/6425536/145922899-04e25d46-4f8e-4023-bac8-3a48788ace37.png)

-------------------
#### 4. Trick question with static, with counter not being incremented before main() call
```java
public class MainTest {
	 static int cnt = 0;
	public static void main(String[] args) {
		if(cnt < 3) {
			main(null);
		}else {
			cnt++;
		}
		System.out.println("Main Test program");
	}
}
```
- Output:
```
Exception in thread "main" java.lang.StackOverflowError
	at com.java.test.MainTest.main(MainTest.java:10)
	at com.java.test.MainTest.main(MainTest.java:10)
```
-----------------
#### 5. Trick question with static main call in recurion, with couter. How many times the string gets printed.
   - since the cnt variable declared with 0, there are 4 statement printed out. 
   - Since recursion of main method is used, the method is pushed to stack, and retrieved LIFO order
```java
public class MainTest {
	 static int cnt = 0;
	public static void main(String[] args) {
		if(cnt < 3) {
			cnt++; // Added to the flow.
			main(null);
		}else {
			cnt++;
		}
		System.out.println("MainTest outstatement: "+cnt);
	}
}
```
- Output
```
MainTest outstatement: 4
MainTest outstatement: 4
MainTest outstatement: 4
MainTest outstatement: 4
```

#### 6. Polymorphism (the capacity to take different forms).
 - In language like Java, it describes languate ability to process objects of various types and classes through singe, uniform interface.

 - Types of Ploymorphism
    - **Compile time ploymorphism** _(static binding)_ => `method overloading`
    - **Runtime polymorphism** _(Dynamic binding)_ => `method overriding`

Example:
   - ` Animal is a class, where Cat is a subclass`

Static ploymorphism:
  - Achived through `method overloading`, where several methods of a class having same name, but different types/order/number of parameters.
  - At compile time java knows, which method to invoke by checking the method signature.

Dynamic polymorphism:
  - A Sub class overrides a method from Super class
  - Note: As the method to call is determined during runtime it is called `dynamic binding` or `late binding`
  ```java
  class Animal{
    public void legs(){
    System.out.println(“has multiple legs”);
    }
}

class Cat extends Animal{
    public void legs(){
    System.out.println(“has 4 legs”);
    }
}

class Test{
    public static void main(String[] args){
    Animal animal=new Cat();
    animal.move();    // prints Cat object, has 4 legs
    animal=new Animal();
    animal.move();    // prints Animal object, has multple legs
    }
 }
  ```
