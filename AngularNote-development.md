
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

What is Service?

When we need to make two or more component to communicate with each other using services.

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
  
COMPONENT:
  - building blocks of angular
  - angular comprises of multiple component, which build the application.
  - type of `directives` - components has a template of their own, this is known as self-contained directives. it has its own UI.
  - in general `directives` - when we need to add some specified behaviour to DOM element, we use directive
  - `ng new project <project-name>` will create a component itself.
  
Files created within component (using CLI):
   - \*.html 
   - \*.spec.ts - testing 
   - \*.css - style
   - \*.ts - logic goes here
  `module` all the directives, components are kept, kind of container.
    - app.module.ts - updated with the created component in the `array @NgModule` and `imported the component`
 
 Note: If we don't want the spec.ts file to be created, we can use the option/flag
 ```
 $ ng g c <component-name> --spec=false
 ```
 
 Creating a component manually (without cli)
    - in the app directory, create another component files directly (files extension) or within a folder.
    - SomeComponent.component.ts 
    - SomeComponent.component.html
    - SomeComponent.component.css
 
 In "SomeComponent.component.ts" file, update the below code.
   - create a class
  ```
  -- Create the content of component:
  import { Component } from '@anguar/core';
  //second step - add the decorator @Component the VS code will automatically import the 
  // component from angular package
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
  
  -- Update the Module
   update the @NgModul array with the component class name
   import the somecomponent path
  ```
  
 
    
 
 
    
 
 
 
  
