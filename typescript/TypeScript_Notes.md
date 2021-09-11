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
 // backticks also support multi liner
 console.log(`The lucky number of ${user.name} 
              will alway be ${user.luckynumber}`);
              
 // Arrays in the backticks are stringified, so prints as comma sperated values.
 let numToAdd : Array<number> = [2,4,6];
 console.log(`print - ${numToAdd}`);
 console.log(`print -`, numToAdd); // if needed to print the array itslef use conventional way
 // printing class, will print the object as such, not the properties.
 console.log(`print object`,user);
```
#### Destructure operator `{}` and `[]`

- destructure from object
```ts
let car = {make: "BMW", model: "300 series", year : 2009};

console.log("car :"+car);

//destructure object
let {model,year} = car;
console.log("this car model "+model);
console.log("this car year "+year);
// using alias
let {model: bmwmodel} = car;
console.log(bmwmodel);

//Array destructure

let fruits: Array<string> = ["apple","orange","grapes","banana"];
let veggies: string[] = ["carrot","beetroot","beans"];
let edible : Array<string> = [...fruits,...veggies]; //combines both the array
console.log(`${edible}`); //prints combined array info

//destruct array
let [apple,grapes1] = edible;  // if the item name is not matching form that array uses index
console.log(apple+" , "+grapes1); //prints apple,orange

let[orange, banana] = edible;
console.log(`${orange} and ${banana}`); //prints orange and banana
```

##### REST Parameter - like a vargs in java, helps provide indefinte number of arguments as an array
  - should be prefixed by `...` and this is applied to the last parameter 

```ts
function calc(operation?:string , ...args:number[] ){
    let sum :number = 0;
    for(let i=0; i<args.length; i++){
       sum = +sum + +args[i]; // add +before number to treat it as unary operator
    }
    return sum;
}

console.log("calc = "+ calc("add",1,2,3,4,5,6)); //21
-----

let fruits: Array<string> = ["apple","orange","grapes","banana"];
let veggies: string[] = ["carrot","beetroot","beans"];
let edible : Array<string> = [...fruits,...veggies]; //combines both the array
console.log(`${edible}`); //prints combined array info
let [apple, ...restEdible] = edible;
console.log(apple+" , "+restEdible);

function simple (apple?:string, ...edibles){
    console.log(restEdible);
}

simple(...restEdible)
```

##### Class in typescript
 - similar to the class in java.
```ts
//simple class declaration and usage in TS
class User{

    firstName: string;
    lastName: string;
    age: number;

    constructor(firstName:string,lastName: string, age: number){
        this.firstName =firstName;
        this.lastName =lastName;
    }

    getFullName():string{
        return `${this.firstName} ${this.lastName}`;
    }
}

//creating object
let object : User= new User("fname","lname",10); // since the constructor is defined we need to pass args
console.log(object.getFullName());
```
 - Shorter syntax, directly declaring the properties of class in the constructor and no need to declare it as class property

```ts
class User{

    //placing the properties on the constructor itself
    //since the passed values are hold by the constructor
    // we need not explicitly the properties in the class
    constructor(
         private firstName:string,
         private lastName: string, 
         private age: number){
    }

    getFullName():string{
        return `${this.firstName} ${this.lastName}`;
    }
}

//creating object
let object : User= new User("fname","lname",10); // since the constructor is defined we need to pass args
console.log(object.getFullName());
```
- inheritance and overriding
```ts
class User{

    //placing the properties on the constructor itself
    //since the passed values are hold by the constructor
    // we need not explicitly the properties in the class
    constructor(
         private firstName:string,
         private lastName: string, 
         private age: number){
    }

    getFullName():string{
        return `${this.firstName} ${this.lastName}`;
    }
}

//creating object
let object : User= new User("fname","lname",10); // since the constructor is defined we need to pass args
console.log(object.getFullName());

//in case if we need to call the user class to be used within the Department class
// inheriting helps 
class Department extends User{  // extending the constructor of the user class needs to be invoked

  constructor(
      private departmentName :string,
      private usrCnt: number,
      firstName:string,   //removing the private will not create a property here in this class
      lastName: string, // adding private will create error
      age: number
    ){
     super(firstName,lastName,age); // calls the constructor of user class and had to pass the properties of class else report error
    //super has to called in the constructor or other function
  }

  getFullName():string{
      // this is one way to get the full name and manipulate 
      const fullName: Array<string> = super.getFullName().split(' ');
      fullName.splice(1,0,this.departmentName);
      const newName = fullName.join();
      return `${newName}`;
  }
}

//instatiating or creating department ojbect
const worker:Department = new Department("storage",1,`john`,`doe`,0);
console.log(worker.getFullName());
```
- We are using `private` access specifier, other two types are `public` and `protected`.
  - using `protected`, we might not to do like the above approach. so now the override method can be made simple
```ts
class User{

    //placing the properties on the constructor itself
    //since the passed values are hold by the constructor
    // we need not explicitly the properties in the class
    constructor(
        protected lastName: string, 
        protected firstName:string,
        protected age: number){
    }

    getFullName():string{
        return `${this.firstName} ${this.lastName}`;
    }
}

//creating object
let object : User= new User("fname","lname",10); // since the constructor is defined we need to pass args
console.log(object.getFullName());

//in case if we need to call the user class to be used within the Department class
// inheriting helps 
class Department extends User{  // extending the constructor of the user class needs to be invoked

  constructor(
      private departmentName :string,
      private usrCnt: number,
      firstName:string,   //removing the private will not create a property here in this class
      lastName: string, // adding private will create error
      age: number
    ){
     super(firstName,lastName,age); // calls the constructor of user class and had to pass the properties of class else report error
    //super has to called in the constructor or other function
  }

  getFullName():string{
      return `${this.firstName}${this.departmentName}${this.lastName}`;
  }
}

//instatiating or creating department ojbect
const worker:Department = new Department("storage",1,`john`,`doe`,0);
console.log(worker.getFullName());
```
- We can use `getter` and `setter` methods. But the parent class can't access the getter and setter if it is present in child class in javascript
