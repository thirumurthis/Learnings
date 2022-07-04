### Webpack server environment for RxJS learning with TypeScript

- In this blog will demonstrate how to setup the Webpack dev server to work with RxJS Reactive programming.
- I have just included a simple Obeservable code of RxJS for demonstration.

- Before starting the below steps, we need to install the `nodejs` and `npm` package manager. Install from by downloading those executables.


#### Install the webpack server environment

- First we will start with setting up the webpack dev environment structure to work on. This requires `webpack-cli` and generators to be installed.
- Create a folder and use below command to install the generators. 

```
npm install -D @webpack-cli/generators
```

- If `npx` is not installed, use `npm i npx`, then issue the below command. 
- This command will prompt questions to create the structure, follow as mentioend in the Output section

```
npx webpack-cli init
```

- Output of the webpack initialize command to choose different options

```
? Which of the following JS solutions do you want to use? Typescript
? Do you want to use webpack-dev-server? Yes
? Do you want to simplify the creation of HTML files for your bundle? Yes
? Do you want to add PWA support? No
? Which of the following CSS solutions do you want to use? CSS only
? Will you be using PostCSS in your project? Yes
? Do you want to extract CSS for every file? Only for Production
? Do you like to install prettier to format generated configuration? Yes
? Pick a package manager: npm
[webpack-cli] ℹ INFO  Initialising project...
 conflict package.json
? Overwrite package.json? overwrite
    force package.json
   create src\index.ts
   create README.md
   create index.html
   create webpack.config.js
   create tsconfig.json
   create postcss.config.js
```

- Now the basic structure is created, we need to install below package. This will update the `package.json` file

```
npm install rxjs typescript ts-loader
````

- In case if ther package.json is missing the webpack dependencies version use below commadn to update it. This is not required in most cases.
```
npm install webpack webpack-dev-server 
```

#### Configure the webpack environment to use `typescript` and `node`

#### Update `tsconfig.json`

- The  `tsconfig.json` file needs to be update to use the typescript node engine and corresponding ES version usage.
- Copy the content, we are using es6 as target. Refer the webpack documentation on updating webpack typescript configuration.

```json
 {
  "compilerOptions": {
    "allowSyntheticDefaultImports": true,
    "noImplicitAny": true,
    "module": "es6",
    "moduleResolution": "node",
    "outDir": "./dist",
    "target": "es6",
    "allowJs": true,
    "lib": [
        "es2017",
        "dom"
      ]
  },
  "ts-node": {
    "compilerOptions": {
      "module": "CommonJS"
    }
  },
  "files": ["src/index.ts"]
}
```

> **Note**
> `"moduleResolution": "node",` -tells that we will use node engine, so import of rxjs is easy in the index.js.

##### Udpate the `package.json` with the start command within the scripts

```
  "scripts": {
    "start": "webpack-dev-server --hot --port 8080 --mode development",
    ...
    },
```

#### Update the `index.html` so RXJS code can update changes

- The Webpack init creates the index.html filw which we will update like below.
- The below changes in index.html, will be used from index.js, where the changes we use in Observable will update the `ul` tag.
- Updating the `li` in the html file will be easy for us to view the changes in the browser, rather than using the console from Dev tools.

```html
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Webpack App</title>
        <!-- Include a simple style -->
        <style>
            body { font-family: 'Arial'; background: lightgray }
            ul { list-style-type: none; padding: 20px; }
            li { padding: 5px; background: whitesmoke; margin-bottom: 5px; }
        </style>
    </head>
    <body>
        <h1>RxJS</h1>
        <!-- Added div tag, where ul will be used from index.js-->
        <div>
            <ul id="list"></ul>
        </div>
        <script src="/bundle.js"></script>
    </body>
</html>
```

##### Update the `index.js` to include the `Observable` from RXJS

- Below is a simple RxJS code, where we create an `Observable` and emitt some data.
- We demostrate the subscriber or observer, which will read the data and updates the updateHTML()

```ts
import { Observable} from 'rxjs';

var observable = new Observable((observer1)=>{
    console.log("Observer started to emitt");
    observer1.next("message1");
    observer1.next("message1");
    observer1.complete();
});

observable.subscribe((data)=>{
  console.log("subscribed - "+data);
  updateHTML("subscribed "+data);
})

/* Below code is used to update the index.html li tag */

function updateHTML(val:any) {
    var node = document.createElement("li");
    var textnode = document.createTextNode(val);
    node.appendChild(textnode);
    document.getElementById("list").appendChild(node);
}
```

#### Command to execute the webpack-dev server

 - Below command will start the webpack-dev server. 

```
npm run serve
```

- The above command will open up the browser automatically, since we set this config in `webpack.config.js`.

```
   devServer: {
    open: true,
    host: "localhost",
  },
```

#### Output:

![image](https://user-images.githubusercontent.com/6425536/177222395-c133ec78-0775-4cc2-be62-07f70cb7cff0.png)
