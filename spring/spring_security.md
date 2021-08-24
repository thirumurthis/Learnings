### Spring security:

- `authentication` 
    - providing username and password, the user is authenticated (known as knowledge based authentication)
    - using id card, mobile are possesion based authentication
    - combination is knowledge base and possesion based is multi factor (MFA)
    
- `authorization` 
     - once the authentication is completed, then comes authorization is whether this user can perform action on the application is based on the role.

- `princpal`
     - This is the user is identified by authentication process. Currently logged in user. This contains the user information.
     - This prinicipal is remembered by application at session level, so no need to provide username/password everytime.

- `Granted Authority`
      - after authorization, the set of permission that the user can do is called granted authority. The user can do a action, only if the user has the authority.
      - fine-grain control can be provided.

- `Role` 
     - This is a group of authority. For example, Manager as a role, Developer is a role.


   
   
