
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
- Tips: using `npm init` will creat e a `pacakge.json` file, unde rthe scripts attribute adding the command for execution will make exeuction easier
        For example, `scripts [ "start" : "protractor config.js", ..]` thne using `$ npm start` will execute the protractor command.
        
- issue below command to run the test
```
// passing the config file, make sure the config file exists in the path where the command is executed. .js extension is options.
$ protractor config 
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
#### how to use `tagName` locator?
```
 element.all(by.tagName("option")).each((item)=>{
                  item.getAttribute("value").then(function(val){
                    console.log("option: "+val);
                  })
              });
```

##### Handling auto-suggest/auto-complete text box.

 - From Protractor API, we can use `action`
 - Moving cursor can be moved, using action.
 
```js
// in angular app if we have a dropdown, we can select text box and the dropdown list using actions

//for selection of auto-suggest box and inputing chars
browser.actions().mouseMove(
      element(by.model("locationQuery")
    ).sendKeys("lond")).perform();

//for selecting the dropdown value suggested after passing lond
browser.actions.sendKeys(protractor.Key.ARROW_DOWN).perform();

//for entring the key or selecting from dropdown list
browser.actions.sendKeys(protractor.Key.ENTER).perform();

//to slow down the browser usign sleep
browser.actions.sendKeys(protractor.Key.ENTER).perform().then (()=>{
  browser.sleep(5000);//to sleep 5 sec
});
```

- Using regular expressiong in `by.css()`
```
//selecting the tile of serach and clikcing the specific href link
element(by.css("a[ng-href*='Island']").click();

element(by.css("a[ng-href*='Island']").click().then(()=>{
  browser.sleep(3000);
});
```

##### Handling the click opens another window, rather then within that window.
   - Protractor, will perform the serach only within the parent window.
   - Any child window search, the protractor should instructed to focus to the child window.

 - This can be done using `switchTo()`.

```
browser.switchTo().window(nameOrhandle); // how get the handle of the child window.
// protractor has an API

browser.getAllWindowsHandles(); //this gives the handle of all the opned window/tab and returled in the array. 
// the handle promise needs to be handled.
browser.getAllWindowsHandles().then(function(handle){
 // Node: handle[0] - this has the parent window handle.
 // in this case handle[1] - has the child window (since this is opned per protractor test)
  browser.switchTo().window(handle[1]);
})
```
- Using the above child winodow handle, from child window.
```js
browser.getAllWindowsHandles().then(function(handle){
  browser.getTitle().then((title)=>{console.log("parent : "+title);}
  browser.switchTo().window(handle[1]);
  browser.getTitle().then((title)=>{console.log("child: "+title);} // returns the child window title since after switch
})
```

##### How to handle `alerts` in protractor.
- Clicking the option the clicking button when alerts open.
- This is mostly for non angluar app or hybrid apps. 
```js
it('test alert',()=>{
     browser.waitForAngularEnabled(false);
     browser.get("https://www.rahulshettyacademy.com/AutomationPractice/");
      element(by.id('confirmbtn')).click();
      // accept returns a promise and we are handling it here.
       browser.switchTo().alert().accept().then(()=>{   
         browser.sleep(1000);
       });
      //  browser.switchTo().alert().accept(); // This will create alerts and click ok which is positve
        element(by.id('confirmbtn')).click();
        browser.switchTo().alert().dismiss().then(()=>{
            browser.sleep(4000); //instead of accept use dismiss
        });
    });
```
#### How to handle frames. (iframe).
  - Embedding another site within a site. 
  - use `switchTo().frame()` in this case, below is code example. refer protractor API for more info.
```js
  it("test frame", ()=>{
        browser.waitForAngularEnabled(false);
        // as a best practice maximize the browser when you perform testing.
        // there are no protractor also uses the selinium code for this.
        browser.driver.manage().window().maximize(); // in selinium simply use driver.manage().window().maximize();
        browser.get("https://www.rahulshettyacademy.com/AutomationPractice/");

        browser.switchTo().frame(element(by.tagName('iframe')).getWebElement());
        element(by.css("a[href*='sign_in']")).getText().then((txt)=>{
            console.log("iframe title : "+txt);
        })
    });
```
#### Synchronization in non-angular app.
 - Protractor works with angular to handle waits and synchronization.
 - In non-angular app, clicking an button in some case might take long time to render.
   - That is where we need to synchronize the calls.
   - Don't use browser.sleep(), we never know how long to wait. 

For synchronization and sleep required for non-angular app use expected condiction
  - __Protractor uses concept called `expected condition`, where it has timeout.__
 - Check protractor API documentation for expectedconditions

```
## declare the variable EC with expected condition
var EC = protractor.ExpectedConditions;
// below waits for id abc to be no longer visible on the dom
browser.wait(EC.invisibilityOf($('#abc')),5000);
```
- sample code
```js
 it('test synchronization',()=>{

        //for non-angular app
        browser.waitForAngularEnabled(false);

       /*
        //without synchronization 
        browser.get('https://www.itgeared.com/demo/1506-ajax-loading.html');
        //when clicking the link, it takes sometime to load the string
        element(by.css("a[href*='loadAjax']")).click();
        //printing the text after clicking which takes time.
        element(by.id("results")).getText((text)=>{
            console.log(text);
        });
        */

        var EC = protractor.ExpectedConditions;
        browser.get('https://www.itgeared.com/demo/1506-ajax-loading.html');
        //when clicking the link, it takes sometime to load the string
        element(by.css("a[href*='loadAjax']")).click();

         // wait till 5 sec, if response recived before 5 sec perform next step
        browser.wait(EC.invisibilityOf(element(by.id("loader"))),10000);
        element(by.id("results")).getText().then((text)=>{
            console.log(text);
        });
    });
````

##### `Page Object` and `jasmin data provider`
- Page Object 
    - If there is a simple javascript object, it can be used in another javascript file by using `module.exports` and using `requires(file-name.js)`.
    - In the above, comparing java it is like importing the class from different package.

```
// file1.js
var Car = { "make" : "BMW", "model" : 2020 };

module.export = new Car();

//file2.js

var car = requires('file1.js);

console.log(car.make); // prints BMW

//NOTE: using node.js above program can be executed as node file2.js
```
  - The above underling concept can be used for protractor as well, creating a java script object, and using requires to import those into test class.
  - by conventions, the page object files are named as `*.po.ts`
  
- Data provider
   -  similar to Page object, the using jasmine data provider npm plugin, we can extract the data out of the test class as well.


