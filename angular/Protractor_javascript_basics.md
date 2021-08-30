
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
