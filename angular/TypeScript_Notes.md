#### Data type:
  - number - strict, cannot assign string back to this datatype.
  - boolean - strict
  - string - strict
  - any -> in this data type, any type of data can be assigned at later point of time in the code.

#### Interface contract.

 - A interface is type script is created using interface keyword.
 - Per the interface contract all the field in the interface should be implemented.
 ```js
 interface Customer{
     customerName: string;
     age: number;
     itemNumbers: []
 }
 // how to use the interface 
 let customer: Customer;
 customer: {
  customerName: "Dwanye",
  age: 45,
  itemNumbers: [2,4]
 } 
 ```
 
 - `Optional` fields in interface
 
 ```js
  interface Customer{
     customerName: string;
     age? : number;
     itemNumbers: []
 }
 // how to use the interface 
 let customer: Customer;
 customer: {
  customerName: "Dwanye",
  itemNumbers: [2,4]
 }
// only the customerName, itemNumbers are implemented, still it complies to interface contract.
 ```
