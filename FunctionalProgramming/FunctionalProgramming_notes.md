#### What is higher order functon in functional programming?
A higher order function is simply a function which can either accept another functon as parameter or return another function as a parameter.

```js
function print(){
   console.log("print message");
}

function func1(callback){
   callback();
}

func1(print); // this will be as callback.
```
##### Tips: Use of _ in javascript lambda
```js
firstUnique = (a) => {
    let item = a.pop(); 
       if(a.indexOf(item) != -1) { 
           a = a.filter(_ => _ != item);  // use of _ is passing a 1 argument;
                                           // _ usage is considered to be inconsistent with other lambda code, better usage is (a) =>
                                           // _ means it can be a single argument lambda and either not used in the function
            return firstUnique(a); 
            } 
       return item; 
     };
// in Go and Drat _ is used to indicate the parameter is ignored
```
Reference [link](https://stackoverflow.com/questions/41085189/using-underscore-variable-with-arrow-functions-in-es6-typescript)
