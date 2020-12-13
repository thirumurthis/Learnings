Java script, ECMA 6, functional programming functions `map`, `reduce`, `filter`.

//index.html
```html
<!DOCTYPE html>
<html>
    <head>

    </head>
    <body>
        <script src="main.js"></script>
       <h6>ECMA 6 Javasscript</h6>
    </body>
</html>
```

//main.js
```js

const numArray = [12,23,45,23,34,11,00,09,78,66,01,05,83,45,64,49];


const numsGrtThan20 = numArray.filter( num => num > 20);

console.log(numsGrtThan20);

const numMapsqrt = numArray
.filter( num => (num > 10 && num < 50))
.map(n => n*2).map(m => Math.sqrt(m));

console.log(numMapsqrt);

const mapReduce = numArray.filter(n => n < 10).reduce((total,num)=>(total+num),0);
console.log("map reduce : "+mapReduce);

//Without functional approach
const sum = numArray.reduce(function(total,num){
  return total + age;
  },0);
```

For deploying this application in a simple server, use the chrome plugin `200 OK - web server for chorme` plugin. Refer the folder.
The plugin has an option to automatically load index.html file directly.
