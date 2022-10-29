### Generics in java

### TOC
 - Generic class
 - Generic Interface
 - Generic methods
   - Within the generic class
   - Outside the generic class
 - using extends keyword for setting bound 
 - different example
    - type inference (type witness)
 - type erasure example
 - Restriction of Generics

Generics allows the same class to be used for multiple reference type.

- Generics are defined by using `parameterized types`, 
these types are replaced by the compiler with concrete types at runtime.
- The compiler checks this at the compile time.
- Classes, interfaces, methods and even variables can be declared as generic types
- Generic type must be refernce type, they cannot be primitive type (like int,char, etc).
- Generic type is a generic class or interface that is parameterized over types
- Generic can also be sub type, then it becomes a bounded type. (this can be done using extend keyword)

Advantage of Generics:
 - Strong type checks can be achived at compile time
 - Finding issues at compile time is much better than finding at runtime.
 - This eleminates unnecessary casts.
 
Example of Generic types in Java comparable interface

```
public interface Comparable<T>{
   public int compareTo(T 0);
}
```

Syntax:

- Syntax for `Generic class`

```
class name <T1,T2,... TN> {
  ...//Type parameters are delimited by angle bracktes
  ...//Class can have multiple parameters
}
```
 Common Type Parameter name conventions used
 
```
 E - Element (used mostly in Collection package)
 K - key
 V - Value
 N - Number
 T - type
 S,U - other conventions
```

- Syntax for `Generic method`

```
public static <E> void calculate(E[] list){
  //... Note the type paramter in angle bracket 
  // comes before the return type void.
}
```

- Syntax for `Generic interface`

```
public interface Person <E> {
  //... Similar to the generic class syntax but 
  // type parameter is specified in angle bracket
  // above contains one type parameter
}

public interface Tuple <K,V> {
  //...There can be more than one type parameter
}
```

### Type Erasure
 - Type Erasure is the process of removing explicit type information from generic code during compilation.
 - Example: List<String> is converted to nongeneric List. The non generic type is called raw type. This is done for backward compitablity.
 - List raw type is different from List<String>. 

### Terminology:
  - **Type Parameter** - class simple<T1,T2,...,TN> {} - T1,. is refered as type parameter
  - **Type Argument** - concrete type when creating an instance of the generic class is called type argument.

```
public class Pet<E> {} //E is type parameter

Pet<Dog> p1 = new Pet<>(); // Dog is type argument
// Note the above is not creating an Pet object, this is a place holder to hold the Dog object.
```

### Example of using Generics and method

- Note that the operation like >,< can't be applied in generics so we use comparable.
- also the type parameter is extends comparable where we doing this we only restrict to those type which are double, int, chars ,etc.

```java
package com.generics;

public class PrintListUsingGeneric {

    public static void main(String[] args) {

        Integer[] intItemList = {1,2,3,4,5,6,7,8};
        String[] strItemList = {"a","b","c","d","e","f","g"};

        printElement(intItemList);
        printElement(strItemList);
        System.out.println(countOccurrence(intItemList,5));
    }

    /**
     * method finds number of times the element occured
     */
    public static <T extends Comparable<T>> int countOccurrence(T[] list, T element){
        int count =0;
        for( T item : list){
            //if( item > element){ // if we use this compilation error since compiler doesn't know type
                                // To fix this we extend it using comparable like above

            if(item.compareTo(element) > 0){
                 // compareTo returns -ve  number if first number is less than second
                 // compareTo returns +ve number if first number is greater than second
                 // cmpareTO return 0 if first and second are equal

                count++;
            }
        }
        return count;
    }

    public static <E> void printElement(E[] itemList){
        for(E item : itemList){
            System.out.println("item :- " + item);
        }
        System.out.println("_____________");
    }
}
```

### Example of Using Generic interfaces
  - interface declared wtih type parameter are generic 
  - They expose memebers of a class to be used by othe classes
  - Force a class to implement a specific functionality

```java
package com.generics;

public interface SimpleGenericInterface <T>{

    public void add(T t);
}


package com.generics;

public class GenericList<T> implements SimpleGenericInterface<T> {

    public T customList;

    public void add(T t) {
        customList = t;
    }
}

package com.generics;

public class GenericInterfaceClient {

    public static void main(String[] args) {
        GenericList<String> customList = new GenericList<>();
        customList.add("one");
    }
}
```

### Bounded generic types example

- We can apply restriction on type parameters using bounded generic types
- Bounded types allow compile time restriction, this is called bound
- They are specified using `extend` keyword
- Type parameter can be bounded by a superclass or an interface

- Bounds increase the number of permitted method calls
- An unbounded type may only call the object methods
- Applying a superclass, the accessible memebers of that types are also available.

```
public <T extends Number> void display(T t){}

display(new Integer(100)); // acceptable OK
display("example"); // will throw error 
```
- multiple bound parameters can be provide by sperating using `&`

```
public <T extends Integer & Double> void display(T t){}
```
Note:
  - extends used to speciy the super class which is the upper bound
  - Pet< T extend superclass>, when the Pet is invoked the T can replaced by any thing that is a super class and its subclass. (super class is inclusive)
  - Pet <T extend interface1> when the Pet is invoked the T can be replaced by any class that implements the interface. 
  - Class and the interface uses the same `extend` keyword
  - Multiple bounds using `&`
    - Example: Pet<T extends interface1 & interface2>
               Pet<T extends superclass1 & interface1 & interface2>
    - When using mutiple bounds, the class needs to be specified first, else will display a compilation error.
    - We can only use only one class but we can use multiple interfaces. (since multiple inheritance is not available in java)

###  Examples with generic interface

```java
package com.generics;

public interface Pair <K,V>{
    public K getKey();
    public V getValue();
}

package com.generics;

public class Tuple<K,V> implements Pair<K,V>{

    private K key;
    private V value;

    public Tuple(K key,V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}

package com.generics;

public class DataClient {

    public static void main(String[] args) {
        Pair<Integer,String> tuple1 = new Tuple<>(1,"one");
        Pair<String,Integer> tuple2 = new Tuple<>("two",2);

        display(tuple1.getKey(),tuple1.getValue());
        display(tuple2.getKey(),tuple2.getValue());
    }

    public static <K,V> void display(K key, V value){
        System.out.println("key: "+key + " || value: "+value);
    }
}
/* Output:
key: 1 || value: one
key: two || value: 2
*/
```

### Generic Methods 

 - Generic Method outside Generic class. This is the one we saw above.
    - Example:
    ```
     public static <T> display(T t){...}
    ```
 - Generic Methods inside Generic Class.
    - A method inside generic class will always has the access to the type parameter that has defined on the class level. So this automatically becomes the generic method.
    - A method inside generic class can also use more generic one generic type that is defined in the generic class.
    - Example: 
    ```
     class Temp<T> {
         public <T,S,U,V> display (...){...} //can use more parameter along with T class level parameter.
     }
    ```

### Example using generic class and methods

```java

package com.generics.ex1;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Pet <T>{

    @Getter
    private List<T> petInfo;

    public Pet(){
        petInfo = new ArrayList<>();
    }

    public void addPet(T t ){
        petInfo.add(t);
    }

    public T getLastPetInfo(){
        if (petInfo.isEmpty()) return null;
        return petInfo.get(petInfo.size()-1);
    }
}


package com.generics.ex1;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Dog {

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String owner;
    @Getter
    @Setter
    private double weight;
}

package com.generics.ex1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Cat {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String color;

    @Getter
    @Setter
    private double weight;
}


package com.generics.ex1;

public class Vet {

    // this is a case where we are outside the generics class
    // we need to define the generic type parameter
    // only when we define we can use the generic parameter
    public static <T> int getPetsVisited(Pet<T> pet){
        // the T is not a return type here
        return pet.getPetInfo().size();
    }
}

package com.generics.ex1;

public class DomesticClient {

    public static void main(String[] args) {
        Pet<Dog> dogPet = new Pet<>();
        dogPet.addPet(new Dog("Tommy","Joe",40.0));
        dogPet.addPet(new Dog("Bagel","Jerry",28.0));
        System.out.println(dogPet.getLastPetInfo());
        System.out.println(dogPet);
        System.out.println(Vet.getPetsVisited(dogPet));

        Pet<Cat> catPet = new Pet<>();
        catPet.addPet(new Cat("Tom","white",20.0));
        catPet.addPet(new Cat("Ben","brown",22.4));
        System.out.println(catPet.getLastPetInfo());
        System.out.println(catPet);
        System.out.println(Vet.getPetsVisited(catPet));

    }
}

/* output
Dog(name=Bagel, owner=Jerry, weight=28.0)
Pet(petInfo=[Dog(name=Tommy, owner=Joe, weight=40.0), Dog(name=Bagel, owner=Jerry, weight=28.0)])
2
Cat(name=Ben, color=brown, weight=22.4)
Pet(petInfo=[Cat(name=Tom, color=white, weight=20.0), Cat(name=Ben, color=brown, weight=22.4)])
2
*/
```

- We add an interface bound to the above example.

```java
package com.generics.ex1;

public class Vet {

    // this is a case where we are outside the generics class
    // we need to define the generic type parameter
    // only when we define we can use the generic parameter
    public static <T extends Animal> int getPetsVisited(Pet<T> pet){
        // the T is not a return type here
        return pet.getPetInfo().size();
    }
}

package com.generics.ex1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Cat implements Animal{
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String color;

    @Setter
    private double weight;

    @Override
    public double getWieght() {
        return weight;
    }
}

package com.generics.ex1;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Dog implements Animal{

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String owner;
    @Setter
    private double weight;

    @Override
    public double getWieght() {
        return weight;
    }
}

package com.generics.ex1;

public class Vet {

    // this is a case where we are outside the generics class
    // we need to define the generic type parameter
    // only when we define we can use the generic parameter
    public static <T extends Animal> int getPetsVisited(Pet<T> pet){
        // the T is not a return type here
        return pet.getPetInfo().size();
    }
}

package com.generics.ex1;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Pet <T extends Animal>{

    @Getter
    private List<T> petInfo;

    public Pet(){
        petInfo = new ArrayList<>();
    }

    public void addPet(T t ){
        petInfo.add(t);
    }

    public T getLastPetInfo(){
        if (petInfo.isEmpty()) return null;
        return petInfo.get(petInfo.size()-1);
    }

    public double getTotalWeight(){
        return this.petInfo.stream().mapToDouble(Animal::getWieght).sum();
    }
}

package com.generics.ex1;

public class DomesticClient {

    public static void main(String[] args) {
        Pet<Dog> dogPet = new Pet<>();
        dogPet.addPet(new Dog("Tommy","Joe",40.0));
        dogPet.addPet(new Dog("Bagel","Jerry",28.0));
        System.out.println(dogPet.getLastPetInfo());
        System.out.println(dogPet);
        System.out.println(Vet.getPetsVisited(dogPet));
        System.out.println("weight:- "+dogPet.getTotalWeight());

        Pet<Cat> catPet = new Pet<>();
        catPet.addPet(new Cat("Tom","white",20.0));
        catPet.addPet(new Cat("Ben","brown",22.4));
        System.out.println(catPet.getLastPetInfo());
        System.out.println(catPet);
        System.out.println(Vet.getPetsVisited(catPet));
        System.out.println("weight:- "+catPet.getTotalWeight());
    }
}

/*
Dog(name=Bagel, owner=Jerry, weight=28.0)
Pet(petInfo=[Dog(name=Tommy, owner=Joe, weight=40.0), Dog(name=Bagel, owner=Jerry, weight=28.0)])
2
weight:- 68.0
Cat(name=Ben, color=brown, weight=22.4)
Pet(petInfo=[Cat(name=Tom, color=white, weight=20.0), Cat(name=Ben, color=brown, weight=22.4)])
2
weight:- 42.4
*/
```

 ### Generic Class hierarchies rule

 - Subclass must pass type parameter to superclass
```
# if we are extending the ArrayList in the case
# the <E> is not passed the this will fall under raw List
public class myList<E> extends ArrayList<E> {}
```

- Generic subclass can externd a non-generic (raw) superclass/parent class
  
```
# Animal is defined as Raw type since not type parameter included
public class Dog<T> exteands Animal { }
```
- Subclass can also be declared by passing type argument to superclass
  - Below is how it looks like, though the Comparator is generic already
```
public class IntComparator implements Comparator<Integer> {}
```

- subclass can declare more than one type parameter

```
## the type parameter <E> of the parent is added to the subclass
public class myList<E,S> extenads ArrayList<E>{}
```

- Casting generics is allowed

```
List<Integer> items;
ArrayList<Integer> numbers = new ArrayList<>();
items = numbers; // this is valid
```

With the example class above
  - Create a Mammal class extending the Pet class
```java

package com.generics.ex1;

public class Mammal<T extends Animal> extends Pet<T>{
// if we don't sepcify the Pet<T> type parameter compiler warns
// of using raw type.

    @Override
    public double getTotalWeight() {
        return super.getTotalWeight();
    }
}

package com.generics.ex1;

public class MammalClient {

    public static void main(String[] args) {
        Mammal<Dog> mammal = new Mammal<>();

        Pet<Dog> dogPet  = new Pet<>();

        dogPet = mammal; // casting is allowed

    }
}

```

### Type argument hierarchies rule
 - Type arguments require a strict match
 - We cannot pass ArrayList<Double> argument to a method with List<Number> parameter.
 ```
 public class MyList{
     public static calculateSum(List<Numbers> items){....}
 }
 MyList.calculateSum(new ArrayList<Double>()); // this is not allowed
 ```
 - Casting is not allowed
 ```
 List<Numbers> numbers;
 ArrayList<Double> doubles = new ArrayList<Double> ();
 numbers = doubles; // this is NOT allowed
 ```
 - The casting can be relaxed using wild cards

Example of valid type argument

List<Double> can be passed with ArrayList<Double>

```

package com.generics.ex1;

public class Doodle extends Dog{
    // since we are extending Dog class
    // we need to create the default constructor else throws exception
    public Doodle(String name, String owner, double weight) {
        super(name, owner, weight);
    }
}

package com.generics.ex1;

public class DoodleClient {

    public static void main(String[] args) {

        Pet<Dog> dogPet  = new Pet<>();
        
        Pet<Doodle> doodlePet = new Pet<>();
        // if we try to cast the doodlePet with dogPet object
        // throws compliation error 
        doodlePet=dogPet;

    }
}
```

## Type Erasure
- The generics is information is removed during compilation process.
- Generics provides type safety checks only at compile time.
- The compiled byte code doesn't have any genric type information

Type Erasuser internal working:
- The java compiler replaces the type parameter with object type for unbounded type parameter.
- The java compiler replaced type parameter with the corresponding bound type when the type parameter is bounded. 

- Unbound type
```
public class Pet<T>{
   private T name;
}

# after compiled looks like below 

public class Pet{
  private Object name;
}
```
- bounded type 
```

public class Pet< T extends Animal>{
 private List<T> petInfo;
}

# after compiled looks like below

public class Pet{
   private List<Animal> petInfo;
}
```

Note: 
 - The compiled code contains the Generics info as Metadata.
 - For example, using decompiler like IDE for ArrayList, we still be able to view the Generic defined. This information is stored part of the metadata of compiled class file. i.e, the .class file has the generic info stored as metadata
 - In case the if the class is being used as library, then user developing using this class the compiler will use this info to infer the generics are used.
 
### Bridge Methods:
  - Type erasure process generates Bridge methods.
  - This happens when some subclasses extends generic classes
  - This are needed since the type erasure process for overriding method is not the same as the type erasure for superclass method. In order to handle this situation java compiler creates bridge methods which is just an implementation detail used internally. (we will not invoke this methods or use it.)

```
public interface Comparator<T>{
   int compare(T n1, T n2);
}

public class IntComparator implements Comparator<Integer>{
   int compare(Integer n1, Integer n2){...}
}

# after compilation the type erasure looks like below

public interface Comparater{
   int compar(Object n1, Object n2); // super class type erasure is Object
}

public class IntComparator implements Comparator{
    int compare(Integer n1, Integer n2){  // this is still Integer type
    ...}
}

### An bridge methods will be created 

public class IntComparator implements Comparator{
  int compare(Object n1, Object n2){..} // this is bridge method created by compiler
                                        // using polymorphism
  int compare(Integer n1, Integer n2){..}  
}
```

### Type Inference
 - Type witness a way to force compiler to use a type argument

```java
package com.generics.ex1;

import java.util.Collections;

public class GenericClient {

    public static void main(String[] args) {

        // we are using var to store the emptyList
        // compiler will infer that the emptyList() returns List<Object>
        var list = Collections.emptyList();

        //Type witness - we specify a <> bracket with type
        // like below so compiler infers that it returns List<String>
        var items = Collections.<String>emptyList(); //Most case we don't do this.

    }
}
```
####  Type inference 
 - The compiler will infer the type arguments using the method definition and invocation.
 - Based on the above it picks most specific type that satisfies the argument and return type.
 
```
package com.generics.ex1;

import java.util.Collections;

public class GenericClient {

    public static void main(String[] args) {

        // if we change the return to an Integer then it will 
        // throw compilation error
        Number result = generate(12,14.5);
        
        // using the above example 
        Dog dog = new Dog("B","John",25.0);
        Doodle doodle = new Doodle("D","Tom",40.0);
        Dog getPet = generate(dog,doodle);
        
        // since we know dog is the parent class to the doodle
        //Doodle getDoodel = generate(dog,doodle); // compilation error
        
        //works 
        Animal pet1 = generate(dog,doodle);  //works
    }

    public static <T> T generate(T n1, T n2){
        if (Math.random() > 0.5){
            return n1;
        }
        return n2;
    }
}
```
Note: 
 - Within a Generic class, when using for static method we need to define the generic types
 
 ```java
 package com.generics.ex1;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Pet <T extends Animal>{

    @Getter
    private List<T> petInfo;

    public Pet(){
        petInfo = new ArrayList<>();
    }

    public void addPet(T t ){
        petInfo.add(t);
    }

    public T getLastPetInfo(){
        if (petInfo.isEmpty()) return null;
        return petInfo.get(petInfo.size()-1);
    }

    // this works fine, if we need to expose this method as static 
  //    public Pet<T> of(T t){
  
     // with the static we need to define the type paremeter in the method
     // though we are already within the generic class
      public static <T extends Animal> Pet<T> of(T t){
        Pet<T> pet = new Pet<>();
        pet.addPet(t);
        return pet;
    }
    public double getTotalWeight(){
        return this.petInfo.stream().mapToDouble(Animal::getWieght).sum();
    }
}

// with the client java code in main we can use
//   Dog dog = new Dog("B","John",25.0);
//   var box = Pet.of(dog);  // this infers as Pet<Dog>
//   Doodle doodle = new Doodle("D","Tom",40.0);
//   var pet2 = Pet.of(doodle); //this infers as Pet<Doodle>
```

- Type inference based on target type

```java 
// define below in the Pet class
    public static <T extends Animal> Pet<T> emptyPet(){
        return new Pet<>();
    }
    
// invoking from the client 
// var pet01 = Pet.emptyPet(); //infers as Pet<Animal> based on target type
```

- Wild cards

```java
public class Vet {

   // below is how the generics without wild card
   // public static <T extends Animal> int getPetsVisited(Pet<T> pet){
   
   // with wild cards, we don't need to define the type paramter before 
   // return type of the method
   public static int getPetsVisited(Pet<?> pet){
        // the T is not a return type here
        return pet.getPetInfo().size();
    }
}
```

``` 
         Pet
  ________|_______
 |                | 
 Dog             Bird
 | 
 Doodle
 
 - with an hierarchy like above
 
 Pet< ? extend Dog>  can only accept type argment as Pet<Dog>,Pet<Doodle>
```

List<? extends Number> can accept List<Integer>, List<Double>, etc. type the are subclass of Number.

- using `super` with the wild card. Lower Bound wildcards

```
         Pet
  ________|_______
 |                | 
 Dog             Bird
 | 
 Doodle
 
 below accepts any super class of Doodle
 Pet< ? super Doodle> only accept Pet<Dog>, Pet<Doodle>, Pet<Pet>.
```

### Restriction
 - we cannot instantiate type parameter ` T t = new T()` is not allowed.
 - we cannot have a static type parameter `private static T t;` Pet<Dog>; Pet<Doodle>; is not allowed.
 - instanceof operator (happens at runtime)
 ```
  Pet<Dog> dog = new Pet<>();
  if( dog instanceof Pet<Dog>).. // this won't work since type erasure removes the generics during compile time.
 ```
 - Special case, the instanceof works with unbounded wildcards
 ```
  ```
  Pet<Dog> dog = new Pet<>();
  if( dog instanceof Pet<?>).. 
 ```
 - Arrays of parameterized types are not allowed. ` Pet<Dog>[] dogArray = new Pet<>[2]`
 - We cannot do method overload with same type erasuer
 ```
 private static void add(Pet<Dog> dog)..
 private static void add(Pet<Cat> cat)...
 ```
 - we cannot create generics for any class extends Throwable or its subclass
 ```
 class PetException<T> extends Throwable {...}
 ```
 - we cannot use catch instance to use type parameter
 
 ```
 catch (T e){...}
 ```
 - we can use throws clause with the type parameter 
 
 ```
 public class GenericClass<T extends Throwable>{...}
 
 public void process() throws T{...}
 ```
