
#### Angular
##### Decorator

- Decorators are function or high order functon if they accept an argument
- Decorators is prefixed with `@` 
- Angluar classes & properties in them are defined by the decorators.
- In Angular almost everything is a class, byt these decorators transform them into a module, component, service, pipe or directive.
- Few example, `@NgModule`,`@Component`, `@Directive`, `@Pipe`, `@HostListener`,`@HostBinding`, `@Inject`
                `@Input`,`@Output`, `@Viewchildern`, `@ContentChild`, `@Injectable`, etc

```js
// below is @NgModule decorator, with some configuration

@NgModule({
   imports : [...],
   declarations : [....],
   bootstarp: [...],
   providers: [...],
})
//============
// simple component decorator with metadata
@Component({
   selector : '...',
   templateUrls '...'
   styleUrls : [...],   
})
export class AppComponent {
 ...
 }
```
##### Angular Modules
  - cohesibe block of code dedicated to an application domain, workflow, related set of capablities
  - Angular apps are modular
  - Angular has its own modularity system called NgModules
      - Alteast one NgModule class in the root module.
      - conventionally named as  `AppModule`
  - Modules are helps orgonaize an application and extend the cpabilites from external libraries
  - Example, `FormsModule`, `HttpClientModule` and `RouterModule`
  - Third-party libraries, `AngularMaterial`,`Ionic`..

```
## to create a ng module using angular -cli
$ ng g m module-name
- for few cli the folder will be created like module, etc.
```
- Decleration properties: (used for registering)
   - is an array, accpets a set of delcarables to be register on angular module
   - This doesn't accept @NgModule, @Injectable, a non-anular class, Declarable registered on any other Angular module

