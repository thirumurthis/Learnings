
Create a project

```
 $ ng new <project-name>
```

Create a component

```
$ ng g c <component-name>
or
$ ng generate component <component-name>
```

To compile the application and serve the application in local browser
```
$ ng serve
or 
$ ng serve --open --port 8080
```

Creating Directive

What is directive?
Driective:
   Helps to add custom behaviour to DOM element.

Component is also a Directive, called as self-contained directive.

```
$ ng generate directive <directive-name>
or 
$ ng g d <directive-name>
```

Creating Service:

What is Service? - When we need to make two or more component to communicate with each other using services.

```
$ ng generate service <service-name>
or 
$ ng g s <service-name>
```

Angular 8.0
  - Ivy, compile engine and rendering engine. (reduce bundle size)
  - Dynamic routing (perform lazy loading of routes)
  - type script support for 3.1
  - cli interaction prompts 
  - virtical scrolling 
  - drag and drop feature.
  
#### COMPONENT:
  - building blocks of angular
  - angular comprises of multiple component, which build the application.
  - type of `directives` - components has a template of their own, this is known as self-contained directives. it has its own UI.
  - in general `directives` - when we need to add some specified behaviour to DOM element, we use directive
  - `ng new project <project-name>` will create a component itself.
  
##### Files created within component (using CLI):
   - \*.html 
   - \*.spec.ts - testing 
   - \*.css - style
   - \*.ts - logic goes here
   
  **module** sort of containers for all components/directives.
  
  `app.module.ts` is updated with the created component, by including it in the `array @NgModule` 
  and also the component is `imported` here in this file.
 
 Note: If we don't want the spec.ts file to be created, we can use the option/flag
 ```
 $ ng g c <component-name> --spec=false
 ```
 
 ##### Creating a component manually (without cli)
    - in the app directory, create another component files directly (files extension) or within a folder.
    - SomeComponent.component.ts 
    - SomeComponent.component.html
    - SomeComponent.component.css
 
 In "SomeComponent.component.ts" file, update the below code.
   - create a class
  ```
  -- Create the content of component:
  import { Component } from '@anguar/core';
  //second step - add the decorator @Component and import the component from angular package
  //third step: - add the selector property within the @component decorator
               // add the templateUrl - provide link to html5
               //    - for inline template - use template : '<h2>some-template-content</h2>'
               // add the styleCSS - two option inline CSS or external CSS
               //    - for external Css, we try using array of CSS
               //    - for inline Css, we use styles
  @Component ({
    selector: 'app-sc-component',
    templateUrl : './somecomponent.component.html',
    styleUrls : [ './somecomponent.component.css']
  })
  
  // first step - create class name
  export class SomeComponent{
  
  }
  ```
  
  ```
  -- Update the Module (app.module.ts)
   update the @NgModul array with the component class name
   import the somecomponent path
  ```
  
 If the component is to return the data then component class needs to ` implements OnInit` *_life-cycle hook_*.
 And we need to implement the method `ngOnInit ()` from interface, like below
 
 ```
 // implements the onInit 
 export class SomeComponent implements OnInit{
 
 data : any;
 
 //implement the interface info,
 ngOnInit() : void {
   this.data = this.getData ();
 }
 
 getData(){
    return  [ { "name": "name1" }, {"name" : "name2"} ];
    //note make sure the return and following statement are in the same
    //line since the javascript treats as return statement, will not be
    //able to reach it.
 }
 ```
 
 Update the somecomponent.component.html to achive, **interpolation** data binding using {{}}
 ```
 <table>
   <tr *ngFor = let d of data'>
     <td> {{d.name}}</td>
   </tr>
 ```

In order to use bootstrap for styling in the somecomponent.component.html, refer the bootsrap css in the index.html.
Or use css styling in the .css file within the component.

```
//index.html - add for including the bootstrap style sheet
<link href="https://..bootstrap...css">

//somecomponent.component.html
<table class="table table-striped">
```

### Data binding:

Bind the data from component(ts) to the view (html) template:
  - *Interpolation* {{..}}
     - used to display the value of attribute present in the component.
  - *Property binding*  [..]
     - used to bind the property of element in component. 
  - *Event binding*  (...)
     - bind event of the component to the view template.
  - *Two-way data binding*
     - communication between the component & view and vice versa.
     - any change in the UI/view the value is probagated to component.
     - any change in the component is reflected in the view
         property binding [..]
               +                 ----->   Two way data bindning [(...)]
         Event binding (..)

**Property binding - example** 

```
//app.component.ts - define a property
...
export class AppComponent ....{
data : Any;

//property to be used in the view.
heightValue : number = 10;

...
}

// use the property in view - app.component.html
...
<button [style.height.px] = 'heightValue' >Link</button> //heightValue will be used from the component ts file.
...
```

**Event binding - example**

```
// app.component.ts
...
export class AppComponent ... {
value1 : number = 10;
value2 : number = 20;
result : number ;
product(){
this.result = this.value1 * this.value2;
}
...
}

//app.component.html
..
<button class="btn btn-success" (click)="product()">Product</button> //Event binding (...)
...
```

**Two-way databinding - example**
  - when the data int the model got updated, the view is also updated.
  - when the view is updated the changes are probagated back to component/model.
 
 Another way of defining this is, combination of property binding and event binding which turns into **[(ngModel)]**
 
 ```
 // in app.component.html
 ...
 <input type="text" [(ngModel)]= "userInput" >
 <br>
 {{userInput}}
 ...
 
 Note: At this point if we didn't include the "ngModel" value in the Component there will be an error message in the 
 console of browser "Template parse error: Can't bind 'ngModel' since it isn't a known property of input...." 
 
 Solution: we need to import the "FormsModule" module in app.module.ts and update NgModule -> imports array
 import {FormsModule} from '@angular/forms';
 
 @NgModule({
 ...
 imports :[
 BrowseModule, FormsModule
 ... 
 ```
 
 Note: The `app.module.ts` needs to be updated in this case to import the `FormsModule` to use [(ngModel)].


 
 
  

