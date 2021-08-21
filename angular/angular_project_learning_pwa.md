#### Progressive Web app
  - A web application can be acessed using different types of devices, such as a desktop, mobile, tablet and various network types, such as broadband, Wi-Fi, and cellular. 
  - A web application should work seamlessly and provide the same user experience independently of the device and the network of the user.
  - Progressive Web Apps (PWA) is a collection of web techniques for building web applications to work seamlessly considering above points. 
  - One popular technique used for PWA is the `service worker`, which improves the loading time of a web application.
  - PWA applications stand in between the web and native applications and share characteristics from both. 

Create new project

```
$ ng new weather-app --style=scss --routing=false
```

Adding angular metarial design

```
$ cd <to-project>
$ ng add @angular/material --theme=indigo-pink --typography=true --animations=true

--theme => specific theme will be added by cli. adding a theme involves in modifying angular.json with css files. index.html will also be included with material design icon.

--typography => enables angluar material typography globally. defines how the text content should be displayed. uses Roboto Google font by default. index.html will have the link. other css classed to the body of index.html

--animations -> enables browser animations, imports BrowserAnimationsModule
```

**NOTE:** `@angular/material:` The npm package name of Angular Material library. 
  - This will also install the @angular/cdk package, a set of behaviors and interactions used to build Angular Material design. 
  - Both packages will be added to the dependencies section of the package.json file of the application

#### IMPORTANT: 
   - `environment.ts` - the typescript file is used for development environment. It is used when we use `ng serve` command.
   - `environment.prod.ts` - the typescript file is used for prod environment. It is used when we build the application using `ng build` command.
   - The environment object is imported from the default environment.ts file. The Angular CLI is responsible for replacing it with the environment.prod.ts file when we build our application

Building a PWA weather app.
  - Note, create an account to get the API key from openweatherapp on current data.
  - Add the url and API key in the `enviornment.ts` json

```
apiUrl: 'https://api.openweathermap.org/data/2.5/',
apiKey: '<API key value>'
```

Create a interface, `$ ng generate interface weather` define the object that will be recevied.
Check [link for more param](https://openweathermap.org/current#parameter)
```ts
//below will be used in the oberservable
export interface Weather {
  weather: WeatherInfo[],
  main: {
    temp: number;
    humidity: number;
  };
  wind: {
    speed: number;
  };
  sys: {
    country: string
  };
  name: string;
}
interface WeatherInfo {
  main: string;
  icon: string;
}
```

- In order to use HttpClient to access the openweatherapp api, we need to import `HttpClientModule` from `@angular/common/http`. update the `app.module.ts`
- Create service `$ ng generate service weather`-
     -  import `Httpclient` from `@angular/common/http` and inject http service to constructor `constructor (private http: HttpClient) {}`
     -  regarding the data metrics check the [link](https://openweathermap.org/current#data)
```ts
// import the environment typescript from '../environments/environment'
// import the interface Weather 
// import httpparam form @angular/common/http
// import Observable from rxjs
getWeather(city: string): Observable<Weather> {
   // Note: we are using constructor object of HttpParams and set method for each params. The HttpParams object is immutable, so we are using set method. 
  const options = new HttpParams()
    .set('units', 'metric')
    .set('q', city)
    .set('appId', environment.apiKey);
    
    // The http.get method is used which take two parameter one the url and the object to pass addition configuration as url param
    
  return this.http.get<Weather>(environment.apiUrl + 'weather', { params: options });
}
```

- To display the weather information, create a component `$ ng generate component weather` 
- In the `app.component.html` add the newly create component name `<app-weather></app-weather>`

- Lets import few Material package to `app.module.ts`
   - import { MatCardModule } from '@angular/material/card`
   - import { MatInputModule } from '@angular/material/input'
   - import { MatIconModule } from '@angular/material/icon'
   - add the modules to imports section as well.

- In the `weather.component.ts`, inject the WeatherService service in constructor and import the package.
   - declare a weather variable
 ```
 import { Component, OnInit } from '@angular/core';
import { Weather } from '../weather';
import { WeatherService } from '../weather.service';
@Component({
  selector: 'app-weather',
  templateUrl: './weather.component.html',
  styleUrls: ['./weather.component.scss']
})
export class WeatherComponent implements OnInit {
  weather: Weather | undefined;
  constructor(private weatherService: WeatherService) { }
  ngOnInit(): void {}
  
  searchQuery(city: string) {
  this.weatherService.getWeather(city).subscribe(weather => this.weather = weather);
  }
}
```
- Udpating the weather component template `weather.component.html`
```html
     <mat-form-field>
      <input matInput placeholder="Enter city" #cityCtrl (keydown.enter)="search(cityCtrl.value)">
      <mat-icon matSuffix (click)="search(cityCtrl.value)">search</mat-icon>
    </mat-form-field>
    <mat-card *ngIf="weather">
      <mat-card-header>
        <mat-card-title>{{weather.name}},{{weather.sys.country}}</mat-card-title>
        <mat-card-subtitle>{{weather.weather[0].main}}</mat-card-subtitle>
      </mat-card-header>
      <img mat-card-sm-image src="https://openweathermap.org/img/wn/{{weather.weather[0].icon}}@2x.png" [alt]="weather.weather[0].main">
      <mat-card-content>
        <h1>{{weather.main.temp | number:'1.0-0'}} &#8451;</h1>
        <p>Humidity: {{weather.main.humidity}} %</p>
        <p>Wind: {{weather.wind.speed}} m/s</p>
      </mat-card-content>
   </mat-card>
```
- Adding some css styles
```
# :host selector is an Angular unique CSS selector that targets the HTML element hosting our component (in our case the app-weather HTML element)
:host {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding-top: 25px;
}
mat-form-field {
  width: 20%;
}
mat-icon {
  cursor: pointer;
}
mat-card {
  margin-top: 30px;
  width: 250px;
}
h1 {
  text-align: center;
  font-size: 2.5em;
}
```
 - At this point, `$ ng serve` command will successfully launch the application.
 - Check the Audit section in borwser dev tools if the service worker is setup for application.

#### Setting up `service worker` in angular.
 - Add the pwa package to angluar project
```
$ ng add @angular/pwa
```
- The above command add `@angular/service-worker` package to dependencies section in `pacakge.json`
- It also creates `manifest.webmanifest` file in `src` folder. This file has the info about application needed to install and run it as native app. It adds `assests` array of the `build` poperty in `angular.json`.
- It also creates `ngsw-config.json` at the root of project. this is the service worker congiguration file used to define configration-specific artifacts like which resources to be cached and how they are cached (example options freshnes, prefetch) etc. [link]( https://angular.io/guide/service-worker-config#service-worker-configuration)
- `angular.json` is updated with ngswConfigPath property of build.
- the `serviceworker` property is set to true in `build` configuration in `angular.json`
- It registers service worker in the `app.module.ts` file.
  ```
  import {ServiceWorkerModule} from '@angular/service-worker;
  ...
  import : [ ...
  // this will register the service worker as soon as the app is stable or after 30 seconds (whichever comes first)
  ServiceWorker.register('ngsw-worker.js', 
    {enabled: environment.production,registrationStrategy: 'registerWhenStable:3000'})
   ], providers : [],
  ...
  ```
  - `ngsw-works.js` file contains the acutal implementaton of service worker. this is automatically created for us when we build the application in production mode. angular uses `register` method of ServiceWoekermodule class to register it within our application.
  - Several icons are created alone with tehma color added to `index.html`
  
  ##### Installing http-server to the project.
  ```
  $ npm install -D http-server
  
  $ ng build  // update the environment.production.ts with url and api key
  ```
  - In order to execute `$ npm run server` to start the `http-server` update `package.json`
     - if required add the dependencies of http-server
  ```
  ...
  "scripts" : {
  ...
  "server" : "http-server -p 8080 -c-1 dist/weather-app"
  }
  ...
  ```
  - Now run below command to start the server in 8080 port. The `ng build` will create artifacts under the `dist/weather-app` configured in angualr.json

------------
  **Note:**: Adding the PWA above will make the app to be chaced in the browser, and any update to the application will not be reflected in the browser.
  
  ##### How to update the changes to the chaced PWA apps? we can use `SWUpdate` but here we will use `MatSnackBarModule`.
  - in `app.module.ts` add/ import {`MatSnackBarModule`} from `'@angular/material/snack-bar'`; update the imports array as well.
     - MAtSnackBarModule - an angular material module allows us to interact with snack bar. (a snack bar is a pop-up window that usaully appears on bottom of hte page and used for notification puprposes)
   
   - Inject `MatSnackBar` and `SWUpdate` services in the constructor of `app.component.ts`.
      - `SWupdate` service is an angular service worker and contains observables that we can use to notify regarding the update process on our application
      
   - Import onInit lifecycle hook to update the changes
  ```
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SwUpdate } from '@angular/service-worker';
import { switchMap,filter, map  } from 'rxjs/operators';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'weather-app';
  constructor(private updates: SwUpdate,
              private snackbar: MatSnackBar) {}
}

ngOnInit() {
  this.updates.available.pipe(
    switchMap(() => this.snackbar.open('New version notification!', 
              'Update now').afterDismissed()),
              filter(result => result.dismissedByAction),
               map(() => this.updates.activateUpdate().then(() =>
      location.reload()))
  ).subscribe();
  // upon update now, we are reloading the screen.
}
  ```
   - the `SWUpdate` service contains `available` observable property that can be used to get notified when a new version is available.
   - Typically, we tend to subscribe to observables, but here we don't instead we subscribe to the pipe method (an RxJS operator used for composing multiple operators) 
   - `switchMap` - is called when a new version of our application is available. It uses open method of `snackbar` property to show a snack bar witn an action button and subscribes to `afterDismissed` observable. (this afterDismissed observable emits when the snack bar is closed either clicking the button or prorgramatically using its API methods)
   - `filter` - is called when snack bar is dismissed using button
   - `map` - calls the `activateUpdate` method of `updates` property to apply new version. once the new version is updated the window is reloaded.
 
 ##### build and start the application
 ```
 $ ng build
 $ npm run server  //server script will be executed to run http-server in 8080
 ```
 
 Now add a new component to the project. using `$ ng generate component header`
 - Import `MatButtonModule` and `MatToolBarModule` in `app.module.ts`
 - Include the header component to `app.component.html` (\<app-header>\</app-header>)
 - update the `header.component.html` and `header.component.css`.
 ```
 <mat-toolbar color="primary">
  <span>Weather Now</span>
  <span class="spacer"></span>  // in css file add .spacer { flex: 1 1 auto' }
  <button mat-icon-button>
    <mat-icon>refresh</mat-icon>
  </button>
</mat-toolbar>
 ```
  Now, if we build (`$ ng build`) and start the server (`$ npm run server`). should popup the New version notification message with the button in the browser.
  - Open up the app in a private browser, in network make it offline.
  - perform update to component, and latter enable the netowrk in browser refersh the app and wihin few seconds should see the message.

--------------------------------

##### Hosting to Google FireBase.
  - Using google account create a project. [Firebase](https://console.firebase.google.com.).
     - click add project button
     - Enter a name
     - click continue
     - disable google analytics
     - The project will be created, click continue.
  - The firebase configration is complete now.

  - in your angluar application issue below command
  ```
   $ ng add @angualr/fire
  ```
   - after installing the package above command will request to  authenticate to firebase 
      - use the token to authorize the firebase cli to access the project
   - a bunch of dependecies will be added in `package.json`
   - will create `.firebaserc` file at root of project, which has the details of selected project
   - `firebase.json` will be created with the firebase configuration
   - the default directory of `ng build` is `dist`, the command will add `deploy` entry to the architect section of the `angular.json`
   - now issue `$ ng deploy`, this will deploy the artifacts to firebase and provides the hosted url back in the console.
   - After validating, delete project from firebase project settings.
