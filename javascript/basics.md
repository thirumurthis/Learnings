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
 
 
