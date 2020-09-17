#### Component
 A web page can be viewed as multiple components. Benefits are reusablity, etc.
 In an UI, developer can design header and footer as components, since this can be reused in any page within.
 @Component decorator is used to define a component
 
##### SPA (Single Page Application)
An Web application where all the view are rendered by single or minimal request to the server.
In case of components, the view is rendered upon initial request, so the experience is more responsive

##### MPA (Multi Page Application)
An Web application where for all the click or action the request is submitted to the server. Over time the user experience tends to delayed.

##### Component to Component interacton
  export - if a component needs to be used in another component, use export keyword to the component.
  import - in the component where the external component that needs to be used, use import keyword.
  
  ```
  ng new <project-name>
  ng generate component <component-name>
  ```
  
  ##### Module
  Components can be grouped to form a module.
  Root or parent module, app.module.ts (typescript) file which has the decorator @NgModulde and the metadata bootstrap which indicates 
  this is a root module. This helps the browser to resolve the custom html tag (which refers the component)
   
  
  ##### Building the angluar project.
  
  ```
  ng build
  ```
  
  ###### environment.json
  To define an variable and pass argument which can be used by component.
  environemnt-prod.json (which has the key production : true)
  
  In Microservice world if the backend service is hosted in www.sample-ms-dev.com/api/service, www.sample-ms/api/service
  the envrionment.json can hold the dev environment related url 
  
  when using the build command for production configuration to use the Angular CLI webpack-lite-node server
  ```
  ng serve --configuration=production 
  ```
