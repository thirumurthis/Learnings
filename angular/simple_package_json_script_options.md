```
package.json

"scripts":
{
    "start:mock":"ng server --configuraton=mock",
    "build:dev" : "ng build --configuration=development --output-path=dist",
    "akita" : "akita"
    "test:ci" : "ng test --code-coverage -browsers=CustomChromeHeadLess --watch=false",
    "lint" : "ng lint",
    "pree2e" : "webdriver-manager update --versions.chrome=x.x.x.x --standalone=false --gecko=false",
    "e2e" : "ng 323 --webdriverUpdate=false",
    "e2e-compile": "tsc -p e2e",
    "test" : "ng test --browsers=Chorme --watch=false",
    "start:e2e" : "npm run preere && npm run start --confgiruatoon=e2e-local",
    "build:prod" : "ng build --configuration=production --output-path=dist"
},
```
