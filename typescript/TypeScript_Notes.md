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
 - Spread in array
```ts
 let fruits : Array<string> = ["apple","orange","banana"];
 let cars : string[] = ["BMW","Benz", "Ford"];
 // if we want to combine above two arrays to a single array we can use spread operator
 
 let fruitsAndCars: Array<string> : [...fruits, ...cars]; // will hold ["apple","orange","banana" "BMW","Benz", "Ford"];
 console.log(fruitsAndCars);
```
- Spread in Object
```
let fromPerson1 = { "fromName": "user1"}
let toPerson2 = { "toName" : "user2" }
let department = { ...user1, ...user2, "toAddress": "nowhere" }; // {"fromName" : "user1", "toName" : "user2", "toAddress" : "noWhere" }
```
- Spread in function
```
function add ( a? : number, b?: number, c?: number){ // addind an elvis operator before the make this optional;
  return a + b + c;
}

let numToAdd : Array<number> = [2,4,6];
//conventional way
console.log ( add(numToAdd[0],numToAdd[1],numToAdd[2]);

//using spread operator
console.log (add(...numToAdd)) // will pass the array directly without the ? elvis operator there will be compilation error in Typescript editor 

```
 ##### backticks was created in ES6
  - mostly used for template string

```
 let user = { "name" : "thiru", "lukynumber" : 0}
 //conventional way 
 console.log("The lucky number of "+user.name+" is "+ user.luckynumber); //concatenation is peformed
 
 // using backticks
 console.log(`The lucky number of ${user.name} is ${user.luckynumber}`);
```
