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
