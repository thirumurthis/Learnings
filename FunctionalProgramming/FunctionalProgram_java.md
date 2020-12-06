### Functional programming 
  - functional programming is a way of organizing code that makes more scalable and maintainable.
  - Note functional programming and object oriented programming are NOT opposites.
  - In some areas, the OOPs programming has some drawback where the Functional programming helps.
     - In OOPs programming, in large programs it is difficult to recreate, in functional programming helps write bug free code.
  - Functional programming brings the mathematical approach. like in maths having a f(x) = x+1, where there is no possible bugs in this function.
  
  
##### Declaritive and imperative:
     `Functional programming` is Declaritive style of programming.
     
     Declaritive programming - focus on What things are? (more like using the existing function)
     ```
       x = averate(inputArray); // the sum() is already defined part of the program
     ```
     Imperitive programming - focus How to do things. (mostly like steps reqiured.)
     ```java
      x = 0;
      for (int i =0; i < inputarray.length(); i++)
         x = x+inputarray[i];
      x = x/inputarray.length(); // get averate of an serias of number
     ```
     
 #### Concepts of Functional programming
    - Immutability
    - Functional purity 
    - first-class functions

##### Immutability
  - in functional programming when the variable x is set to 5, like final int x=5; it is not changed further.
  - mostly the variable should be not change further, like PI in a programing language, where this value is not changed.
  - In functional programming, if we declare an employee object with name and age and wanted to update the age at
    latter point of time, we create a new object rather updating the same object.
  - Doing this is to avoid `state change`
  - since functional programming starts with immutable set of data, as `single source of data`.
  - advantage: 
      - the original data in a program is available and not changed.
      - program constructed is more easy to keep track of
  
##### Purity
   - a function should return the same output.
   -
   ```
   //
   ```
    
