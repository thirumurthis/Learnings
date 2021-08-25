#### unit testing
 - Each unit test file will be ending with `.spec.ts`
 - use `ng test` to run test cases
     - This will first compile the application
     - Open browser to run the tests
     - If the test is set to run part of pipelin, then can be configured `headless` execution.
  - The tests written in `Jasmine` and are executed using `karma` runner.

Basic syntax:
  ```
  describe('appComponent', () => {
     it('should write to console', () => {
             consle.log(100);
     });
     it('shoule equal to 100', () => {expect(100).toBe(100)});
  })
  ```
##### configuring karma to run on browser or as headless
 ```
 ng test 
 command can run with below options as well
 
 --browsers=browser
 --code-coverage
 --karmaConfig=karmaConfig
 --main=path to main spec.ts file
 --configuration=configuration
 -progress=true|false
 ```
- `karma.config.js` is the config file which can be updated

```
## karma.config.js file

config.set({...
plugins: [
   require('karma-jasmine'),
   ...
   require('karma-chrome-launcher'),
   require('karam-firefox-launcher'),
   ...
   ]
  ...
  port: 9876,
  ...
  browsers: ['Chrome']  //add firefox -> but the plugins needs to be added
  ...
  
```
- Angular uses `Protractor` for running the end to end tests
- `ng e2e` is used to run the end to end e2e run.
- A report will be generated at the end of execution.
- All the e2e test cases are located in e2e folder
- `*.e2e.spec.ts` -> contains e2e config test info under e2e folder

`ng e2e` can be passed with arguments
```
--browsers=browser
--baseUrl=baseUrl
--specs
--host=host
--port=port
--prod=true|false
--main=main
--suite=suite
```
 - The configuration is present under `protactor.conf.js` config file.
----------------

#### code coverage during testing
```
$ ng test --code-coverage=true
```
Another approach is to update the angular.json, test properties with `codeCoverage`:`true`.

When set in pipeline, better to use the `ng test --code-coverage=true` in CLI itself.

-----

#### how to skip the testing execution
  - Angular has supports for skipping testing E2E tests
  - Ways to skipe tests:
     - 1. for `new` applicaton
     - 2. for `exiting` application
     - 3. for `existing test` scripts
     
- 1. To skip the test use --skip-test option in CLI when creating 
```
$ ng new app-name --skip-test
```
- 2. In `angular.json`, update/add. NOTE: This is not recommended.
```
projects : {
...
"projectType: : "application",
   "schematics/angular:component": { "skipTests" : true }
 ...  
```
- 3. Adding `x` in front of the tests case like `describe` and `it`
    - x prefixed means the test case will be suppressed.
```
 xdescribe(...
 xit(..
```
-------

#### basic concepts of testing (Jasmine is BDD (bhaviour driven development))

- `Describe` - test suite beings with a call to the global Jasmine function describe with two parameter. string and function.
```
    describe("test the app component", function (){ //callback
    
    // we write it function (this is called specs) - smallest unit test case.
    });
```
- `Specs` - these are defined by calling the global Jasmine function `it`, which, like descirbe takes a string and a function.
```
 it("check app title component", function(){//callback function });
 // there can be multiple it function.
```
- `Expect` - Expectations are built with the function excpect which takes a value, called the actual. Expect 
```
var actual =100;
expect ("actual").toBe(100);  // toBe part is the matcher.
//toBeTruthy(), etc.
```
- `Matcher` - It is chained with a matcher function, which takes expected value.

##### First test case
```js
// string can be any value
describe("Hello test", () => {
   it("First test case",() => { 
      console.log("inside firs test case");
   });
});
```
- When adding `f` (focused) to describe that is the only test that will be executed
```
fdescribe("Hello test", () => {
   it("First test case",() => { 
      //console.log("inside firs test case");  // this will report spec has exptectiatio
      expect(10).toBe(10); // this will execute to pass in ng test.
   });
});
```
 - Now executing `$ ng test`, this fdescribe will be executed.
-------------
### Default generated test files and its details

- `TestBed`,`Async` - Import from built-in modules from core package
- Import the requries Modules 
- Import required Components
- 2 BeforeEach Methods - this comes within the describe function, basic setup is done here.
- TestBed.configureTestingModule({ // this is setting up a module, pass module, components etc.  });
- Fixture - is a wrapper for the components and it's template.

```
1. Imports the rquired classes and interfaces like 
 async, TestBed, ComponentFixture
 
2. Import the components required for testing (at least it should contain the component for which the testing is executed.)

3. describe () function

4. inside describe (), it function

5. inside there is a fixture
   - fixture is a wrapper for component and the template
   - using fixture, we can get the properties of component class and template.
6. BeforeEach method
    - Before running up the test case, the setup needs to be done, this should setup at this method.
    - why 2 beforeEach method.
    
 7. TestBed - main utility to define and test modules
```
- Sample to query css value
```
...
beforeEach(()=> {
fixture = TestBed.createComponent(Appcomponent);
component = fixture.componentInstance;
const queryElement = fixture.debugElement.query(By.css('.brand'))); // to get the properties from template.
fixture.detectChanges();
});
...
```
