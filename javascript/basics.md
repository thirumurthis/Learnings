##### javascript fundamentals
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
 - usage of `===` operator, this compares both the data type and the data.
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
   
