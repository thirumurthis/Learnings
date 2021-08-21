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


