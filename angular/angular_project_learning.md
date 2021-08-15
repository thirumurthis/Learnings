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
        <a class="nav-link">Site</a>
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
- Add the native component to the `src/app/app.component.html` 

```html
<app-header></app-header>
<router-outlet></router-outlet>
<app-footer></app-footer>
```
#### Note: By convention, group specific features to sperate modules. Angular modules that contains functionality for a specific feature are called feature modules.

- Adding linkable Contact in the header section.
   - We will create a new module, for contact. For linkable contact we use routeLink to route to contact module.
```
$ ng generate module contact
$ ng generate component contact --path=/src/app/contact --module=contact --export --flat
# -- flat - make sure to create the component within the same folder of contact module. (not like the core/header)
```
  - Update the `contact.component.html` with the content that needs to be displayed
```html
<div class="card mx-auto text-center border-light" style="width: 18rem;">
   <div class="card-body">
    <h5 class="card-title">My first Application </h5>
    <p class="card-text">
      Simple project for learning purposes, uses scully (jamstack)
    </p>
    <a href="https://angular.io/" target="_blank" class="card-link">Angular</a>
    <a href="https://scully.io/" target="_blank" class="card-link">Scully</a>
  </div>
</div>
```
   - update routing `app-routing.module.ts` to import the contact component and define route for contact
```
...
import { ContactComponent } from './contact/contact.component';
...
const routes: Route [
 { path : 'contact' , component: ContactComponent }   // Make a Note that there is NO "/" in the path contact. "/" adds different meaning
 ];
...
```
   - Import the ContactModule to the `app.module.ts`, and also add it under imports array
```
import { ContactModule} from './contact/contact.module';
...
imports : [ ...
   ContactModule         # Note : Imports in module require module to import so components in that module can be used.
 ]
```
   - update `src/core/header.component.html` to include routeLink
```html
...
<li class="nav-item">
  <a routerLink="/contact" routerLinkActive="active" class="nav-link">Contact</a>
</li>
...
```
 - IMPORTANT: To make sure the routeLink is add to `core.module.ts`, import the `RouterModule` to the coreModule where header component is added.
```
import { RouterModule } from '@angluar/router';
...
imports: [..., RouterModule ]
...
export class CoreModule { }
```
- To add details or content to the _Site_ link in the header component. refer `src/core/header.component.html`
  - Execute below commend to auto generate the module and routing components 
```
$ ng generate module articles --route=articles --module=app-routing
# --route option defines the url path of the feature that will be updated in the app-routing.module.ts, content like below will be generated
//  const routes: Route [ ...{ path: 'articles', loadChildren: () => import('./articles/articles.module').then(m => m.ArticlesModule) }..]
# --module option indicates the routing module that will define this routing configuration that include and activates articles feature

## the above command creates src/app/articles with routed component and activated by default
## the articels-routing.module.ts files is automatically created with the route configuration information
## the routing info is also included in the main routing module (app-routing.module.ts)
```
  - The `articels-routing.module.ts` imports the RouterModule using `forChild` method to pass the routing configuration to the Angular route.
```
...
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
...
```
  - The `app-routing.module.ts` imports RouteModule using `forRoot` method.
  - **IMPORTANT**: The **`forChild`** method is used in feature modules, whereas the **`forRoot`** method should be used only in the main application module.
```
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
  ...
```
- Update the `src/core/header.component.html` to include the routerLink 
```html
 <a routerLink="/articles" routerLinkActive="active" class="nav-link">Site</a>
```

- With the updates till now `$ ng serve` will now, enabled the Sites link in the header to display the default content. Using Dev tools in Browser, when clicking the Site, the `articles-articles-module.js` file is requested.

- Now in order to display the Site with the `artice.component.html` we need to add default routes to `app-routing.module.ts`
  - `''` in the path is default 
  - `'**'` is wildcard route, which is triggered when the router cannot match a requested URL with a defined route.
```
..
const routes : Routes [..
  { path: '', pathMatch: 'full', redirectTo: 'articles'},
  { path: '**', redirectTo: 'articles'}
  ];
 ...
```
 - **IMPORTANT:** Define most specific routes first and then add any generic ones such as the default wildcard routes. Angular router parses the route configuration in the order that we have defined and follows a first match wins.
 - Adding the above and starting the ng serve opens the application with content from the `articles.component.html`.

