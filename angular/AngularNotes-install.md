To install angular CLI in Linux Centos 7.2
```
$ sudo yum -y install nodejs
```

verify the installation of node and npm for version:
```
$ node -v 
 >>> V6.17.1
$ npm -v
 >>> 3.10.10
```
 
 for  downloading angular cli
 
 ```
 $ npm install -g @angular/cli
    -g for global
    in case of exception use `sudo npm`
 ```

Exception message
```
/usr/lib/node_modules/@angular/cli/bin/postinstall/analytics-prompt.js:8
(async () => {
       ^

SyntaxError: Unexpected token (
    at createScript (vm.js:56:10)
    at Object.runInThisContext (vm.js:97:10)
    at Module._compile (module.js:549:28)
    at Object.Module._extensions..js (module.js:586:10)
    at Module.load (module.js:494:32)
    at tryModuleLoad (module.js:453:12)
    at Function.Module._load (module.js:445:3)
    at Module.require (module.js:504:17)
    at require (internal/module.js:20:19)
    at Object.<anonymous> (/usr/lib/node_modules/@angular/cli/bin/postinstall/script.js:5:1)
npm ERR! Linux 3.10.0-514.26.2.el7.x86_64
npm ERR! argv "/usr/bin/node" "/bin/npm" "install" "-g" "@angular/cli"
npm ERR! node v6.17.1
npm ERR! npm  v3.10.10
npm ERR! code ELIFECYCLE

npm ERR! @angular/cli@8.0.4 postinstall: `node ./bin/postinstall/script.js`
npm ERR! Exit status 1

```
The above exception was due to the mismatch of npm and nodejs version.

Used [Link](https://tecadmin.net/install-latest-nodejs-and-npm-on-centos/) to install the latest npm & nodejs.
- Ensure to install the c++ make updates

During ng serve command if encountered below message

### One reason for the missing module was due to unresolved dependencies in the workspace folder of angular project, when i cloned code from git which didn't include the node_modules folder within the project which has the dependencies mentioned in the package.json.
### After navigating to the project level folder and usign the `$ npm install` command resolved the issue. 

 - The `npm install` should resolve the dependencies. 

Issue 1:
     Could not find module “@angular-devkit/build-angular”, can be fixed with the below command. (use sudo in case of permission issue, during installation) 
        ```
        npm install --save-dev @angular-devkit/build-angular
         >  -g for global install
        ```
Issue 2:
     Another exception when using `npm start` was "An unhandled exception occurred: Cannot find module '@angular/compiler-cli'".
 
 Both issues 1 & 2 where resolved after usign `npm install`


### Uninstall angluar

```
$ npm uninstall -g @angular/cli
$ npm cache clean --force
```
### Re-install angluar latest
```
$ npm install -g @angular/cli
```
