##### javascript fundamentals
  - Primitive Types and Reference Types
  - use of **`typeof`** operator
  - use of **`===`** operator
  - use of **`instanceof`** operator
  - in-built functions `toUpperCase`, `toLowerCase`, `charAt()`, etc.
  - Reference types: 
    - Objects - How to create object? using new and literal form using `{}`.
    - Arrays - How to create arrays? using new and literal form using `[]`.
    - Functions - How to create function? using new and/or Declaration and Expression approach.
    - Regular Expression - How to create RegEx object? using new and literal form using `//`.
  - What is the difference between Declaration and Expression approach in functon? `Hiositing` occurs in declaration approach.
  - Function overload
     - Like other language that supports OOPS, javascript can't support overloading. This is because by default any number of parameter/arguments can be passed.
     - when multiple function with the same name and different arguments are used, javascript engine uses the very last one.
  - `call`, `apply` and `bind` functions on object.
  - Objects
     - `in` operator to check if specific property exits or inherited in an object
     - `hasOwnProperty` function to check if specifi property owned by an object
     - `delete` operator is used to delete the property in an object. delete returns true or false

 - Primitive type
   - `string`,`number`, `boolean`
```js
  var name = "username"; //string
  var intTest = 10; //number
  var floatTest = 10.0; //number
  var boolTest = true; // boolean
  var object = null; // object as such
  var x =10;
  var y = x;  // only the value of the variable is stored
  var x = 15;
  
  document.write(x); // 15
  document.write(y); // 10
```

 - Default values of variable is `undefined`
 ```js
  var test;
  document.write(test); // prints undefined
 ```
 
 - `typeof` operator to display the type of variable
 
 ```js 
  var name = "username"; //string
  var intTest = 10; //number
  var floatTest = 10.0; //number
  var boolTest = true; // boolean
  var object = null; // object as such
  var x =10;
  var y = x;  // only the value of the variable is stored
  var x = 15;

  document.write(typeof name); // string
  document.write(typeof object); // object ->> the value stored is null
  document.write(typeof intTest); // number
  document.write(typeof floatTest); // number
  document.write(typeof x); // number
  document.write(typeof y); // number
 ```
 - usage of **`===`** operator, this compares both the data type and the data.
 ```js
 var x = 123;
 var y = "123";
 document.write(x == y);  //returns true
 document.write (x === y); // returns false since the data type is not correct (strict match)
 ```

 - methods applied over the primitive type, there are many methods below is few basic
```js
  var str="This is example string";
  
  document.write(str.toUpperCase()); // THIS IS EXAMPLE STRING
  document.write(str.toLowerCase()); // this is example string
  document.write(str.substring(3,8)); // s is
  document.write(str.charAt(5)); // i

  var numberTest = 123;
  document.write(typeof numberTest.toString()); //string

  var booleanTest= false;
  document.write(typeof booleanTest.toString()); //string
```

#### Reference type
- `Reference type` this is close to the class or objects of the type.
- instance of reference type is called object.
- This is a list or collection of properties, each property has a name and value or function.

- Types of creating Objects / reference type and add properties:
- javascript allows dyanmically add properties file.

```js 

var obj = new Object();  // in build reference type in javascript is Object.
// The obj will just point to the memory locaion where the object told.

// Adding properties
obj.property1 = "sample";

document.write(obj.property1); // sample will be displayed
document.write(JSON.stringfy(obj)); // {"property1":"sample"} will be displayed

// ANY VARIABLE REFERRING THE OBJECT REFERS TO THE MEMROY SO ANY UPDATE TO PROPERTY IMPACTS OTHER VARIABLE AS WELL
var obj1 = obj;
document.write(obj1.property1); // sample will be displayed 
```

- The memory is being allocated by the Javascript, but how to deallocate the memory (garbage collector will take care)
- by allocating the object variable to `null`, the javascript runtime engine garabage will clean up the memory reference.

###### Other in-built object type other than `Object`
 - Array  ` var intArray = new Array ();`
 - Date  ` var dateEx = new Date();`
 - Error ` var errorEx = new Error ("issue passed as string");
 - Function ` var sampleFunction = new Function("document.write('function example')");
 - RegExp  ` var sampleRegex = new RegExp ("\\d+");`  d - represents digits; + one or more digits
 
 
##### How to create object without using `new` operator in javascript using `literal form`
- Object literals
  - use the literal form starts with `{` and ends with `}`.
  - each property has a key and value. the key and value can ALSO be wrapped in double quotes like { "name": "Thiru" , "science": 90}
```js
var student = { 
   name: "Thiru",
   science: 90
}

document.write(student.name); //Thiru is displayed
```
- Array literals
   - use the literal form starts with `[` and ends with `]`
```js
 var names = ["Thiru1","Thiru2","Thiru3"];
 
 document.write(names[0]); //Thiru1
 
 var namesArray = new Array("thiru1","thiru2", "thiru3");
 document.write(namesArray[0]); //thiru1
```

- Function literals
  - Don't use the new operator to create a function.
  - Literal way is use `function functionName (){ ... }`
```js

function printName(name){
  return "Hello "+ name;
}
document.write(printName('Thiru')+ "<br/>"); // Hello Thriu
```

- Regular Expression literals
  - The regex literal starts with `/` and ends with `/`

```js
var regExDemo = /\d/; // note there is no need to escape the backspace.

document.write(regExDemo.exec(10)); // prints 1 since we are not using +
```

#### `typeof` oeprator on different objects
```js
var student = { "name":"Thiru" };
function printName(name){return "Hello " + name;};
var namesArray = ["Thiru1","Thiru2"};

document.write(typeof student); // Object
document.write(typeof printName); // function 
document.write(typeof namesArray); // Object

```
 - Note: the type of coudn't differentiate the Ojbect type in this case use `instanceof 

##### `instanceof` usage
```js
var student = { "name":"Thiru" };
function printName(name){return "Hello " + name;};
var namesArray = ["Thiru1","Thiru2"};

document.write(student instanceof Object); // true
document.write(printName instanceof Function); // true
document.write(namesArray instanceof Array); // true
document.write(namesArray instanceof Object); // true  - Array is a type of Object

// if adding a string to use br string then use () 
document.write((namesArray instanceOf Array) + "<br/>"); // Object
```

##### Wrapper types for javascript primitive types (similar to java where we have Integer, Float, etc)
 - Number
 - String
 - Boolean

```js
var str1 ="Test Contect";

var temp = new String("Test content");
var s= temp.substring(3,5);   // wrapper types are internally by javascript engine and then it will remove or not accessible.
```
 - NOTE: Don't use the wrapper type in development as it will cause confusion and error.
 
```js 
 var str = "string test";
 str.name = "test";  //str is of wrapper type internally, on setting will display.
 
 document.write(str.name); // undefined
 
 document.write( str instanceof String); // true
```

##### `Functions` in javascript are Objects
  - the difference between function and other objects is that `function has internal property called **call**`.
  
 Creating function:
   - Decleartion approach
   ```js
     function evenOrOdd(num){
        if (num%2 == 0) return "even";
        else return "odd";
     };
     document.write(evenOrOdd(12)); //even
   ```
   - Expression approach 
      - There is no name in expression approach, instead assign to a variable.
   ```js 
    var evenOrOdd = functon (num) {
      if (num%2 == 0) return "even";
        else return "odd";
    };
    document.write(evenOrOdd(7)); //odd
   ```
   
  Note: Each function are reference type in javascript. This can be assigned to a variable.
  
 ###### Difference between the declearing and expression representation of function.
   - code hoisting happens.
   - for example, lets invoke the function before declaring it.
   - function `hoisting` happens and executes it. (kind of setting global scope)
   
 - in declaration syntax `hoisting` happens and workes   
 ```js
 document.write(evenOrOdd(12));  //even - executed even the function is declard after using
 function evenOrOdd(num){
   if(num%2 == 0) return "even";
   else return "odd";
  };
 ```
  
  - in expression type `hositing` doesn't occur since variable being used
   ```js 
    document.write(evenOrOdd(7)); // issues evenOrOdd is not decleared or used
    var evenOrOdd = functon (num) {
      if (num%2 == 0) return "even";
        else return "odd";
    };
   ```
##### passing function as parameter
```js
   var scores = [15,24,102,85,09];
  scores.sort();   // in javascript the sort uses the array values as strings and uses it for sort
  document.write(scores); // NOT sorted in expected order
  
  // how to fix the above issue, we can pass a functon to sort function
  
  scores.sort(function(n1,n2){
    return n1-n2;  //like  comparator, using anonymous function
  });
  document.write(scores); // now the output is sorted
```

##### Any number of parameter can be passed to a function
  - the additional parameters are ommitted.
```js
 var printName = function(name){ return "Hello "+name;};
 document.write(printName("one","two"); // Hello one
```
  -NOTE: **How to access the additional parameter passed in to a function.**
    - each function has additional variable called `arguments`, an array to hold extra parameter passed in.
    
```js 
function add(num1,num2){
  document.write(arguments); // [ object Arguments] is displayed
  document.write(arguments[3]); //4 is printed 
  document.write(arguments.length); //4
  
  // TO CHECK THE LENGHT OF THE ACTUAL ARGUMENTS PASSED IN A FUNCTION
  document.write(add.length); //2  add is the function itself
  
  return num1+num2;
};
document.write(add(1,2,3,4));  //3 (1+2)
```
- usecase of arguments

```js

function productNumber(){
var output =1;
 for (var i=0; i<arguments.length;i++){
    output *= arguments[i];
  }
return output;
};
document.write(productNumber(1,2,3)); //6
document.write(productNumber()); //1
document.write(productNumber(1,20,3)); //60
```

##### Function overloading
  - function name same with different number of arguments or return type.

```js

function add (num1,num2,num3){ return num1+num2+num3;};
function add (num1,num2){ return num1+num2;};

document.write(add(10,20,30); // expected result 60 but result is 30

// The reason is the overloading is not supported since by default function can take any number of parameter

// why 30, the javascript engine considers only the last function when there are multiple function with the same name. so engine override and uses the last one.
```

##### How to achive overloading in javascript?
  - using the length of the `arugments` array data structer with if else condition based on the length. 
      - Even in this case we use only one function rather than two function to achive it. 
  Or
  - the other way is to pass in named parameters and check if those are undefined.

#### How to add `functions` to an object
```js
var student = {
  id: 1,
  name: "thiru",
  display: function(){
    document.write(student.id);
    document.write("<br>");
    document.write(student.name);
  }
};
  student.display();
```
 - with reference to the above example, how to reuse the above display function. Use `this`
 - `this` is the object
```js
var student = {
  id: 1,
  name: "thiru",
  display: function(){
    document.write(this.id);
    document.write("<br>");
    document.write(this.name);
  }
};
  student.display();
```
 - Now like above example, since we have used `this` operator, the display function can be extracted outside like below.
 ```js
function displayDetails(){
    document.write(this.id);  //usage this
    document.write(" ");
    document.write(this.name);  //usage this
  } 
var student1 = {
  id: 1,
  name: "thiru-11",
  display: displayDetails
};

var student2 = {
  id: 2,
  name: "thiru-12",
  display: displayDetails
};

  this.id = 4;
  this.name ="global"  //This here available in gloal scope. If this is not decleard
  
  student1.display(); //1 thiru-11 
  student2.display(); //2 thiru-12 
  displayDetails(); // 4 global   (undefined in case if the this.id and this.name is not used)  
```

##### `call`, `apply` and `bind` methods in javascript.
  - `this` variable is replaced with in the method scope if used within function.

 - **`call`** method usage

```js
function displayDetails(){
    document.write(this.id);  //usage this
    document.write(" ");
    document.write(this.name);  //usage this
  } 
var student1 = {
  id: 1,
  name: "thiru-11"
};

var student2 = {
  id: 2,
  name: "thiru-12"
};

  this.id = 4;
  this.name ="global"  //This here available in gloal scope. If this is not decleard
  
  displayDetails.call(student1); // 1 thiru-11   
  displayDetails.call(student2); // 2 thiru-12
  displayDetails.call(); // 4 global   (the global scope no arguments passed with call)
``` 
 -Note: The advantage of call, is not need to define the function within the object itself.
 
 - `call` with arguments
 ```js
function displayDetails(score){
    document.write(this.id+" ");  //usage this
    document.write(this.name+" ");  //usage this
    document.write(score +" ");
  } 
var student1 = {
  id: 1,
  name: "thiru-11"
};

var student2 = {
  id: 2,
  name: "thiru-12"
};

  this.id = 4;
  this.name ="global"  //This here available in gloal scope. If this is not decleard
  
  displayDetails.call(student1,10);  //with arguments
  displayDetails.call(student2,20); 
  displayDetails.call(this,30); 
``` 

 - **`apply`** method usage. This is similar to call method, except it takes `arrays of arguments`

 ```js
function displayDetails(score1,score2){
    document.write(this.id);  //usage this
    document.write(this.name);  //usage this
    document.write(this.score1+ " " +this.score2);  
  } 
var student1 = {
  id: 1,
  name: "thiru-11"
};

var student2 = {
  id: 2,
  name: "thiru-12"
};

  this.id = 4;
  this.name ="global"  //This here available in gloal scope. If this is not decleard
  
  displayDetails.apply(student1, [10,20]); 
  displayDetails.apply(student2,[20,30]); 
  displayDetails.apply(this,[10,40]); 
``` 
 - **`bind`** method usage, this is different from call and apply, using bind an object can be dynamically bounded.
 
 ```js
function displayDetails(score1){
    document.write(this.id);  //usage this
    document.write(this.name);  //usage this
    document.write(this.score1+ " ");  
  } 
var student1 = {
  id: 1,
  name: "thiru-11"
};

var student2 = {
  id: 2,
  name: "thiru-12"
};

this.id = 4;
this.name ="global"  //This here available in gloal scope. If this is not decleard
  
//bind will return a value which needs to be stored
var displayStud1 =  displayDetails.bind(student1); 
displayStud1(100);

//bind with passing param within
var displayStud2 = displayDetails.apply(student2,20 ); 
displayStud2();

//what happens if we are assigning the displayStud2 displaydetails with displayStud1

displayStud2.displayDetails = displayStud1;
displayStud2.displayDetails(90); // prints 90 invoking the displayStud1 since that is resassigned and displayStud1 is bound to  object initally bounded here is student1.
```

### javascript `Object` in detail
   - How to updated properties in object
   - How to stop modifying the object by restricting it.

- `put` function
```js
  var subject1Info = {
    name: "Javascript"  //Internally the javascript engine created [[put]] function
  };
  
  subject1Info.name="Javascript details"; // javascript internally uses [[set]] function to allocate the value to the property
  
  var subject2Info = new Object();
  subject2Info.name = "Java"; // initally the [[put]] method is called
  
  subject1Info.description = "End to End Javascript info"; // [[put]] is internally invoked, when the property is new one for the object.
```

#### How to check if the property exits in an object? use `in` operator
 - in is applicable to object and function properties.
```js

var car1info = { 
 name: "Toyota,
 year : 2000
 };
 
 console.log("year" in car1Info);  // true - checks the properties
 console.log("description" in car1Info); //false - checked
```
- Note: `in` operator can also check the properties inhertied by the object

```js
var car1info = { 
 name: "Toyota,
 year : 2000
 };

console.log("toString" in car1Info); // true - the toString is inherited from parent object
```

#### use `hasOwnProperty` is used to check if specific property is owned by the object
```js
var car1Info = { 
 name: "Toyota,
 year : 2000
 };

console.log(car1Info.hasOwnProperty("toString")); // false - the toString is inherited from parent object
```

#### `delete` operator on object

```js
var car1Info = { 
 name: "Toyota,
 year : 2000
 };

delete car1Info.year;  // [[Delete]] method is invoked internally by Javascript engine
console.log(year in car1Info); // false
```
