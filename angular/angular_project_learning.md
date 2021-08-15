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
  # below for angular 8 version
   @import "~/bootstrap/scss/bootstrap";
   // note the ~ refers to the /node-modules folder within the my-app folder, where the package are installed.
   // the .scss extension is not included which is relative path
  
  # for angluar 12 
  @import "../node_modules/bootstrap/scss/bootstrap.scss";
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


-------
 ###### Using Scully to add some content to make this as a blog site.
   - **Note:** Scully neeeds at least one route defined in an angular application to work correctly.
 - Install the Scully library. within the Angular project from command prompt issue below command. This will download and install the Scully npm packages.
```
$ ng add @scullyio/init
```
  - It imports `ScullyLibModule`, into the main Angular application. check `app.module.ts` file for included libraries and imported modules.
  - The command also creates a configuration file `scully.my-app.config.ts`. With 
     - `projectRoot` : path of the source code of main Angular application (src)
     - `projectName` : name of the main Angular application (my-app)
     - `outDir` : output path of scully generated files. **_Note: This path should be different than the Angular CLI outputs. The angular cli output is configured in `angular.json`_**
     - `routes` : contains the route configuration that will be used for accessing blog posts. (Scully will populate automatically) 

###### Initialize blog page
- Scully provides a specific angluar CLI schematic for initaizlizing an Angluar application, such as blog using markdown (.md) file. Use below command to start the configuration

```
$ ng generate @scullyio/init:markdown
## ? what name do you want to use for module? <- type posts 
## ? what slug do you want for the markdown file ? <- hit enter defaults to id
## ? where do you want to store your markdown files? <- type mdfiles
## ? under which route do you want your file to be requested? <- type posts
```
 - Now check the `scully-my-app.config.ts` file, routes will be updated accordingly.
 - The `posts-routing.module.ts` file will be created with routing configuration
    - `path` for the route is set to `:id` and activates PostsComponent. The `:` indicates that `id` is a route parameter defeined in the slug property defined earlier in command input.
    - `posts.component.html` - contains `<scully-content></scully-content>`. This page can be customized except the above native html tag.
  - The installation and configuration is complete.

##### To display the content of the blog in site page (articles route)

- in `articles.component.ts`, import the ScullyRoutesService and inject in constructor and add posts variable. In ngOnInit subscribe to the promise.
```
...ts
import { ScullyRoute, ScullyRoutesService} from '@scullyio/ng-lib';

// import rxjx
import {Subscription} from 'rxjs';
export class ArticlesComponent ...
....
//to store the scully published blogs
posts: ScullyRotes[] = [];

//To defined subscription for unsubscribing
private routeSub : Subscription | undefined;

constructor (private scullyService: ScullyRoutesService){ }
....


ngOnInit() : void{
   this.scullyService.available$.subscribe(posts -> { this.posts = posts.filter(post => post.title); });
}

ngOnDestroy() : void {
   this.routeSub?.unsubscribe();
}
```
- the `available$` property of ScullyRoutesService is an `observable`. To retrive the values, we subscribe to it. The returned posts variable contains available routes of our angular application. _To avoid displaying routes other than those related to blog posts (such as the contact route), we filter ther results from the available$ property._

##### Note: when we subscribe to an observable, we need to unsubscribe from it when our component no longer exits. (otherwise, this may cuse memory leaks in our application)
- Lets reference the post variable in component.html
```
<div class="list-group mt-3">
  <a *ngFor="let post of posts"
    [routerLink]="post.route" class="list-group-item list-group-item-action">
    <div class="d-flex w-100 justify-content-between">
      <h5 class="mb-1">{{post.title}}</h5>
    </div>
    <p class="mb-1">{{post.description}}</p>
  </a>
</div>
```
 - routerLink in the html component, is surrounded by [] called `property binding`.
    - The routerLink directive is bind to the route property of the post `template reference variable`.
    
#### IMPORTATN: Angluar services that provides initialization logic to a component should be called inside the `ngOnInit` method and not in constructor because it is easier to provide mocks about these services when unit testing the component.

- Now we need to publish the scully blogs, build the angular application
```
$ ng build

$ npm run scully   ## Execute this command everytime we have added the blog posts.
## The above command will create scully-route.json file inside src/assests,
## which contains the routes of our angular application

$ npm run scully:serve
## above command will start two web servers: 
### one contains static prerendered version of website built using scully
### another the Angular live version of our application
```
 - Even with the above the blogs will not be listed in the application.
 - Edit the `*.md` file generated under the `mdfiles`, make publish to `true`, remove slugs if exists. With this edit again issue ` $ ng run scully` then `$ ng run scully:serve`.

- To add more blog, in the md file, issue below command and provide inputs when prompted.
```
$ ng generate @scullyio/init:post --name="Angular and Scully"

# ? what's the target folder for this post? <- type mdfiles
```
  - Above command would have created a `angular-and-scully.md` file under `mdfiles` folder.
  - Edit the file, update `publish` from `false` to `true`, add some content in md format.
     ```
      this is test content
      - https://angular.io
     ```
 - Now issue `$ ng run scully` and `$ ng run scully:serve`. The application should be up under `http://localhost:1668`
 - `dist/static` folder will contain prerendered version of our angluar application generated from `npm run scully` command.
 - `ng  build` command builds our application and outputs files in `dist/my-app` folder.
