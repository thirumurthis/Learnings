 - `fetch ()` takes and url as an argument
 - `fetch ()` returns a promise. Promise in javascript is the way to handle asyncronus event.

Reference: [Link](https://github.com/CodingTrain/Intro-to-Data-APIs-JS); [1](https://www.youtube.com/watch?v=tc8DU14qX6I)

Per the reference, 
 - An image is fetched and converted to blob and displayed in the image tag.
 - The blob itself is a raw type, so use of createObjectURL() is used passing the blob.
 ```html
  <html>
      <head>
          <meta charset="UTF-8" />
          <title> Simple app </title>
      </head>
      <body>
          <img src="" id="image1"/>
          <script>
              fetch("image1.jpg").then(response => {
                console.log(response);
                return response.blob();
              }).then(obj => {
               console.log(obj);
               document.getElementById('image1').src = URL.createObjectURL(blob);
              })
              .catch( error => {
                 console.log("error!!");
                 console.error(error);
              });
          </script>
       </body>
   </html>
 ```
 
  - The `.then` on the fetch method can be rewritten using `async/ await`. `async` and `await` are new features in javascript.
  - The `await` can be only used within the context of `async` keyword.
  
Below is the similar to above code using async and await:
```html
  <html>
      <head>
          <meta charset="UTF-8" />
          <title> Simple app </title>
      </head>
      <body>
          <img src="" id="image1"/>
          <script>
              console.log('render image');
              // invoke the function getImage() method, so the image will be rendered. 
              // in case of any error, then that also needs to be caught.
              getImage().catch( error => {
                 console.log("error!!");
                 console.error(error);
              }); 
              
               async function getImage(){
                 const response = await fetch('image1.jpg');   // since we have used the async function, the fetch has to await for the promise which is the response
                 const blob = await response.blob();
                 document.getElementById('image1').src = URL.createObjectURL(blob);
              
              }
          </script>
      </body>
</html>
```
- In order to read the csv file with comma seperated data in java script using fetch command.
 - use same approach ` response from the fetch, can be converted to text using response.text()` in the above the response was converted to bolb
 - once the text is converted then manipulate using split, slice, command to extract data.
 
 ```js
   const data = await response.text();
 ```
 
#### Javascript destructuring
 - Say there is an json object, we need only few properties, we can perform below.
 
```
const temp = { idx: 1, movie: "hero1", actor: "actor1"}; 
const {movie,actor} = temp;
console.log(movie); //hero1
## Note: in console once the variable is declared with const, then it cannot be re-declared with const again.

let tmp = { idx: 1, game: "cricket", players: "11"}; 
let   {game,players} = tmp; console.log(game+" && "+players);
```
  
#### Javascript `var` is not used in newer version of javascript, `const` or `let` newer javascript version.
```
```
Sample example to using `fetch` function:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Fetch with Basic Access Authentication</title>
    <meta name="viewport" content="width=device-width"
    <style>
        p{
            cursor: pointer;
        }
    </style>
</head>
<body>
    <header>
        <h1>Fetch with Basic Access Authentication</h1>
    </header>
    <main>
        <p>Response will appear here after you click.</p>
    </main>
    <script>
        let p;
        
        document.addEventListener('DOMContentLoaded', 
            function(){
                p = document.querySelector('main>p');
                p.addEventListener('click', doFetch);
            });
        
        function doFetch(ev){
            let uri = "https://localhost/serve/sample.json"; <!-- Actual backend serving json -->
            
            let h = new Headers();
            h.append('Accept', 'application/json');
            let encoded = window.btoa('user1:pass1'); <!-- backend setup with this username and password as user1 and pass1 -->
            let auth = 'Basic ' + encoded;
            h.append('Authorization', auth );
            console.log( auth );
            
            let req = new Request(uri, {
                method: 'GET',
                headers: h,
                credentials: 'include'  
            });
            //credentials: 'same-origin' <!-- only applicable with the same domain -->
            //credentials: 'include' <!-- also include the cookies though different domain -->
            
            fetch(req)
            .then( (response)=>{
                if(response.ok){
                    return response.json();
                }else{
                    throw new Error('BAD HTTP stuff');
                }
            })
            .then( (jsonData) =>{
                console.log(jsonData);
                p.textContent = JSON.stringify(jsonData, null, 4);
            })
            .catch( (err) =>{
                console.log('ERROR:', err.message);
            });
        }
        
        /********************************
        Server can send headers
        WWW-Authenticate: Basic realm="Realm Description" charset="UTF-8"
        HTTP/1.1: 401 Unauthorized
        HTTP/1.1: 403 Forbidden
        
        Client sends header
        Authorization: Basic QWxhZGRpbjpPcGVuU2VzYW1l
        The string is username:password base-64 encoded  <-- since base64 is easily decoded using javascript atob() function
        MUST BE OVER HTTPS
        ********************************/
    </script>
</body>
</html>
```
