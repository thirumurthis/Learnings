### Shallow Copy Vs Deep Copy:

 - Shallow copy : `pass by reference`, the reference of the other object is assinged to the another variable.
 - Deep copy : `pass by value`, the complete object is copied to an new instance.


Note:
  - In javascript all the primitive type variable is immutable.
  - non-Primitive type like array, Object are mutable.

#### Shallow copy:
 ```js
 const temp = 10;
 const sCopy = temp;
 
 console.log(temp); // prints 10
 console.log(sCopy); // prints 10
 
 sCopy = 19;
 console.log(temp); // prints 19  //since the reference is copied the value also gets updated.
 console.log(sCopy); // prints 19  

let student1 = {
fName : "fn",
lName : "ln"
}

let student2 = student1;

console.log(student1); //{ fName:"fn" , lName:"ln"}
console.log(student2); //{ fName:"fn" , lName:"ln"}

student2.fName="fn2";
console.log(student1); //{ fName:"fn2" , lName:"ln"}  // both the reference are updated
console.log(student2); //{ fName:"fn2" , lName:"ln"}

 ```
 
#### Deep Copy:
There are different ways to achieve this, but each has little side effect.
  - using `JSON.stringify()`& `JSON.parse()`=> Limitation: Any function in the object will not be cloned to the new instance of the object.
  - using `Object.assign()` => Limitation: After cloning, chaning the value of the nested object will update the original object nested object as well.
  - using sperator operator `...` => Limitation : we need to perform code in order to copy the nested object with another sperator operator.

#### Using `JSON.parse()` and `JSON.Strignify()` to clone
```
let student1 = {
fName : "fn",
lName : "ln"
}

let student2 = JSON.parse(JSON.stringify(student1));

console.log(student1); //{ fName:"fn" , lName:"ln"}
console.log(student2); //{ fName:"fn" , lName:"ln"}

student2.fName="fn2";
console.log(student1); //{ fName:"fn" , lName:"ln"}
console.log(student2); //{ fName:"fn2" , lName:"ln"} //this works for simple object, but if there is a function it won't be able to copied
```
 - With function, the json.parse will not be able to clone the `function`

```js
let student1 = {
fName : "fn",
lName : "ln",
  getName: function (){
     return `$this.fn , $this.ln`;
   }
}

//let student2 = student1;
let student2 = JSON.parse(JSON.stringify(student1));

console.log(student1); // { fName: "fn", getName: function (){ return `$this.fn , $this.ln`; },  lName: "ln"}
console.log(student2); // { fName:"fn" , lName:"ln"}

student2.fName="fn2";
console.log(student1); //{ fName: "fn", getName: function (){ return `$this.fn , $this.ln`; },  lName: "ln"}
console.log(student2); //{ fName:"fn2" , lName:"ln"}
```

#### using `Object.assign()` to clone the object
```js
let student1 = {
fName : "fn",
lName : "ln",
  getName: function (){
     return `$this.fn , $this.ln`;
   }
}

//using Object.assing (<empty object>, originalObject)
let student2 = Object.assign({},student1); //copy student1 values or clone to student2

console.log(student1); //prints { fName: "fn", getName: function (){   return `$this.fn , $this.ln`;  },  lName: "ln"}
console.log(student2); //prints { fName: "fn", getName: function (){   return `$this.fn , $this.ln`;  },  lName: "ln"}

student2.fName="fn2";
console.log(student1); //prints { fName: "fn", getName: function (){   return `$this.fn , $this.ln`;  },  lName: "ln"}
console.log(student2); //prints { fName: "fn2", getName: function (){   return `$this.fn , $this.ln`;  },  lName: "ln"} //fn2 updated
```
- Demo: where the where updating the nested object value in the cloned instance also update the original object nested object as well
```js
let student1 = {
fName : "fn",
lName : "ln",
details : {
  course : "Science",
  year : 2020
  } 
}

//let student2 = student1;
//let student2 = JSON.parse(JSON.stringify(student1));
let student2 = Object.assign({},student1);

console.log(student1); // prints { details: { course: "Science", year: 2020 }, fName: "fn", lName: "ln" }
console.log(student2); // prints { details: { course: "Science", year: 2020 }, fName: "fn", lName: "ln" }

student2.fName="fn2";
student2.details.course="English";
console.log(student1); // prints { details: { course: "English", year: 2020 }, fName: "fn", lName: "ln" }  // Note the details ojbect updated 
console.log(student2); // prints { details: { course: "English", year: 2020 }, fName: "fn2", lName: "ln" } // nested object details updated and fn2 also updated 
```

#### Using seprator operators to clone object.
- with just the sperator object, we see the same limitation like the `Object.assign()` where the nested object values gets updated.

```js
let student1 = {
fName : "fn",
lName : "ln",
details : {
  course : "Science",
  year : 2020
  } ,
  getName:function (){
  return `$this.fn,$this.ln`;
  }
}

//using sperator operator
let student2 = {...student1}

console.log(student1); // prints { details: {course: "Science", year: 2020  }, fName: "fn", getName: function (){return `$this.fn,$this.ln`; },  lName: "ln"}
console.log(student2); // prints { details: {course: "Science", year: 2020  }, fName: "fn", getName: function (){return `$this.fn,$this.ln`; },  lName: "ln"}

student2.fName="fn2";
student2.details.course="English";
// below prints the updated nested object value as updated to English
console.log(student1); // prints { details: {course: "English", year: 2020  }, fName: "fn", getName: function (){return `$this.fn,$this.ln`; },  lName: "ln"}
console.log(student2); // prints { details: {course: "English", year: 2020  }, fName: "fn2", getName: function (){return `$this.fn,$this.ln`; },  lName: "ln"}
```
##### Complete demonstration of deep copy
```js

let student1 = {
fName : "fn",
lName : "ln",
details : {
  course : "Science",
  year : 2020
  } ,
  getName:function (){
  return `$this.fn,$this.ln`;
  }
}

let student2 = {
  ...student1,
  fName:"f2",
  details:{
  ...student1.details,
  course:'English'
  }
}

console.log(student1); //prints {details: {course: "Science", year: 2020}, fName: "fn", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}
console.log(student2); //prints {details: {course: "Science", year: 2020}, fName: "fn", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}

student2.fName="fn2";
student2.details.course="English";
console.log(student1); //prints {details: {course: "Science", year: 2020}, fName: "fn", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}

// only this instance is updated with the required values. English and fn2.
console.log(student2); //prints {details: {course: "English", year: 2020}, fName: "fn2", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}
```

Note: 
 - There are other javascript library which provide functions to create deepCopy like, underscore.js and lodash

Other ways are 
 - To iterate the object keys and copy
```js


let student1 = {
fName : "fn",
lName : "ln",
details : {
  course : "Science",
  year : 2020
  } ,
  getName:function (){
  return `$this.fn,$this.ln`;
  }
}
let student2 = {}; // the new empty object

// Copy all user properties into it
for (let key in student1) {
  if (student1.hasOwnProperty(key)) {
  student2[key] = student1[key];
 }
}
//update the 
student2.fName = "fn2"; // changed the data 
student2.details.course="English";

//Same limitation as the assign(), where nested object value is also updated
console.log(student1); // prints {details: {course: "English", year: 2020}, fName: "fn", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}
console.log(student2); // prints {details: {course: "English", year: 2020}, fName: "fn2", getName: function (){ return `$this.fn,$this.ln`;}, lName: "ln"}
```

#### using lodash to clone 
 - with the node.js install `npm install lodash.clone` and `npm install lodash.clonedeep`
```js
const clone = require('lodash.clone')
const cloneDeep = require('lodash.clonedeep')

let student1 = {
fName : "fn",
lName : "ln",
details : {
  course : "Science",
  year : 2020
  } ,
  getName:function (){
  return `$this.fn,$this.ln`;
  }
}

//shallow copy 
const student2 = clone(originalObject)

//deepcopy
const student3 = clonedeep(originalObject)
```
