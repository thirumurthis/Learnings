
- In few cases, protractor returns promise. 10% of the time.
`getText()` method is one where the promise is not handled by the Protractor API itself.

```js
describe('simple test11', ()=>{

    it('test calculate',()=>{
        browser.get("http://juliemr.github.io/protractor-demo/");
        element(by.model("first")).sendKeys(1);
        element(by.model("second")).sendKeys(2);
        element(by.id("gobutton")).click();
        
        //using old conventional way to get the text object and print
        element(by.binding('latest')).getText().then(function(text){
            console.log(text);
        });        
        // using lambda approach
        element(by.binding('latest')).getText().then((text)=>{
            console.log(text);
        });        
       expect(element(by.css("h2[class='ng-binding']")).getText()).toEqual('3');
    });
})
```

- In order to run this in local, install protractor using `npm i -g protractor`
- Since we are using the protractor demo site, we can crate a config file as below

```js
// name as config.js  - note no need for local webmanger driver will be started by default.
// recommended: to start the webmanger sever externally using  > webdirver-manager start 
exports.config = {
    seleniumAddress: 'http://localhost:4444/wd/hub',
    specs: ['spec1.js']
  };
```
- issue below command to run the test
```
// passing the config file, make sure the config file exists in the path where the command is executed. .js extension is options.
$ protractod config 
```
- Info: to update the IE driver ` > webdriver-manager update --ie`

- To specify the browser, update the capabilities section on the configuration file, check docs.

**Important**: To working with non-Angular, check the documentation `browser.waitForAngularEnabled(false);`

##### How to use repeater, chain locators
 - If the angular app, inspect element uses ng-repeator, then we can use repeaters

```js
describe('simple test11', ()=>{

    it('test calculate',()=>{
        browser.get("http://juliemr.github.io/protractor-demo/");
        element(by.model("first")).sendKeys(1);
        element(by.model("second")).sendKeys(2);
        element(by.id("gobutton")).click();
        //chaining element over another element, the scope of the css will be within that element 
        // not the result in memory ng-repeat is table with two tds, and we need to get the text and value of the second child.
        element(by.repeater("result in memory").element(by.css("td:nth-child(2)").getText(); // the result : 3+5 promise is returned
        
        //to resolve the promise, use .then()
        element(by.repeater("result in memory").element(by.css("td:nth-child(3)").getText().then((test)=>{
        console.log(test);
        })
        });
```
##### using `all` locators
 - the link  `http://juliemr.github.io/protractor-demo/`, prints the results in a table appended to the result section
 - How to grab more than one results and validate?

```js
it('all test',()=>{
            browser.get("http://juliemr.github.io/protractor-demo/");
            element(by.model("first")).sendKeys(1);
            element(by.model("operator")).element(by.css("option:nth-child(4)")).click();
            element(by.model("second")).sendKeys(2);
            element(by.id("gobutton")).click();

            element(by.model("first")).sendKeys(2);
            element(by.model("second")).sendKeys(2);
            element(by.id("gobutton")).click();
            

            // this will print the count where count send the promise which needs to be handled.
            element.all(by.repeater('result in memory')).count().then((cnt)=>{
                 console.log("Count: "+cnt);
            });
             //in order to loop through the repeates and pring the results of the 3rd td 
             element.all(by.repeater('result in memory')).each(function(item){
                item.element(by.css("td:nth-child(3)")).getText().then((text)=>{
                console.log("product : "+text);
              });
            
            });
  
```
