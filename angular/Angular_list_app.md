### A simple app to add remove list and navigate between component.
  - The code demonstrates,
     - interpolation
     - Event binding
     - property binding
     - two way binding usign @NgModel
     - Routing
        - passing and reading parameter during routing
  
Note:
 - When using routing, the component doesn't fall under the parent child component category.
 - In this case the data can be passed to components either using query param or service.
 - Below example passes the data using query param.

- Download node.js, and make sure the `node -v` and `npm -v` displays the intended version. (at this time i was using 15.4)
- after installation, create a folder and issue below command
```
> npm install -g @angular/cli
```

- once the installation is complete.
```
> ng new list-app
# once completed, navigate to the list-app folder.
```
 
- Open up Visual studio code, add `Angular Language Support` for development ease.
- Create two components, Home and About
```
> ng generate component home
> ng g c about
# two directory will be created and added to the angular project
```

- update the `app.component.html` remove the place holder content except the `<router-outlet>...` line.
- we will use the __`routerLink`__

#### app.component.html

```js
<div routerLink="">Home</div>
<div routerLink="about/3000">About</div>

<router-outlet></router-outlet>
```

#### app.component.ts
  - Note: `@ngModel` cannot be used within `form tag`.
```
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms'; // --------> This module was added deliberately for using of two way binding using (@ngModel)

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component'; //------> when issuing ng generate component this will be updated
import { AboutComponent } from './about/about.component'; //------> when issuing ng generate component this will be updated

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    AboutComponent
  ],
  imports: [
    FormsModule,   // ---------> Import the FormsModule in this component, without this the ngModel cannot be accessible in other component
    BrowserModule,
    AppRoutingModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```
#### home.component.html
```js
<div>
    Data from component ts, interpolation : {{valueFromTs}}  <!-- ******* USING ITERPOLATION ************ -->
</div>

<p>Add item to list: current count (<strong>{{itemCount}}</strong>)</p>
<div>
    <input type="text" placeholder="Input text" [(ngModel)]="inputItem">
    <input type="submit" [value]="buttonName" (click)="addItem()">              <!-- ************ PROPERTY/ATTRIBUTE BINDING using [] on value ************ -->
    <input type="submit" value="clean Item Context" (click)="clearInput()">     <!-- ************ EVENT BINDING using () on click ************* ->
</div>

<div>
    <p>Items: <span style="color: red;">{{errorMsg}}</span></p>           
     <p *ngFor="let item of listOfItems; let i = index" (click)="removeItem(i)">  <!-- STRUCTURAL DIRECTIVE ngFor usage -->
         {{item}}</p>
</div>
```

#### home.component.ts
```js
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  valueFromTs : number = 3000;
  buttonName: string = 'Add Item';
  listOfItems: any =[];
  errorMsg: string='';
  itemCount : number =0;

  inputItem :string ='Item 1';
  constructor() { }

  ngOnInit(): void {
    this.itemCount = this.listOfItems.length;
  }

  addItem():any
  {
    if(this.inputItem == ''){
        this.errorMsg = 'Input cannot be blank!!'
        return;
    }else{
      this.errorMsg = ''
    }
   this.listOfItems.push(this.inputItem);
   this.itemCount = this.listOfItems.length;
  }

  removeItem(i: number):any{
    this.listOfItems.splice(i,1);
    this.itemCount = this.listOfItems.length;
  }

  clearInput():any{
    this.inputItem = '';
    this.errorMsg = ''
  }
}

```
#### about.component.html
```js
<a href="" (click)="goHome()">Go Back to Home!!!</a>
```

#### about.component.ts
  - Adding router details to fetch the __`queryParameter`__ and __`navigate back`__
```
import { Component, OnInit } from '@angular/core';
import {ActivatedRoute,Router} from '@angular/router';  //---> ************ IMPORT ACTIVATEDROUTE to read the query parameter
                                                        //---> ************ Use app-routing.module.ts to define path values.
@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {

  queryParam : number =0;
  constructor(private route: ActivatedRoute, private router: Router) { //------> ************** DEPENDENCY INJENCTION of ActivatedRoute and Router
      route.params.subscribe(result => {this.queryParam = result.id;console.log('passed id: '+result.id)}); // ---> ************ Read the value by subsribing to promise
   }

  ngOnInit(): void {
  }
  
  // -------> *************** Simple function to navigate back to the home component.
  goHome(): any{
    this.router.navigate(['']);
  }
}
```

#### app-routing.module.ts 
  - This file gets generated when crating a angular project and when prompted for Routing to yes.
  - To define the path
```js
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home/home.component';  // -->*********** IMPORT THE COMPONENT
import { AboutComponent } from './about/about.component'; // -->*********** IMPORT THE COMPONENT

// --------> ******* DEFINE THE PATH WITHIN THE ARRAY LIKE BELOW
const routes: Routes = [
  {path:'',component:HomeComponent},
  {path:'about/:id',component:AboutComponent}  // -----------> THE ID PARAMETER IS PASSED USIGN :id
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

```
