### Generics in java

### TOC
 - Generic class
 - Generic Interface
 - Generic methods
   - Within the generic class
   - Outside the generic class
 - using extends keyword for setting bound 

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
