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

