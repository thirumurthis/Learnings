
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
     
```     
         property binding [..]
               +                 ----->   Two-way data bindning [(ngModel)]
         Event binding (..)
```

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

##### Sample program using data binding to product two numbers 
```
//app.component.html
Enter first number 
<input type="number" placeholder="first number" [(ngModel)]= "val1" >
{{val1}}<br>
<input type="number" placeholder="second number" [(ngModel)]= "val2" >
{{val2}}<br>
<button class="btn btn-success" [style.height.px]="heightValue' (click)="product()">Product</button>
{{result}}

//app.component.ts
export class AppComponent ... {
val1 : number = 10;
val2 : number = 20;
result : number ;
product(){
this.result = this.val1 * this.val2;
}

// similar to product, we can have sub, add, etc function

```

### Component Communication

  - @Input
  - @Output
  - Event Emitter

ngOnChanges *life-cycle* hook
 
 Communication between component can happen in below ways too,
  - using template reference variable
  - `@viewChild` and `@ContentChild`
  - Services - used when two component are not related to each other.
  
  When two component need to share data between each other.

```
@Input () decorator

                               data   
  component 1 (parent)       -------->      component 2 (child)
```

###### @Input decorator
 Inputs value from one component to another component.
 
```
//app.component.ts -- This is parent component
...
export class AppComponent implements OnInit{
..
messageFromParent = "from parent";
messageToChild= "child message, passed from parent";
...
}
```
---------------------
```
//SomeComponent.component.ts -- This is the child component
...
@Component({
 selector: 'app-sc-component'
 ....
})
//import the input decorator within this component
export SomeComponent... {
...
//using @input() decorator for passing values from parent to child
@Input() passedMessage: String; //holds the value of parent component
}
```
-----------------------
```
// app.component.html --- this will hold the child component referenc
   //Interpolation
{{messageFromParent}} 
  // use the selector from the child component (SomeComponent.component.ts)
  // use the "property binding" to pass value from parent (app.component.ts)
  // to child component (SomeComponent.component.ts) to passedMessage attribute
  
 <app-sc-component [passedMessage]='messageToChild'></app-sc-component>
 ```
 -----------------------
 ```
 //SomeComponent.component.html
 {{passedMessage}}
 
```

#### @Output & Event Emitter

```
@Output() & Event Emitter

                            @Output()
component 1 (parent)     <---------------   component 2 (child)
                           Event emitted
```

For a scenario, when clicking a button in child component and it needs to execute a function in parent componment we can use **@Output()** 

```
//SomeComponent.component.ts - this is the CHILD component
//import the @Ouput within this component
....
export class SomeComponent....{
...
@Output() emitterObj = new EventEmitter();
result : number = 100;

    //function which will emit the value from this component
 passResultUsingEmit(){
    this.emitterObj.emit(this.result); // Emits the properties from child component
 }
...
}
```
--------------------
```
// SomeComponent.component.html --- child component view template

//This Event binding will invoke the child component function.
<button (click) = "passResultUsingEmit()"> Click here to emit </button>
-------------------
// app.component.html  --- This is PARENT component view template

{{messageFromParent}}
//In the case of Event Emitter & @Output () decorator
//we need to use Event binding - which gets some event type using "$event"
<app-sc-component (emitterObj)="displayEmittedValue($event)"></app-sc-component>
```
---------------------
```
//app.component.ts --- This is PARENT component ts file 
...
export class AppComponent implements OnInit{
..
messageFromParent = "from parent";
messageToChild= "child message, passed from parent";
...
//Create the function which will be used by the Event binding from the parent component view
result : number ;
displayEmittedValue(result){
  //use console to see if the value is emitted
  console.log(result);
}
```
---------------

##### ngOnChanges *life-cycle hook*
 Any change on input bound property or output bound event then angular use ngOnChange life-cycle hook in which the value gets updated on DOM. 
 
Scenario usage, when there is a change in value then the color of the button. 

```
//SomeComponent.component.ts include import for `onChanges` from @angular/core
import { Component, OnInit,Input, EventEmitter, Output, OnChanges} from '@angular/core'
...
 
 export class SomeComponent implements OnInit, OnChanges {
 
 //implement the method from onChanges interface
 ngOnChanges(){
   //if wanted to perform some color changes
   if (this.result > 100){
      this.color ='red';
   }
   ...
 }
```

##### Template Reference Variable

Scenario: Assume there is a component view/html with below content
If we meed to acces the content of tempate where in this case below \<h2\> element tag and display it after the parent \<div\> which is not reference inside the component. Without using it in the component \(*.ts file\), then we can use **template reference variable** option.
 
 Using # tag within the element.
 
```
   <div>
      <h1>Header of the component</h1>
      <span>Align this sub title</span>
   </div>
```
Implementing the tempate reference variable

```
//app.component.html 
   <div>
     // using a hash with a variable name, latter this can be accessed outside the 
     // parent div tag
      <h1 #elementRef>Header of the component</h1>
      <span>Align this sub title</span>
   </div>
   
   //Below is the way to access the above <h1> using template reference 
   {{elementRef.textContent}} //"Header of the component" will be displayed 
   
   <button (click)="display(elementRef)">Click here</button>
```
--------------
```
//app.component.ts
...
export class AppComponent...{

//passed value from the click event above - the type is HTMLInputElement
display(elementValue: HTMLInputElement) {
  //upon clicking the template html5 the console will print the value
  console.log((<HTMLInputEelement>elementValue).value);
}
```

In case of template refernce variable, the value of \<h1\> is passed as parameter to a function.
 
 Scenario: In case if we don't want to pass the value as parameter, and perform some operation before passing the value.
 
 @ViewChild decorator helps in this scenario.
    - When parent component (app.component.ts) wanted to access the whole child component template (i.e. SomeComponent.component.html)

```
 //app.component.ts - PARENT component
 ...
 export AppComponent implements OnInit,AfterViewInit {
 ...
 
 // init dom boject using ngAfterViewInit
 // implementation of method from AfterViewInit life-cycle hook 
 ngAfterViewInit(){
   console.log(this.somecomponentviewfirstchild);
 }
 
 //@ViewChild decorator is used and passed with child component class and options (in this case {static:false})
 //Below alone will not pass the child content to parent. init dom object
 @ViewChild(SomeComponent,{static:false}) somecomponentviewfirstchild : SomeComponent;
 //The compelet child (SomeComponent.component.html) content id displayed in the somecompoenentviewfirstchild value.
 ```

 ##### @ContentChild
  - If we have some content within the `ngContent` we can use `@ContentChild` to access it.
   - Accessing content of component in another component using @ContentChild

```
// app.component.html - PARENT component
....

<button class="btn btn-info" [style.background]='color' (click)="display()">Click here</button>

//The content of the CHILD is applied here using ng-content
// use the selector "app-sc-component" from SomeComponent.component.ts
<ng-content select="app-sc-component"></ng-content>
```

How to access the `ng-content` in the app.component.ts PARENT file:
```
// app.component.ts

export class AppComponent implements OnInit,  {

// use the reference to the CHILD component
@ContentChild(SomeComponent, {static:false}) somecomponentViewContent: SomeComponent;

// we need to declare the life-cycle hook
ngAfterContentInit(){
  console.log(this.somecomponentViewContent); 
  // the ng content is access from the template of (app.component.html ng-content)
}
..
}
```

##### Directive:
  - Used to add a specified behavious to the DOM element. This is like a marker to the DOM.
  
 ##### Types:
   - Component directive - which has html of its own (building blocks of angular)
   - Strutural directive - used to change the layout like `ngFor`, `ngIf`
   - Attribute directive - used to change the apperance or behaviour
   - Custom directive

```
//app.component.html
//ngIf and ngFor usage
....
export class AppComponent...{
...
flagCheck : boolean = false;
result : any;
onInit(){
this.resut = this.someFunction();
}
...
someFuntion(){
thisflagCheck = true;
return [{"name":"name1"},{"name":"name2"}];
```
----------
```
//app.component.html

<table *ngIg=flagChek>
   <tr *ngFor = "let name of result">
      <td>{{{name}}</td>
   </tr>
</table>
```

##### Custom Attribute directives
   - HostBinding
   - HostListener

How to create  `Custom Attribute directives` ?

##### To create using CLI:
```
$ ng g d <directive-name>
```

Files created:
  - \*.directive.ts
  - \*.directive.spec.ts (used for test)
  - app.module.ts - updated/registered with the new directive.
  
##### To create directive manually:
 - To create a directive, create the class with `@Directive` decorator
 - Inject services ElementRef and Renderer
 - Register directive in module (for example: app.module.ts file, when using cli)
 - Use the created directive
 
 ```
 //color.directive.ts - created by the CLI
 //STEP:1 - created a class with @Directive decorator
 import {Directive} from '@angular/core';
 
 @Directive({
 selector: '[backgroundColor]'
 })
 export class ColorDirective {
   // STEP:2 - inject with the ElementRef and Renderer
    constructor(private element: ElementRef, private rendrer : Renderer){
      // call the method
      this.updateColor('blue');
    }
    
    // To use the Directive - create a function outside constructor.
    updateColor(color : string){
       this.renderer.setElementStyle(this.element.nativeElement, 'color', color);
    }
 }
 ```
 ----------------
 ```
 //app.component.html
 
 <div  backgroundColor> //the attribute directory selector value is used here
    <p> Using custom attribute directive </p>
 </div>
 ```

Using custom attribute on element in this case we used in div element.
And we wanted to capture the events like click on these elements - then we use `@HostListener`

Sample div element we created:
```
  <div  backgroundColor> 
    <p> Using custom attribute directive </p>
 </div>
```
----------------
 ```
 //color.directive.ts - created by the CLI
 import {Directive} from '@angular/core';
 
 @Directive({
 selector: '[backgroundColor]'
 })
 export class ColorDirective {
    constructor(private element: ElementRef, private rendrer : Renderer){
      // call the method
      this.updateColor('blue');
    }
    
    updateColor(color : string){
       this.renderer.setElementStyle(this.element.nativeElement, 'color', color);
    }
    
    //import the hostlistener from core package
    // when the user clicks the below funtion will be invoked.
    @HostListener('click') userClick(){
    this.color = 'red';
    }
 }
 ```
 
 
