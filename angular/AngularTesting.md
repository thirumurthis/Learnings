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
   require('karam-firefix-launcher'),
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
 - 
