Module contains many component.
  - In module other modules needs to be imported
  - In module other component needs to be delared (in declearation)
  - bootstrap property tells which component is going to be started.

--------------

- Create a new angular project 
```
$ ng new my-app --routing --style=scss 
$ cd my-app
```
- Install the bootstrap package
```
$ npm install bootstrap
```
 - Import the scss file in the `src/styles.scss` file to apply this style globally to all components.
```
   @import "~/bootstrap/scss/bootstrap";
   // note the ~ refers to the /node-modules folder within the my-app folder, where the package are installed.
   // the .scss extension is not included which is relative path
```
- For the project/application lets add header and footers page.
    - For Header lets create new module called core, this is core module. We then add components to this module to include the html content of header.
      - Note: The component will be added to the module automatically by the cli command.
```sh
    $ ng generate module core
    $ ng generate component header --path=src/app/core --module=core --export 
```
   - In the generated `src/app/core/header/header.component.html` add below content
```html 
 <nav class="navbar navbar-expand navbar-light bg-light">
  <div class="container-fluid">
    <a class="navbar-brand">My App</a>
    <ul class="navbar-nav me-auto">
      <li class="nav-item">
        <a class="nav-link">About This Site</a>
      </li>
      <li class="nav-item">
        <a class="nav-link">Contact</a>
      </li>
    </ul>
  </div>
 </nav>
 ```
   - For footer create a new module called shared, this will be the shared between other modules as well.
```
# create module
$ ng generate module shared
$ ng generate component footer --path=src/app/shared --module=shared --export 
```
   - Add a date field to the `footer.component.ts`
```
## add a date variable to generated
currentdate = new Date();
```
  - Include the generated `src/app/shared/footer.component.html` add below content
  - interpolation is used to display the date, also we are using date pipe (inbuilt)
 ```html
<nav class="navbar fixed-bottom navbar-light bg-light">
  <div class="container-fluid">
    <p>Copyright @{{currentDate | date: 'y'}}. All Rights Reserved</p>
  </div>
</nav>
```

- Now impor the `CodeModule` and `SharedModule` to the `app.module.ts`
```
import {CoreModule} from './core/core.module'
import {SharedModule} from './shared/shared.module'
...

imports: [ ...
    CoreModule, SharedModule
]
...
```
