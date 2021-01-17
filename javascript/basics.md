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

#### Retriving properties from object in javascript.
  - when an property is added to an object, an `Enumerable` property is set to true.
```js
var car1Info = { 
 name: "Toyota,
 year : 2000
 };
 
 // in runtime to display the propties in object
 for( var property in car1Info){
   console.log(property);   // displays name, year the key of the property.
   console.log(car1Info[property]); // this will display the value of the property
}
``` 

##### Instead of using the for loop in the above example, there is an `keys` properties
```js
var car1Info = { 
 name: "Toyota,
 year : 2000
 };

var allProperties = Object.keys(car1Info); // all properties is returned as array

for (var i= 0; i< allProperties.length; i++){
  console.log(allProperties[i]); // this displays the properties name
  console.log(car1Info[allProperties[i]]);
}
```

#### Note: Not all the properties in built are NOT Enumerable.
  - How to check enumerablity?
```js
var car1Info = { 
 name: "Toyota,
 year : 2000
 };

console.log(car1Info.propertyIsEnumerable("name"); // this displays true; but ceratin in-built object it will be false.

// array length properties are not enumerable
var allProperties = Object.keys(car1Info);
console.log(allProperties.propertyIsEnumerable("length"));
```

#### `accessor` and `mutator` method like `getter` and `setter`. This is a property in javascript.

```js 
var car = new Object();
car.name= "honda" ; // name is called data property since it hold data

//accessor property example
// note _ variable/properties in javascript is private by convention.
var userDetails = {
  _name: "Thiru", 
  get name(){   // this is accessor propertoes
    return this._name;
    },
  set name(value){
  this._name = value;
  }
};

// the userDetails _name property is accessed only using get and set

console.log(userDetails.name); //note name here is the get name() property

userDetails.name = "God"; 
console.log(userDetails.name); // God will be displayed

```
 - `Note`: there is no need to specify `get` and `set` be used always, only when there is special requirements.
 - If we use the `get name()..`, then this value will be read only
 - similary if we use `set name()..`, then this value will be write only
 
##### `Enumerable` properties in object
  - How to set the enumberable property to true for specific property.
  - Below properties is only availabe starting ECMA6
```js
var userDetails = {
  name: "Thiru"
 }
 console.log (userDetails.propertyIsEnumerable("name")); // true
 
 // use definedProperty function passing object itself, attribute/property which needs to be set with enumerable and the object with internal properties.
 Object.defineProperty(userDetails,"name", 
 {
   enumerable: false
 });
 console.log("name" in userDetails); // true since name is a property
 
 console.log (userDetails.propertyIsEnumerable("name")); // false since we have updated it to false using defineProperty
```

#### `configurable` property which is common to both data and accessor properties
  - The `configurable` property determines whether the property can be modified or Not.
  - if set to false the property can't be changed or deleted.
  
```js
var userDetails = {
  name: "Thiru"
 }
 
 // use definedProperty function passing object itself, attribute/property which needs to be set with enumerable and the object with internal properties.
 Object.defineProperty(userDetails,"name", 
 {
   configurable: false
 });
 
 delete. userDetails.name;  // this don't display an error since it is NOT executed in strict mode.
 
 console.log( "name" in userDetails); // true  since the property is cannot be deleted.
 console.log(userDetails.name); 
  
 Object.defineProperty(userDetails,"name", 
 {
   configurable: true
 });
 // above will be creating error, once configurable is marked to false it can't be updated to true  
```
 - Note: running the javascript in strict mode, the above code will display error.
 - Also trying to redefine the configurable to true again will display error.
 
#### Data specific property 

```js
var carInfo = {};
// we are adding a new property name here, passed as second parameter/argument.
// defineProperty () adds the property if not available
// else if the property is avilable, it will set the attributes passed.

Object.defineProperty(carInfo,"name",{
 value: "Thiru",
 enumerable: true,
 configurable: true,
 writable: true  //this will tell the property is writable, if false this will be read only 
});

// by default the boolean value for the enumerable,configurable,writable are false
console.log(carInfo.propertyIsEnumerable("name"); // true since we set it; if we didn't in the above it will be false since it is default.

delete carInfo.name; // if configurable is false this will be deleted else not deleted

carInfo.name="Audi"; // this value will be replaced since writable is set to true; if writeable is false this will not be updated;
console.log(carInfo.name);
```

##### accessor attribute `get` and `set` properties
  - The accessor properties can be applied to the existing object without changing any other property. when only ue the get and set.
  
```js
var carInfo = {
 _name: "Thiru"
 };
 
// basically using the accessor
Object.defineProperty(carInfo,"name",{
  get : function() {  // this is poperty set on the name property itself
    return this._name;
    },
  set: function (value){
    this._name = value;
  }
});
```
  - Note: If the `enumerable` and `configrable` on the properies it can be made read only or write only.
  
```js 

var carInfo = {
 _name: "Thiru"
 };
 
Object.defineProperty(carInfo,"name",{
  get : function() {  
    return this._name;
    }
});

console.log("name" in carInfo); //true
console.log(carInfo.propertyIsEnumerable("name")); //false - note name property added using defineProperty()

delete carInfo.name;   // since configurable is false by default it will not delete
console.log("name" in carInfo); // true
carInfo.name = "Honda"; 
console.log(carInfo.name); // Thiru since set is not configured using defineProperty
```

##### How to create mulitple property using `defineProperties`, since we used defineProperty to create single property. 

```js

var carInfo {};

//pass the object itself, with multiple properties it can be update or create new one with the properties as needed
Object.defineProperties(carInfo,{
  _name: {
    value= "Honda";
    enumerable=true;
    configurable=true;
    writeable=true;
    },
    name:{
      get: function(){
         return this._name;
         },
      set: function(value){
         this._name = value;
         },
         enumerable: true,
         configurable: true
         }
      });
```
 - With the above declearion, how to retrive the data of the property
```js 
  var carInfo = {
   name : "honda"
   };
   
 var details=   Object.getOwnePropertyDescriptor(carInfo,"name");
 console.log(details.enumerable); //true
 console.log(details.configurable); //true
 console.log(details.writable); //true
 console.log(details.value); // honda
 // by default when creating object the enumerable, configurable, writable is true
 // when object updated with defineProperty(), then the default is false.
```

##### Object Extensible - This attribute takes true or false, true means the object can be extended. That is any property can added if object is extensible.
 - Non Extensible
 - Sealing object
 - Freezing object

```js
var car = {
  name : "Honda"
  };
console.log(Object.isExtensible(car)); //true  by default

// to prevent the extensibility use 
Object.preventExtensions(car);
console.log(Object.isExtensible(car)); //false  

car.price = 30000;  // this fails silently since we are running in NON-STRICT mode
consle.log("price" in car);
```

##### seal it means the object will be non extensible and non configurable
```js
var car = {
  name : "Honda"
  };
console.log("isExtensible? : "+Object.isExtensible(car)); //true
console.log("isSealed? : "+Object.isSealed(car)); //false

// how to seal the object 

Object.seal(car); // pass the object to seal

console.log("after seal isExtensible? : "+Object.isExtensible(car)); //false
console.log("after seal isSealed? : "+Object.isSealed(car)); //true 

//adding property will not work
car.price = 10000;
console.log("price" in car); //false
// deleting will also not work - fails silently since NON-STRICT mode
delete car.name;
console.log("name" in car); //true

// get the properties of the object car
var objDescriptor = Object.getOwnPropertyDescriptor(car,"name");
console.log(objDesciptor.configurable); // false since it is sealed already
```
#### Freezing object means the object will be sealed and non writable. `isFrozen`
  - not extend, configure and non writable
```js 
var car = {
  name : "Honda"
  };
console.log("isExtensible? : "+ Object.isExtensible(car)); //true
console.log("isSealed? : "+ Object.isSealed(car)); //false
console.log("isFreeze? : "+ Object.isFrozen(car)); //false

// To freeze an object use 

Object.freeze(car); //pass the object 

console.log("after freeze isExtensible? : "+ Object.isExtensible(car)); //false
console.log("after freeze isSealed? : "+ Object.isSealed(car)); //true
console.log("after freeze isFreeze? : "+ Object.isFrozen(car)); //true

// cannot assign new value
car.name="toyota" 
console.log(car.name); // Honda - the value cannot be done.
// cannot add new property

var objDescriptor = Object.getOwnPropertyDescriptor(car,"name");
console.log(objDesciptor.writable); // false since it is freezed already
```
#### `Constructors` and `Prototypes` in javascript
  - `constructors` - using constructors we can create any number of similar object with same properties. And initialize those properties during object creation using constructor.
  - `prototypes` - is a way to share properties accross multiple objects.
      - Every object in javasctipt has the `prototype` property.
 
 - When using `new` object the construcor is invoked and returns an object.
 - constructor is function.
 - by convention the name of the constructor will start with upper case.
 
```js 

// constructor - convention first char is 
function Vehicle(){
}

var car1 = new Vehicle;
var car2 = new Vehicle;

console.log(car1 instanceof Vehicle); //true
console.log("is car2 instanceof vehicle? :  "+ (car2 instanceof Vehicle)); // is car2 instanceof vehicle? :  true

// checking the constructor instance

console(car1.constructor === Vehicle); // true note the === approach is not used often, better to use instanceof operator
```
  
  - Define a properties to the constructor  
 ```js
  function Vehicle(model,year){
   this.model = model;
   this.year = year;
   
   this.display = function(){
    console.log(this.model);
    console.log(this.year);
    };
 }
 // the constructor now take arguments
 var car1 = new Vehicle("toyota",2002);
 var car2 = new Vehicle("honda",2003);
 
 car1.display();  //calling function in the contructor
 car2.display();
 ```
 
 - Explictly return object from the constructor. the new object is explicitly returned.
 - if we return primitive type like 123, it will be ignored by constructor.
 - we can use the accessor type like using `defineProperty()` inside the constructor.
 
#### `prototypes` info
 - when we create a function, every function contains a property called `prototype`.
 - all the function created with that object, will share the prototype
 
 - prototype is a property on a function, a prototype in turn can have functions and variable/properties defined on it. all these properties and functions of the prototype can be used by the object.
 
 ```js
 
  function Vehicle(model,year){
   this.model = model;
   this.year = year;
   
   this.display = function(){
    console.log(this.model);
    console.log(this.year);
    };
 }
 // the constructor now take arguments
 var car1 = new Vehicle("toyota",2002);  // the car1 gets seperate memory
 var car2 = new Vehicle("honda",2003);   // the car2 gets seperate memory
 ```
   - in the above example, say the function display will be referenced in each variable created and occupies memory.
   - if we creat this display function as a prototype function, it can save memory.
   - when object creating the prototype properties are reused.
   
- in built prototypes:

```js
var user= {
  name: "Thiru"
  };
 console.log("name" in user); //true
 console.log(passenger.hasOwnProperty("name")); //true
 console.log("hasOwnProperty" in user); // true
 console.log(user.hasOwnProperty("hasOwnProperty")); // false check if the property is own property
 
// Object.prototype.hasOwnProperty  <== is the actual representation that is the reason previous console returns false

console.log(Object.prototype.hasOwnProperty("hasOwnProperty")); // true
//toString is aslo on the prototype.
```
- **How to access the  objects prototype and check details of prototype**

```js
 var car = {};
 
 // to get the prototype details
var prototypeDetails = Object.getPrototypeOf(car);

console.log(prototypeDetails === Object.prototype); //true

//testing if the object is prototype passing the object itself
console.log(Object.prototype.isPrototypeOf(car)); //true
```

`NOTE: -` When accessing a property on an object, the javascript engine first checks the object if property exists the value is returned.
if the value doesn't exists, the engine checkes the properties available in prototye. if exits value is returned  else returns `undefined`.

```js
 var car = {};
 console.log(car.toString()); // [object Object ] // prototype value
 
 // to create our own toString function on object
 
 car.toString = function(){
   return "Custom tostring";
 };
 
 console.log(car.toString()); // custom tostring is displayed
 
 delete car.toString;
 console.log(car.toString()); // [object Object] // prototye value is returned
 
 // the prototype toString cannot be deleted. if we try to delete again
```

- **How to create or access the prototype in the object**

```js
 function Vehicle(model,year){
   this.model = model;
   this.year = year;  
 /* below consumes memory if we create 100 similar object.
 this.display = function(){
    console.log(this.model);
    console.log(this.year);
    };
    */
 };
 // below is how we create prototye on an object
 Vehicle.prototpye.display=function(){
    console.log(this.model);
    console.log(this.year);
 };
 
 var car1 = new Vehicle("honda",2002);
 car1.display(); //prints honda 2002 invoking the prototype
```

##### How to add multiple properties in object prototypes, use the object literal approach `{}`.

```js
function Vehicle(name, year){
 this.name = name;
 this.year = year;
 }
 
 //literal approach, where the object contains multiple function
 Vehicle.prototye = {
   display: function(){
      console.log(this.name);
      console.log(this.year);
      },
  toString: function() {
      return "name: "+this.name+ " ; year: "+ this.year;
   }
   };
   
   var car1 = new Vehicle("honda",2001);
   car1.display();
   console.log(car1.toString());
```

##### constructor proprerty is within the prototype so use prototype define 
```js
function Vehicle(name, year){
 this.name = name;
 this.year = year;
 }

var car1 = new Vehicle("test",2001);

console.log(car1 instanceof Vehicle); //true
console.log(car1.constructor === Vehicle); //false (only when below prototype code is not added and executed.
console.log (car1.constructor === Object) //true - this becaue we didn't set the constructor in the prototype

//the contsructor is property of prototype
Vehicle.prototye = {
   constructor: Vehicle, // this is how to define constructor in the prototype
   display: function(){
      console.log(this.name);
      console.log(this.year);
      },
  toString: function() {
      return "name: "+this.name+ " ; year: "+ this.year;
   }
   };
console.log(car1.constructor === Vehicle); //true

```

##### Adding prototypes to inbuilt object, this should be used with caution since it might confuse other user assuming the custom property is actual property of the inbuilt object.
```js

String.prototype.display= function(){
  console.log(this);
  };
  
  "Test".display();

```

#### Inheritance in javascript using prototype chaining.
```js

var car = { name: "toyota"};

console.log(car.hasOwnPropety("name")); // the car object doesn't have hasOwnProperty this is inherited from parent object

var prototypevar = Object.getPrototypeOf(car);
console.log)prototypevar === Object.prototype);
```

##### Object.create usage for inheritance
```js

 var car = {
    name: "Toyota"
    };
    // first parameter passed is the prototype pointer or property    
    // similar to define property syntax, the name property of the car is defined here
    Object.create(Object.prototype,{
       name: {
          configurable: true,
          enumerable: true,
          value: "honda",
          writable: true
    });
    
    //Here we are creating an object explicitly and assign a prototype using create property.
    // the Object.prototye passed will be used on any new object created 
```

#### How to inherit an property in object from user defined object
```js 
var car1 = {
 name: "Toytoa",
 display: function (){
    console.log(this.name);
    }
  };
  
  var car2= Object.create(car1, {
    name: {
       configurable: true,
       enumerable: true,
       value: "NEW CAR",
       writable: true
     }
  });
  
  car2.display();  // NEW CAR
// though the car2 is not set with display explicitly the method is inherited from car1 obj
  car1.display(); // Toyota
```
   - if we define the same properties in the child will shadow the same proeprties in the parent.
   
 - `prototype chaining` - in order to find the proeprty in an object, the javascript engine checks for the property in that object, then prototype, its parent object and its prototype.
 
 ##### constructor inheritance
 
 ```js
 function Vehicle(){
 
 }
 //javascript engine does the following implicitly.
 
 Vehicle.prototype = Object.create(Object.prototye,{
   constructor: {
     configurable: true,
     enumerable: true,
     value: Vehicle,
     writable: true
   }
   });
 ```

```js
function Teacher(name){
  this.name = name;
  }
  
  Teacher.prototype.coursestatus = function(){
    return "completed";
  }
  
  //shadowing the toString method
  Teacher.prototype.toString = function(){
    return "[Teacher "+this.name+" ]";
  };
   
   // any object that uses the Teacher will get the subject and toString.
   
   function English(name, experience){
    this.name = name;
    this.experience = experience;
   }
   
   English.prototype = new Teacher();
   English.prototype.constructor = English;
   
   var teacher = new Teacher("Thiru");
   var english = new English("Thiru", "10 year");
   
   console.log(teacher.coursestatus()); // completed
   console.log(english.coursestatus()); // completed
   console.log(teacher.toString()); // [ Teacher Thiru ]
   console.log(english.toString()); // [ Teacher Thiru ]
   // check 
 //  english instanceof Teacher
  // english instanceof Teacher
   
```
##### `supertype` in java we use super keyword. in javascript if we need to access the property of the parent class use `ParentObject.call()`. 
   - use objectName.functonName();

```js
function Teacher(name){
  this.name = name;
  }
  
  Teacher.prototype.coursestatus = function(){
    return "completed";
  }
  
  //shadowing the toString method
  Teacher.prototype.toString = function(){
    return "[Teacher "+this.name+" ]";
  };
   
   // any object that uses the Teacher will get the subject and toString.
   
   function English(name, experience){
   Teacher.call(this,name); // this is done rarely equivalent to super using .call
    this.name = name;
    this.experience = experience;
   }
   
   English.prototype = new Teacher();
   English.prototype.constructor = English;
```
##### How to override the prototype proerperties of parent.
```js

function Doctor(name){
  this.name = name;
  }
  
  Doctor.prototype.speciality = function(){
    return "general";
  };
  
 function Surgeon(name,type){
   Doctor.call(this,name); // calling the parent calls constructor
   this.name = name;
   this.type= type;
   }
 
 Surgeon.prototype= Object.create(Doctor.prototype,{
   constructor:{
     configurable: true,
     enumerable: true,
     value: Surgeon,
     wirtable: true
     }
     });
     //to override the prototype property from parent after declaring the prototype add same method to Surgeon
     
     Surgeon.prototype.speciality = function(){
      // calling the parent doctor function
      return  Doctor.prototype.speciality.call(this) + "Surgery";
     };
     
  var surgeon1 = new Surgeon("David","Ortho");
  surgeon1.display(); // general Surgery 
 }
```

### Patterns in java `Module pattern`, using `IIFE` (Immediately invoked function expression)
  - Module pattern gives a way to package the properties and functions.

  - IIFE looks like below:
  ```js
    var myObj = (function() {
        // data declaration - this will become private data variables
        return {
            // object is returned. public methods and properties
            // this function is returned, this is called privilged functions
            // since these function can access the private variable above
            // the privat variable is not available at object level only in this function scope.
         };
      }());// () represents invoked immediately - similar to encapsulation in java
  ```
  
  ##### How to implement the module pattern using IIFE
  ```js
  // below is the anonymous function which is created and invoked immediately
  var account = (function(){
       var balance = 10;  // balance is private variable not accessible to public.
       return {
           username : "Thiru",
           getBalance : function (){
              return balance;  //the private variable is accessible here only
              },
              addInterest: function(interest){
                balance+=interest;
              }
            }
         }());
    
    console.log(account.username); //Thiru
    console.log(account.getBlance()); //10
    
    account.balance = 100; 
    console.log(account.getBalance()); //10 is printed since the balance can only be accessed via addInterest
    account.addInterest(10);
    console.log(account.getBalance()); //20 - since it addes the 10 to the balance.
  ```
   - NOTE: only way to access the balance is using the function. this is the key point in module pattern.
 
 ##### `Revealing module pattern` is a variation of module pattern where we can put the data and the function together
 
 ```js
  var account = (function (){
     var balance =10;
     function getBalance(){
       return balance;
       }
    function addIntrest(intrest){
       balance+= intrest;
      }
    
    retunr {
       username: "Thiru",
       retrieveBalance: getBalance,  //we can also use getBalance which is used to access
       addIntrest: addIntrest
       };
     }());
 ```
 
 ### `ES6 features`
   - block scope using `let`
   - `const` for constant variable
   - template string using \`\`
     - variable substitution done using `${name}`
   - De-structuring 
     - forEach
     - for-in
     - for-of
   - `Map` and `Set` datastructure
   - `Classes` is supported.
   - `Modules` support - export out function in one module and others.
   - Note: not all the browser supports ES6
  
#### Details of scope:
   - until ES5, we had Global scope and function scope.
   - we can use let for block scope.

```js
  function letDemo(){
  
   var i=10;
    for (var i=0; i<=5;i++){ //when using var the i is restarts from 0 
       console.log(i);
       }
   console.log(i); //when the for loop used var i, then this will impact variable with function scope in this case since it is declared within a function.
   
   
   var j=10;
    for (let j=0; j<=5;j++){ //using let in this case make scope of j to block only
       let x = 10; // let will be only block scope
       var y = 10;  // var will be accessible to global scope since it will be hoisted
       console.log(j);
       }
   console.log(j); //10
   console.log(y);// 10
   console.log(x); // reference error in console
```

##### If the variable not to be changed then use const, the variable will become immutable.
   - `const` like `let` defines a variable at a block level.
 
```js
"use strict" // this enables the strict mode

const product = {}; // once we defined an object we can reassign but we can add value

product['name']="laptop"; // Make a note, declaring constant on object, the value can be updated. use the Object.freeze like 

const product2 = Object.freeze({}); //cann't be modified 
product2['name'] = "laptop" // this will display the erorr now since we enabled STRICT mode.
```

##### Template strings

```js
  let temp = `this
  is
  me`; // the output will retain the line breaks in this case
  let name="Thiru";
  let templateString = `this is ${name}, hello`; // this is Thiru, hello - will be printed
  
  console.log(temp);
  console.log(templateString);
```
#### Arrow functions
  - is a syntax Arrow function is used to create `anonymous function`. like lambda in java.
  - Example ` var add = (num1, num2)=>num1+num2; console.log(add(1,2));` //3
  - the arrow function can stored to an variable and if needed can be passed as parameters.
  - Examples:
   - ` var add = (n1,n2)=> { console.log(n1+","+n2); return n1+n2;};`
   - ` let x = ()=>console.log("demo arrow"); x();`
   
### De-Structuring

#### Object de-structuring, assigning values to variable
   - new syntax to store the object values
```js
  let car={
   model: "corola",
   make: "toyota",
   year:2010
   };
   
   // Before ES6, below is one way to store values
   const carmodel = car.model;
   const carmake = car.make;
   const caryear = car.year;
   
   // In ES6, due to de-structuring option we can use below 
   
   const {model: carmodel, make: carmake, year:caryear} = car;
   console.log(carmodel);
   console.log(carmake);
   
   const {model:model, make:make,year:year} = car; // we can use the same name of object proeprty
   console.log(model);
   console.log(make);
```

#### Array de-structuring
```js

const courses = ['Angular','Vue','React'];

//use [ ] 
const[course1,course2,course3] = courses;

console.log(course1);
console.log(course2);
console.log(course3);
```

#### function de-structuring
 - passing dynamic parameter to function
```js

function addOlderway(options){ // older way
   console.log(options.n1+options.n2+options.n3);
   };
add({n1:10,n2:20,n3:30}); // 60 

function add({n1,n2,n3}){ // de-structured approach
   console.log(options.n1+options.n2+options.n3);
   };
add({n1:10,n2:20,n3:30}); // 60 

function add({n1=1,n2=1,n3}){ // passing default values for parameter needed
   console.log(options.n1+options.n2+options.n3);
   };
add({n1:10,n2:20,n3:30}); // 60 
```

#### for-of loop (this overcomes the limits of forEach and for-in loop)

```js
//using for of to iterate through arrays

let courses = ['network','java','ML'];

for( let value of courses){
   console.log(value);
}
```

### ES6 provides `map` datastructure. This was not avialable in ES5.

```js
let scores = new Map();
// set and get
scores.set("english",80);
scores.set("science",90);

console.log(scores.get("english"); //80
console.log(scores.get("science"); //90

// other functions
console.log(scores.size);
console.log(scores.has("science");// true science present in the array
console.log(scores.has("social"); // falise social not present in the array

scores.delete("science");
console.log(scores.has("science"); // false

scores.clear(); // clear will delete all the elements in the map;
console.log(scores.size); //0 will be displayed

// another options to declare map values is 

let scoresAlternateWay = new Map([["english",80],["science",90]]); // note starts with dobule square bracket.

console.log(scoresAlternageWay.get("english"));
```

##### iterate the Map to get the keys using for-of
```js
let scores = new Map([["english",80],["science",90]]); 

//get the keys of the map
for(let key of scores.keys()){
  console.log(`key: ${key} & value: ${scores.get(key)}`);
}

//get the values of the map
for (let value of scores.values()){
  console.log(value);
}

//get the entries of the map
for(let entry of scores.entries()){  //entries() method give key value as array data
   console.log(entry[0],entry[1]); // 0 has the key and 1 has the value
 }
 
for( let [k,v] of score.entries()){ //using array de-structuring way to save key,value
console.log(k,v);
```

##### `set` like map stored key value. The set can only store unique values.
```js

let courses = new Set();

courses.add("Network");
courses.add("Java");
courses.add("ML");

for( entry of courses){
  console.log(entry);
 }
 
console.log(courses.size); //3
console.log(courses.has("Network")); // true
courses.clear();
console.log(coureses.size); // 0

//initialize the set with chaining - another syntax to initialize set
let courses1 = new Set().add("Network").add("Java").add("ML");

// another way to initialize set 
let courses2 = new Set(["Network","Java","ML"]); // pass array of elements to set

```

### Classes are ES6 feature, which used to create classes like java. 
- it is like a blue print of an object
- prior to ES6, the prototype was used to create class which was complecated
```js
class Student{
    constructor (name) {
      this.name = name;
     }
    display(){
      console.log(this.name);
     }
  }
```

#### Create a class
```js
class Passenger{

 //use this to assign property and initialize properties
 // constructor is key word
   constructor(firstName,lastName,modeOfTransport) { 
   this.firstName = firstName;
   this.lastName = lastName;
   this.modeOfTransport = modeOfTransport;
   } 
}

//use let as much as possible that var when coding
let passenger1 = new Passenger("Berry","Allen","fly");
console.log(passenger1);

let passenger2 = new Passenger("Java","Scala","road");
console.log(passenger2);
```

### Inheritance, using `extends`
  - accessing the existing functionality
  - update the eixsting functionality or override
  - parent object car, child: BMW, AUDI are type of car but has common properties.
  
```js 
class BMWCar{
   constructor (make,model,year){
     this.make= make;
     this.model= model;
     this.year = year;
     }
     
     start(){
       console.log("start");
     }
     stop(){
       console.log("stop");
     }
}
 class ThreeSeries extends BMWCar{
    constructor(make, mode, year, cruiseControl){
      super(make,mode,year); // invoke parent class constructor
      this.cruiseControl = cruiseControl;
  }
 }
 
 class FiveSeries extends BMWCar{
    constructor(make, mode, year, parkingAssit){
      super(make,mode,year); // invoke parent class constructor
      this.parkingAssit = parkingAssit;
 }
  
  // this is way to override the parent class in child class
  start(){
     console.log("remote start");
  }
}

 let series3 = new ThreeSeries("BMW","300",2020,true);
 let series5 = new FiveSeries("BMW","500",2020,true);
 
 console.log(series3.make, series4.model);
 
 //accessing the function properties of parent
 
 series3.start();
 series3.stop();
 series5.start(); // remote start  
```

### `Promise` - is a placeholder for something in future/
  - this is used in asyncronous call
  - before ES6, we used have callback function to perform the async
  - example call sequence,
     - 1. connect(); 
     - 2. dbCall(); //takes time, where async can be used to wait for promise
     - 3. restCall();
  - has better error handling 

- Create a new promise (below sample):
```js
// the function returns the promise as we declared the template in variable 

function myAsyncFunction{

//promise object constructor takes a inner function as parameter.
// within this function the async code is written
// here we are passing as arrow function
let promise = new Promise( (resolve,reject)=> {   
// resolve, reject - to tell that we have completed he function the process is done
// the resolve, reject - is used by convention it can be response,error also
// by usage, to arguments need to be passed

  // below async code like db call, rest call, we are using setTime out for demo
  setTimeOut(()=>{console.log("setTimeout")}, 1000);  // takes a function as first arguments
});

return promise;
};
```

#### How to invoke the function that returns promise, handling with `then` method
```js 
function myAsyncFunction(){

  let promise = new Promise( (resolve,reject)=> {   
     let error = true;
     setTimeOut(()=>{console.log("setTimeout");
       if(error){ // if any error happens
         reject(); // some function 
       }else{
         resolve();
       }
        }, 1000);
   });
  return promise;
};

myAsyncFunction().then( ()=> console.log("Task complete")); //waiting for 1 sec display msg

myAsyncFunction().then( (resp,error)=> console.log("Task complete")); //using resolve, reject when calling

myAsyncFunction().then(
  ()=>console.log("successfully Completed"), 
  ()=>console.log("Error occurred")
); // by defining the second function automatically the error is handled. 

// output : Error occurred  - since the promise error flag is set to true;
```

- Note: we can pass parameters to the promise reject() and resolve () function. which will be uses as parameters when handling with then.

```js 
function myAsyncFunction(){

  let promise = new Promise( (resolve,reject)=> {   
     let error = true;
     setTimeOut(()=>{console.log("setTimeout");
       if(error){ 
         reject("Error"); 
       }else{
         resolve("Done");
       }
        }, 1000);
   });
  return promise;
};

myAsyncFunction().then(
  (success)=>console.log("successfully Completed -" + success), 
  (error)=>console.log("Error occurred -"+error)
); // by defining the second function automatically the error is handled. 

// output : Error occurred -Error - since the promise error flag is set to true;
```

#### Cleaner way to handle catching error using `catch`
```js 
function myAsyncFunction(){

  let promise = new Promise( (resolve,reject)=> {   
     let error = true;
     setTimeOut(()=>{console.log("setTimeout");
       if(error){ 
         reject("Error");
       }else{
         resolve("Done");
       }
        }, 1000);
   });
  return promise;
};

myAsyncFunction()
  .then((success)=>console.log(success))
  .catch((error) => console.log(error)); 

// output : Error occurred  - since the promise error flag is set to true;
```

### Modules
  - priort to ES6, there are no direct way to create modules, developers where using SystemJS to perform it.
  - ES6 supports modules. (Not all browser supported yet)
  - There might be CORS error.
  - we can use export function to make the access it when declaring model
  - import to use the exported function
