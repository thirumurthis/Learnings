##### Angular supports component based development.
 - Each component we create is isolated from every other part in an application.
 - within the component, we have both business logic and view
 - Every application will have one top-leve component, several sub components
 
 ```
                Component (Root)
                     |
           -----------------------
           |                      |
       component1 (child)      component2 (child)
           |
  ---------------------
  |                   |
 component11         component12
 ```

Angular utlizes `MVVM` design pattern, model view and viewmodel.

`Model` : Used to define the structure of an entity.simply a javascript class
`View` : Is the visual representation of an application (html template)
`ViewModel` : Contains the business logic

View and ViewModel are connected thorug data binding. By default any change to the viewModel properties will be reflected on view too. In angular, the viewModel is a typescript class.

Component : 
 - This is a custom HTML tag, with functionality attached.
 - Seperation of concerns
 - Angular manges the life-cycle of the components, like create, update and delete when user moves through application, developer can take action at each moment in the component life-cycle using the life-cycle hooks.


###### Angular 8 feature
  - Dynamic import (lazy loading)
  - Typescript 3.1 support
  - Differntial loading
  - Ivy compiling

##### Create a project
```
 $ ng new <project-name>
```

##### Create a component
```
$ ng g c <component-name>
or
$ ng generate component <component-name>
```

##### To compile the application and serve the application in local browser
```
$ ng serve
or 
$ ng serve --open --port 8080
```

#### Creating Directive

What is directive?
Driective:
   Helps to add custom behaviour to DOM element.

Component is also a Directive, called as self-contained directive.

```
$ ng generate directive <directive-name>
or 
$ ng g d <directive-name>
```

##### Creating Service:

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
  
##### COMPONENT:
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
  ```js
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
 
 ```js
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
 
 Update the somecomponent.component.html to achive, **interpolation** data binding using `{{}}`
 ```html
 <table>
   <tr *ngFor = let d of data'>
     <td> {{d.name}}</td>
   </tr>
 ```

In order to use bootstrap for styling in the somecomponent.component.html, refer the bootsrap css in the index.html.
Or use css styling in the .css file within the component.

```html
//index.html - add for including the bootstrap style sheet
<link href="https://..bootstrap...css">

//somecomponent.component.html
<table class="table table-striped">
```

### Data binding:

Bind the data from component(ts) to the view (html) template:
  - *Interpolation* `{{..}}`
     - used to display the value of attribute present in the component.
  - *Property binding*  `[..]`
     - used to bind the property of element in component. 
  - *Event binding*  `(...)`
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

```js
//app.component.ts - define a property
...
export class AppComponent ....{
data : Any;

//property to be used in the view.
heightValue : number = 10;

...
}
```
```html
// use the property in view - app.component.html
...
<button [style.height.px] = 'heightValue' >Link</button> //heightValue will be used from the component ts file.
...
```

**Event binding - example**

```js
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
```
```html

<!-- //app.component.html -->
..
<button class="btn btn-success" (click)="product()">Product</button> 
<!--//Event binding (...) -->
...
```

**Two-way databinding - example**
  - when the data int the model got updated, the view is also updated.
  - when the view is updated the changes are probagated back to component/model.
 
 Another way of defining this is, combination of property binding and event binding which turns into **[(ngModel)]**
 
 ```html
 <!-- // in app.component.html -->
 ...
 <input type="text" [(ngModel)]= "userInput" >
 <br>
 {{userInput}}
 ...
 ```
 Note: At this point if we didn't include the "ngModel" value in the Component there will be an error message in the 
 console of browser "Template parse error: Can't bind 'ngModel' since it isn't a known property of input...." 
 
 Solution: we need to import the "FormsModule" module in app.module.ts and update NgModule -> imports array
 ```js
 import {FormsModule} from '@angular/forms';
 
 @NgModule({
 ...
 imports :[
 BrowseModule, FormsModule
 ... 
 ```
 
 Note: The `app.module.ts` needs to be updated in this case to import the `FormsModule` to use [(ngModel)].

##### Sample program using data binding to product two numbers 
```html
<!-- //app.component.html -->
Enter first number 
<input type="number" placeholder="first number" [(ngModel)]= "val1" >
{{val1}}<br>
<input type="number" placeholder="second number" [(ngModel)]= "val2" >
{{val2}}<br>
<button class="btn btn-success" [style.height.px]="heightValue' (click)="product()">Product</button>
{{result}}
```
```js
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
 
```js
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
```js
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
```html
<!-- // app.component.html --- this will hold the child component reference 
   //Interpolation -->
{{messageFromParent}} 
 <!-- // use the selector from the child component (SomeComponent.component.ts)
  // use the "property binding" to pass value from parent (app.component.ts)
  // to child component (SomeComponent.component.ts) to passedMessage attribute -->
  
 <app-sc-component [passedMessage]='messageToChild'></app-sc-component>
 ```
 -----------------------
 ```html
 //SomeComponent.component.html
 {{passedMessage}}
 
```

##### @Output & Event Emitter

```
@Output() & Event Emitter

                            @Output()
component 1 (parent)     <---------------   component 2 (child)
                           Event emitted
```

For a scenario, when clicking a button in child component and it needs to execute a function in parent componment we can use **@Output()** 

```js
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
```html
<!-- // SomeComponent.component.html --- child component view template

//This Event binding will invoke the child component function. -->
<button (click) = "passResultUsingEmit()"> Click here to emit </button>
-------------------
```html
<!-- // app.component.html  --- This is PARENT component view template -->

{{messageFromParent}}
<!-- //In the case of Event Emitter & @Output () decorator
//we need to use Event binding - which gets some event type using "$event" -->
<app-sc-component (emitterObj)="displayEmittedValue($event)"></app-sc-component>
```
---------------------
```js
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

```js
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
 
```html
   <div>
      <h1>Header of the component</h1>
      <span>Align this sub title</span>
   </div>
```
##### Implementing the tempate reference variable

```html
<!--app.component.html -->
   <div>
     <!-- using a hash with a variable name, latter this can be accessed outside the 
      parent div tag-->
      <h1 #elementRef>Header of the component</h1>
      <span>Align this sub title</span>
   </div>
   
   <!-- Below is the way to access the above <h1> using template reference -->
   {{elementRef.textContent}} <!-- //"Header of the component" will be displayed -->
   
   <button (click)="display(elementRef)">Click here</button>
```
--------------
```js
//app.component.ts
...
export class AppComponent...{

//passed value from the click event above - the type is HTMLInputElement
display(elementValue: HTMLInputElement) {
  //upon clicking the template html5 the console will print the value
  console.log((<HTMLInputEelement>elementValue).value);
}
```
##### @ViewChild
In case of `template refernce variable`, the value of \<h1\> is passed as parameter to a function.
 
 Scenario: In case if we don't want to pass the value as parameter, and perform some operation before passing the value.
 
 `@ViewChild` decorator helps in this scenario.
    - When parent component (app.component.ts) wanted to access the whole child component template (i.e. SomeComponent.component.html)

```js
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

```html
<!-- // app.component.html - PARENT component -->
....

<button class="btn btn-info" [style.background]='color' (click)="display()">Click here</button>

<!-- //The content of the CHILD is applied here using ng-content
// use the selector "app-sc-component" from SomeComponent.component.ts -->
<ng-content select="app-sc-component"></ng-content>
```

How to access the `ng-content` in the app.component.ts PARENT file:
```js
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

```js
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
```html
<!-- //app.component.html -->

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
 
 ```js
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
 ```html
 //app.component.html
 
 <div  backgroundColor> //the attribute directory selector value is used here
    <p> Using custom attribute directive </p>
 </div>
 ```
##### @HostListener
Using custom attribute on element in this case we used in div element.
And we wanted to capture the events like click on these elements - then we use `@HostListener`

Sample div element we created:
```html
  <div  backgroundColor> 
    <p> Using custom attribute directive </p>
 </div>
```
----------------
 ```js
 //color.directive.ts - assume the template/structure created by the CLI
import { Directive, ElementRef, Renderer, HostListner } from '@angular/core';
 
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
 
 ##### @HostBinding:
 When we need capture the property of the host element, example height, border, etc.
 
 ```js
 //color.directive.ts - created by the CLI
 @Directive({
 selector: '[backgroundColor]'
 })
 export class ColorDirective {
    constructor(private element: ElementRef, private rendrer : Renderer){
      // call the method
      this.updateColor('blue');
    }
    
    //property that needs to be captured from host element
    // import the decorator 
    @HostBinding ('style.border') border : string;
    
    updateColor(color : string){
       this.renderer.setElementStyle(this.element.nativeElement, 'color', color);
    }
    
      @HostListener('click') userClick(){
  
  //The host element color needs to be updated 
    // so the color is applied to the host element when the click happens
    this.border = '10px solid blue';
    
    this.color = 'red';
    }
 }
 ```
 
 ### Forms:
 ##### Template Driven Forms and perform validation on them
  Forms are used to gather info and submit info to the servers.
 
 ##### Types of forms:
    - Template Driven Forms
    - Reactive Forms (Advanced)
  
Difference between the template Driven forms vs Reactive Forms:
 - Template Driven forms: 
     - Forms used within the html of component.
     - Unit Testing little difficult
 - Reactive Forms: 
     - Forms used in the typescript file of component.
     - Unit Testing is easy
 
 ##### Template Driven Forms:
   `ngForm` directive
   
 ```html
 <!-- // Somecomponent.component.html
 // using boostrap classes - use npm to install -->
 <form (ngSubmit)="save(form)" #form="ngForm">  
<!--  //#form is the template reference variable used.
 // event binding of submit, which invokes the save funtion within the typescript -->
 <div class="form-group">
 <input class="form-control" type="text" [(ngModel)]="name"><br>
 <div>
 <button>Click here</button>
 </form>
 
<!-- //the template reference is interpolated and piped to view -->
 {{form | json}}
 {{form.value | json}}
 ```
 ----------
 In order to use the forms module within the typescript import the forms module
 ```js
 //app.module.ts
 //import the FormsModule
 import { FormsModule } from '@anguar/forms';
 @NgModule({
 ...
 ...
 imports: [
 BrowserModule,FormsModule
 ...
 ```
 ---------
 ```js
 //SomeComponent.component.ts
 ...
 @Component({
 ...
 export class SomeComponent... {
 ..
 // takes form object of NgForm class type
 save(form: NgForm){
  console.log("submitted from the form "+form.value);
  }
  ....
 ```

##### Adding validation to the form

html5 - validation keyword required, minimumlength, etc.

angular - turns off validation in the application everytime.

ngNativeValidate - this will allow validation in the application

```html
<!--SomeComponent.component.html-->

<!--//add ngNativeValidate -->
 <form ngNativeValidate (ngSubmit)="save(form)" #form="ngForm">  
 <div class="form-group">
   <input class="form-control" type="text" [(ngModel)]="name" required minlength="3" maxlength="10"><br>
 <div>
 <button>Click here</button>
 </form>
```

##### To check css style that is applied to the form since using the bootstrap
```html
<!-- //SomeComponent.component.html -->

<form ngNativeValidate (ngSubmit)="save(form)" #form="ngForm">  
 <div class="form-group">
  <!-- //Create a template reference for this element -->
   <input class="form-control" type="text" [(ngModel)]="name" required #tempRef><br>
   //interpolate the values
   {{tempRef.className}}
 <div>
 <button>Click here</button>
 </form>
```

##### Reactive Forms
  - Bind with model
  - Validation on  reactive forms
  - easy to perform unit testing
  - dynamically adding elements
  - applying different validation for different scenario
  - when the form is on the component class it is easy to test 
```js
//app.component.ts
// create FormModel to work with Reactive forms
// For FormModel, import FormGroup and FormControl instance in component class
import { FormGroup, FormControl } from '@angular/forms'

// The data model represents the data passed from the backend server or hardcoded value
// The Form model is the instance of the element (input) that present in the template

export class AppComponent implement OnInit {

customForm: FormGroup;
Userdata = new Userdata();

ngOnInit(){
  this.customForm = new FormGoup({
  name: new FormControl (),
  age: new FormControl ()
 });
 }
 
 save(){
   console.log(this.customForm);
 }
}
```
----------
```js
//app.module.ts
import ....
....
import { ReactiveFormsModule} from '@angular/forms';
@NgModule({
...
// In previous Template dirven we imported formsmodule, in this case we will include
// ReactiveFormsModule
import:[ ...
ReactiveFormsModule,
...
```
-------------
Additional directives that can be used in reactive forms
//formGroup, formControlName, formControl, formGroupname, formArrayName
```html
<!-- //Binding the reactive forms to the template
//app.component.html 

//Reactive Form doesn't support two-way data binding - remove ngModel

//bind using property grouping -->
<form ngNativeValidate (ngSubmit)="save(form)" [formGroup]="customForm">  
 <div class="form-group">
 <!-- //include formcontrol which will provide the form value to component -->
   <input class="form-control" type="text" formControl="name"><br>
 <div>
 <button>Click here</button>
 </form>
 ```

##### Apply validation on Reactive forms
When there is no validation and the above input field, error "Expected validator return Promise or Obeservable" will be displayed.
```js
//app.component.ts
import { FormGroup, FormControl } from '@angular/forms'

export class AppComponent implement OnInit {

customForm: FormGroup;
Userdata = new Userdata();

constructor(private formBuilder: FormBuilder){}

ngOnInit(){
  this.customForm = this.formBuilder.group({
  //array of array with parameters
  name: ['',[Validators.required, Validators.maxlength(3)]] ,
  age: ['']
 });
 }
 
 save(){
   console.log(this.customForm);
 }
}
```
SetValidator and ClearValidator performed in the input fields
```
control.setValidator(Validator.required);
control.clearValidator(Validator.maxlength(8)):
```

##### Reactive form custom validation

Validator is a function.
```js
//app.component.ts
import ....

//Custom validation snippet
// the function will take a parameter AbstractControl
//if anything wrong with the input field perform key, else return null
/*
function myCustomValidator(ac :AbstractControl):{[key : string] :boolean }| null {

if(//input is null ){
 return {'customValidator': true};
}
  return null;
}
*/
//Above function is the structure actual code is below
function myCustomValidator(ac :AbstractControl):{[key : string] :boolean }| null {

  if(ac != null ){
   return {'scaleValidator': true};
  }
  return null;
}
export ....
ngOnInit(){
  this.customForm = this.formBuilder.group({
  //array of array with parameters
  name: ['',[Validators.required, Validators.maxlength(3)]] ,
  scale: ['',myCustomValidator] // represents the custom funtion
 });
 }
... 
```
------------
 Include the message in the template
```html
<!-- //app.component.html -->
<form ngNativeValidate (ngSubmit)="save(form)" [formGroup]="customForm">  
 <div class="form-group">
    <input class="form-control" type="text" formControlName="name"><br>
    
    <!-- //created a input field to apply custom validation -->
    <input id="scale" class="form-control" type="number" formControlName="scaling"><br>
 </div>
 <!-- // a span with validator fail case to display a message -->
 <span *ngIf= "customForm.get('scale').errors?.scaleValidator" >Error happened</span>
 
 <button>Click here</button>
 </form>

<!-- //when the input is entered with value the message will displayed. -->
```
-----------
##### Instead of using a hardcoded value for checking the AbstractControl use FactoryValidator function

```js
function myCustomValidator(ac :AbstractControl):{[key : string] :boolean }| null {

  // hard coded check
  if(ac != null ){
   return {'scaleValidator': true};
  }
  return null;
}
```
##### Factory validator function
Function returns another Validator function, which will help pass two validation condition.

```js
function myValidator(min : number, max : number){

//function will be returned
return (ac :AbstractControl):{[key : string] :boolean }| null => {

    if(ac != null && (isNaN(ac.value) || ac.value < <min> || ac.value > <max>) ){
     return {'scaleValidator': true};
    }
    return null;
  };
}

export ....
ngOnInit(){
  this.customForm = this.formBuilder.group({
  //array of array with parameters
  name: ['',[Validators.required, Validators.maxlength(3)]] ,
  scale: ['',myValidator(1,12)] // represents the custom funtion
 });
 }
```
##### Cross form validation  can be performed
 In the myValidator function get the values of the form and perfom comparison and validated.

##### Custom Validation in Reactive forms `setValue` and `patchValue`
 
 If we need to populate the form with pre-defined or default value we can achive this with `setValue` and `patchValue`.
 
 setValue : In case we have more than one control in the form model, we need to update the values of each one, we can use setValue.
 
 patchValue: In case we need to update the set of values, we can use patchValue.

```html
<!-- //app.component.html-->
<form ngNativeValidate (ngSubmit)="save(form)" [formGroup]="customForm">  
 <div class="form-group">
    <input class="form-control" type="text" formControlName="name" required><br>
    <input class="form-control" type="number" formControlName="age" required><br>
 </div>
 
 <!-- //upon click of the button, the loadData() function will invoke -->
 <button class="btn btn-success" (click)="loadData()" > Load data</button>
  <button class="btn btn-primary">Click here</button>
 </form>

```
--------------
```js
//app.component.ts
...
constructor (private formBuilder : FormBuilder){}
ngOnInit(){
  this.customForm = this.formBuilder.group({
    name: ['', [Validators.required]],
    age: ['', Validator.required]
  });
}
loadData(){
 this.customForm.setValue({
   name: "Name1", //value will be loaded on click
   age: "20"
 })
}

// for usage Passage value witin the same above ts
...
loadData(){
 this.customForm.patchValue({
   name: "Name", //value will be loaded on click
 })
```

### Modules and organizing module

Module 
   - is a class decorated with `@NgModule` decorator
   - where the component and directives are declared
   - Modules can be loaded egarly and lazily.

Structure of modules:
```js
//app.mdoule.ts
....
@NgModule({
declaration: [...
],
imports: [....
],
providers : [], // contains all the service used in the application
bootstrap :[AppComponent]
})
export class AppModule {} // this is used to export this module info, to another module
```
##### bootstrap array 
   - is the `entry point` of the application, is the `root component`.
   - it should be used in one module, not to be used in all other modules within the application.

##### declaration array
   - one components should be used in one module, unless it is shared using export/import.
   - use export to expose the components, use import in another component.
   - don't `export the services`, since the services are already accessible to other modules and components.

##### imports array
   - can access all the modules that are exported, and the components, template within that modules.
   - In case of importing `FormsModule` only that specific funtionality can be accessed.
    
##### Feature and shared modules

##### Feature modules
Seperate the features to different modules, like userinfo, etc.

```
 $ ng generate module <module-name> -m app
 
 // -m is used to create the module within the app directory
 
 //somemodule.module.ts
 ...
 
 @NgModule({
 decelrations: [],
 imports :[
   CommonModule,
   FormsModule,
   RouterModule.forChild([  //child routes
   { path :''}
  }]
 ...
```
##### Shared Module:

```
$ ng g m <module-name> -m app/module1
// - m creates within the app/module1 directory
```
```js
import the created module that you wanted to share in that specific module. (import array)

// new module created and sharing the component
// module1.module.ts
....
@NgModule({
 delarations : [
    SomeComponent
    ],
    imports :[
     CommonModule, FormsModule // this import formsModule can be accessed by importing module
     ],
     exports: [  // export will include the component of this module in order to be accessed by shared modules
      SomeComponent
      ]
    ...
```

###### Enabling Ivy
To implement ivy, 
   1.  For exisitng appluication, edit the ts.config file to include the ivy
   2.  Using CLI use --enable-ivy
      `$ ng new <application-name> --enable-ivy`

###### Lazy Loading of routes: Using `loadChildren`
 - If a feature module was NOT be implemented for every time. We can avoid loading of such module during start of the application and use only when it is needed.
 ```js
 // Older way in previous version: lazy loading is performed using loadChildren
  { path: 'customer ', loadChildren: './customer/customer.module#CustomerModule'}
  // <path-of-the-module = ./customer/customer.module#CustomerModule>
  //this path is a string which is pron to errors
  
  //Newer way: dynamic import
  {path:'customer' , loadChildren: () => import ('./customer/customer.module'). then(m=>m.CustomerModule)}
  //this is dynamic way, since the IDE will be able to catch in case of spelling mistakes
 ```
 
 ##### Differential Loading
   - creation of two bundles, JS version less than Es5 will be a bundle and higher version as a seperate bundle, and the performance is improvised depending on the bundles.
   
 ### Routings:
   - Navigating from one view to another view, done using routing.
   
  Setting Routes steps:
    - 1. Create/Add routes using CLI or manually
    - 2. Configure the application routes
    - 3. Activate the routes
    - 4. use `RouterOutlet` (placing the content over the view template)
 
 ```
 //step #1
 // when creating the application using ng new, the question prompted to add routing
 
 //app-routing.module.ts
  ...
  
  const routes: Routes= [] // route array which contains routing info
 @NgModule({
 imports : [RouterModule.forRoot(routes)],
 exports :[RouteModule]
 })
 export class AppRoutingModule {}
 
 // app.module.ts
 // has the refrence to the routing module in the @NgModule decorator
 ```
 --------------
 ```
 // Step #2:
 // to configure the route, upon clicking one link it should route to another view
 
 // generate a new component using ng g c Customer
 
 //app-routing.module.ts
 ...
 // configure routes in this array
 const routes : Routes= [
 { path :'customer', component: CustomerComponent} // this is the route config
 ]
 ```
 ------------
 ```
 // Step # 3 Activate the route using RouterLink
 //app.component.html  - using the routerLink directive 
 <button class='btn btn-primary' routerLink = "/customer"> click Customer</button>
 
 // when user clicks the button it will navigate to next component.
 ```
 -------------
 ##### Router outlet is a template place holder of new component view
 ```
 // Step #4 - RouterOutlet
 // in the above step 3, if we click on the button the address bar has the routed url but no action happens
 
 // we need RouterOutlet, where the content of the next component comes this is a place holder
 
 // in app.component.html
 <button class='btn btn-primary' routerLink = "/customer"> click Customer</button>
 
 <router-outlet></rotuer-outlet>
 
 //now when the "click customer" is clicked the content of new view will be displayed.
 ```
 
 ###### Notes:
  - in `app-routing.module.ts` the order of the routing impacts the redirection
  - event associated with the Router- NavigationStart, NaviationEnd
  
##### Passing parameter between Routing from one view to another view

   - Passing parameter from URL
   - Passing parameter from Component
   - Reading parameter as `Observable`
   - Reading parameter as `Snapshot`
   
Scenario, when we need to pass the id from component, and fetch the relevant data from another component.

Steps:
   - place holder to hold the data of the id
   - pass parameter from the URL 

```js
// app-routing.module.ts
//route configuration
cosnt routes : Routes = [
{ path: 'home', component : HomeComponent},
{ path: 'search', component : SearchComponent},
{ path: 'view/:_id',component : ViewComponent},
{path: '', redirectTo : 'home', pathMatch: 'full' }
];

// when we need to pass id parameter from view component to search.
// _id is routing parameter 
// now the place holder for the routing paramter is complete.

```
In the above when the "localhost:4200/search" displays some content
upon clicking the link, we pass the \_id view the info

------------
```html
<!-- //app.component.html -->
...
<tr *ngFor= 'let doc of documents'>
.....
<td> <a id="title" [routerLink] = '["/view","doc._id"]'>{{doc?.title}}</td>
...
<!-- // when the user clicks on the <a> link the parameter is passed, doc._id is the parameter -->
```
------
###### Passing router parameter from component class (.ts file)
```
// passing the parameter
this.router.navigate(['/view',this.document.id]);
```

With the passed \_id parameter we need to fetch the corresponding data
 - The \_id value should be fetched and passed to the component which is done by `Activated Route Service`
 Then reading the data
 - using Observable
 - using Snapshot
 
 ```
 // inject the activated route service in side the component
 // then fetch url from the component
 ```
 ```js
 //View.component.ts
...
export class ViewComponent...{
...
// inject the ActivatedRoute
constructor (private dataService: DataService, private route: ActivatedRoute, private router: Router){
...
ngOnInit(){
// using snapshot to retrive the parameter
// when using the snapshot, fetches the initial state of the route parameter
this.route.snapshot.paramMap.get('_id');

//using Observable
// here route parameter changes are monitored and passed
this.route.paramMap.subscribe(params => {
   const paramId = params.get('_id');
   this.getDocument(paramId);
}):
...
}
```

##### Child Routes & Secondary Routes:
  - How to activate and configure 
  
  Child Routes - Display the component template, inside another component template.
  - we have view which is container to another view, and the another view is the child routes
  
```js
//app-routing.module.ts
....
const routes : Routes = [
  {path : 'home', component : HomeComponent },
  {path : 'search', component : SearchComponent},
  // we use children property to use display child route.
  {path : 'view/:_id', component : ViewComponent, children : [
   // when the view/:_id is hit it gets redirected by default to Edit product component
   // in this case the EditComponent is placed in the DisplayComponent
     {path : '', redirectTo: 'edit' , pathMatch :'full'},
     {path : 'edit', component : EditProductComponent, outlet: 'popup'}
];
...
 // use the <router-outlet></router-outlet> in the component template to view the content on appropriate component to display the content
```
------
```html
<!-- view.component.html -->
...
<tr *ngFor=" document of documents">
 ....
 <!- approach 1 to use the Absolute path to activate child on click -->
<a [routerLink] ="['/view',document.id,'edit']" >Edit Product </a>
 ....
 <!- apporach 2 to use the Relative path to activate child on click.
  Angular knows that since this is ViewComponent template, it uses the child 
  route config info. Recommended since any change in the path no need to change here-->
 <a [routerLink] ="['edit']" > Edit Product </a>
 ....
 ```
 -----------
 Activating from the component (ts file)
 ```
 // abosulte path
 this.router.navigate(['/view','this.document.id','/edit']) // view is the parent component
 
 // relative path 
 this.router.navigate(['edit'], {relativeTo: this.route}); // using relativeTo property
 ```
 
 ##### Secondary route or auxilary route or multiple route
 Used to display multiple panels in single view, refer below
 
 ```
 // the component1, 2, 3 are placed on the view 
 // we need to use <router-outlet> of the specific component
    -------------------           -------------------    
    | component1       |          |component2        |
    | <router-outlet>  |          |<router-outlet>   |
    | </router-outlet> |          |</router-outlet>  |
    --------------------          --------------------
    --------------------------------------------------
    |  component3                                    |
    | <router-outlet></router-outlet>                |
    --------------------------------------------------
 ```
 
 ###### How does the \<router-outlet\> placeholder knows which component to display.
  Named Router Outlet
 ```html
 <!-- //app.component.html -->
...
<rotuer-outlet name="popup"> <router-outlet>
<rotuer-outlet> <router-outlet>
...
 ```
 ------
 configure usign `outlet` property
 ```js
 --app-routing.module.ts
 ....
 const routes : Routes =[
   {path : 'first',  component: FirstComponent},
   {path : 'second', component: SecondComponent, outlet : 'popup'} 
   //outlet is the property name is used by the named router-outlet name.
 ];
 --
 ```
 Activate the secondary route
 ```html
<!-- app.component.html-->
...
<button class='btn btn-primary' routerLink="/first">Cick for component1</button>
<!-- acitvate the secondary routes representation -->
<!-- outlets property takes a key value pair, here we are usign popup (name of route)
and passing the path -->
<a [routerLink]="[{outlets: {popup :['second']}}]"> Names router outlet way </a>

<router-outlet name="popup"> <router-outlet>
<router-outlet></router-outlet>
```
To activate from the component class (ts file)
```js
this.router.navigate([{outlets: {popup:['edit']}}]);
```
##### Route gaurds - Securing the route navigation

Scenario to use router gaurd: 
When the user is filling up the form, and wanted to navigate to different screen of application form. The info will be lost and that needs to be alerted to the user.
In this case the application needs a Route Gaurd, where it informs the user that the input data will be lost.

Types of Route Gaurd: (monitoring and securing)
Order is in life-cycle stage sequence of execution:

   - canDeactivate - Guard navigation away from the route leaving the application route
   - canLoad - used for lazy load routes, used in asynchronous routing
   - canActivate - Gaurd navigation to route
   - canActivateChild - Guard navigation to child route
   - resolve - used to resolve things before loading the application. wait for data to be recevied and load the page
   
Creating route gaurd
   - To create manually by creating a service, which will act as route gaurd
   - Using CLI use `$ ng g g <gaurd-name>`, there will be different options
       () canActivate, etc.
  Files will be created:
  <gaurd-name>.gaurd.ts
  <gaurd-name>.gaurd.spec.ts
  
```js
  import { Injectable } from '@angular/core';
  import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
  import { Observable } from 'rxjs';
   
  @Injectable({
    providedIn : 'root'
    })
    
    //implementing canActivte gaurd
    export class SampleGaurdGard implements canActivate{
    
    //when we declare the variable guarded as false
    // this won't allow to navigate to another screen
    gaurded : boolean = false;  //when using this try changing the if condition
    
    //implement method of implemenet interface
    canActivate(route: ActivateRouteSnapshot, state: RouterStateSnapshot):boolean | UrlTree | im...
    if(!this.gaurded){
     return true;
     } else {
       console.log("Cannot activate route");
       }
    }
   ...
 ```
 To implement it we update the app-routing.moduel.ts
 ```js 
 import { NgModule } from '@angular/core';
 ...
 
 //The place to activate the route gaurd
 const routes : Routes = [
   { path : 'first' , component : FirstComponent, canActivate : [SampleGaurdGaurd]}
 ];
 
 @NgModule({
 ...
 })
 
 export class SampleGaurdGaurd ....{
 }
 ```
 
 CanActivateChild gaurd, can be implemented similar to the CanActivate way.
 
 CanDeactivate - This gaurd is called everytime the route changes one path to another.
 This will not work when used opens the different application is closed.
 The basic functionality, is that when the back button is clcked accidently.
 ```js
 export class SampleGaurdGaurd implements CanDeactivate{
 
 guarded: boolean;
 canDeativate (
 component: FirstComponent,
 currentRoute: ActivatedRouteSnapshot,
 currentState : RouteStateSnapshot,
 nextState : RouterStateSnapshot
 ): Observable<boolean|UtlTree|Promise<boolean|UrlTree|boolean|UrlTree{
 return true; // we can also use gaurd check
 }
 }
 
 ```
 ### Services:
  Reusable piece of functionality, that can be shared and used accross different component. We use the service to hold the data, we inject the service in the component which needs that data.
  
  Creating a Service:
      - Manually using directives
      - using CLI
      
Creating Service using CLI
```
$ ng generate service <service-name>
```
Files generated:
  servicename.service.spec.ts
  sercicename.service.ts
  
```js
import {Injectable } from '@angular/core';

// injects the meta data
@Injectable({
 providedIn: 'root'
 })
 export class ServicenameService{
    constructor () {}
    //create the functionality
 }
```  

Creating service manually
  - create a servicename.service.ts file
  - Input the code content `export class ServicenameService { construtor ....`
  - In `app.module.ts`, `providers array` add the service, and import the service.
  
###### Basic ways to injecting service (dependency injection)
Remove the content `providedIn : 'root'` from the `@Injectable` decorator.
Include the service name within modules, provider array.

##### Using the service as a recpie or token in the ts.module.ts (useClass)
In the app.module.ts, providers array as below
```js
 ...
 imports:[
 ..
 providers : [
   {provide : ServicenameService, useClass : ServicenameService} // use the Servicename class when using it
 ...
```

In the service that is implementing this service can implement the service now 
```js
//NewService.service.ts
....
@Injectable ()
export class NewService implements ServicenameService{ //as ServicenameService interface
  constructor () {}
}
```

##### Using the service as a recpie or token in the ts.module.ts (useExisting)
```js
//app.module.ts
...
providers : [ NewService,
  { provided: ServicenameService, useExisting: NewService } //Whenever ServicenameService is requested, the NewService will be executed.
  ],
...
```
##### Using the service as a recpie or token in the ts.module.ts (useValue)
- when using `useValue` the service will not create new instance of service. (single ton)

```js
//app.module.ts
...
providers : [ NewService,
  { provided: ServicenameService, useValue: {
  log : <service>,
  error: <service>
  }
  ],

```

##### How the data is being sent to the component
Basically get the data once, and use it whenever the component is needed
```js
//ServicenameService.js
...
//method gets data
getDocuments(): Observable <Document[]> {
//using htttp service
  return this.http.get('<url>').pipe(tap<Document[]>(data => data));
  }
...
```
How to use it within the component
```js
....
//inject the service
constructor (private servicename : ServicenameService,...)

...
//use the get methods
getDocument(){
  // the data is the variable used in the observable.
  this.servicename.getDocument().subscribe(data => {this.documents = data; });
  }
```
##### Nested Services
Service within another service, service gets the data, and another service using that data

```
//create new service
...
//import the ServicenameService, which has the data
 export class NewDataService{
 
 constructor ({ private newdataservice : ServicenameService}) //just inject the other service where the data is present
...
```

### In-memory web API in angular
  - we can return a hardcoded value
  - have a json file
  - Angular provide mocked backend api
  - using angular web-api (this is not part of @angular/core) this is seperate service
  
Import Angular HttpService
Create data Access service - to get data from the backend
Inject Angular http service to the data access service
Import Observable, etc
Use Http Operations (Get, put, etc)

Get data from in memory
```js
//create a service and import the HttpClientmodule in the 'app.module.ts'
...
import {HttpClientModule} from '@angular/common/http';
..
imports : [
... 
HttpClientModule..
],
```
Create a service using angular CLI
```js
//new.service.ts
import {Observable} from 'rxjs';
...
export class NewService{
//inject the Http service
//the data is handeled asyncronysly and requires Observable to be import it
constructor (private http:HttpClient){
}
```
Installing the In-memory web API as dependency 
```
$ npm install angular-in-memory-web-api --save-dev
// save-dev is used to save it in the package.json of the project
```
Include the In-memory web api in the modules.
```js
//InMemoryBackendService.service.ts
....
//in imports array include the InMemoryWebApiModule
import {InMemoryWebApiModul} from 'angular-in-memory-web-api';

imports : [
AppRoutingModule, FormsModule, ReactiveFormsModule, HttpClientModule,
InMemoryWebApiModule.forRoot(Customer) //Customer class contains the data that to be stored in memory
],
...
//we are using InMemoryWebApiModule.forRoot() - to be used in specific class
```

Class that uses the in-memory 
```js
import {InMemoryBackendService} 

export class CustomerData implements InMemoryBackendService{

createDatastore(){
//Customer class is created already
  const customers : Customer [] = [
  {id:1, fname: 'test', lname :'test', age :10 },
  {id:2, fname: 'test2', lname :'test2', age :10 },
  ...
  ];

```
Service Class that can use the data
```js

import {tap} from 'rsjx';
...
export class DataService{

url='api/customers';
headers = new HttpHeaders().set('Content-Type','application/json').set('Accept','application/json);
httpOptions = {
headers: this.headers
};

consturctor (private http: HttpClient){}

getCustomers():Observable<Customer[]>{
  return this.http.get<Customer[]>(this.url).pipe(tap(data => console.log(data)));
  }
  ...
 }
```
