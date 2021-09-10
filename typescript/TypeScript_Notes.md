#### Notes on type script 

Data types:
 - Primitive types
```
var - this has an issue due to compiler hoisting, the var variables will be declared at the start by the compiler (not initialized)
let  - blocked scope, to resolve the issue of var data type
const - blocked scope, but the values should be initialized
boolean  - let testflag : boolean = true;  let testflag : boolean = (()=>true)(); // self calling function
number  - it can be binary, hexadecimal, it can be null, it can be undefined, it can hold another function returning number
string
null  - can only return or store either null or undefined 
undefined - can only retur or store either undefined or null
```
 - Type reference
 ```
 void - data type assinged to function returns nothing
 Array - let fruits : string[] ; fruits = ['apple',`orange`,"banana", (()=>"kiwi")()];
       - let cars: Array<string> ; cars = ["BMW","Benz","Ford"];
       - let multiDataArray : Array<string| number |boolean> ; multiDataArray = ["test",1, true]
  Tuple - similar to Array, but it can be fixed number of data types. let testTuple : [number, string, boolean] ; testTuple = [12,"test",true]
  objects - creating using {}
  enum - enum is same as class, but only can have constant values. enum months{ JAN = "Janauary", FEB = "Feburary"}; let threeCharMonth : months; months.JAN;
  any - can hold any data types
 ```

#### Spread (...) and backtick (\`\`)
 - Spread
```ts
 let fruits : Array<string> = ["apple","orange","banana"];
 let cars : string[] = ["BMW","Benz", "Ford"];
 // if we want to combine above two arrays to a single array we can use spread operator
 
 let fruitsAndCars: Array<string> : [...fruits, ...cars]; // will hold ["apple","orange","banana" "BMW","Benz", "Ford"];
 console.log(fruitsAndCars);
```

  
