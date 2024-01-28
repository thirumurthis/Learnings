
OAuth 2 is an framework:

OAuth2  - has space for extension

 - OpenID connect (OIDC) extension
    - Provides a standard way to request and share profile data
    - Gives us "Sign in with.." on many sites
    - Depends on JSON Web tokens (JWT)
 - JSON Web Token JWT (RFC 7519) extension
    - OAuth doesn't require JWTs, but these are common
    - JWTs are encoded, NOT encrypted
    - Includes iss (issuer), iat (issued at), sub (subject) aud (audience) and exp (expiration)

 - Token Interospection (RFC 7662) extension
    - Examines a token to describe its contents
    - useful for opaque tokens
    - describes if the token is active or not
    - Mandatory if you have Token Revocation.
 - Token Revocation (RFC 7009)
   - In case if the token is compramised, we can revoke it via API
   - Technically optional and practically required
 - Auth Code with PKCE (RFC 8636) extension
   - Useful for protecting client-side flows (mobile, SPA's)
   - Replaces the Implicit grant type

 - Token Exchange (RFC 8693) Extention
    - Introduces an approch for trading or exchanging tokens on behalf of another user or service, aka delegation.
   
All the above extension might not be supported by all the providers. But with the below discovery spec we can obtain info.

  - Authroization Server Metadata (RFC 8414) discovery spec
    - This gives an endpoint to fetch information to see which extension the provider supports.
    - This is also optional.

 ----------------------------------

OAuth 2
  - Delegates the authorization
  - Loosely defined, unstructured tokens, numerous extension
This where the `OpenID Connect` extension helps, this is not alternate to OAuth.
  - Rigidly defined, strictly structured JWTs, many extension

Everything we get from OAuth can be obtained from OpenID Connect.
OAuth2:
   - Access and refresh tokens
   - Authorize and token endpoints
   - Authorization Code, implicit, Resource Owner password and client credentials.

OpenID connect: provdes and lacks
   - ID tokens
   - Userinfo endpoint
   - Lacks client credentials work flow. (this is mostly used for machine to machine flow)

 ----------------

OpenID connect mostly replaces SAML for Single Sign-On

--------------

How OAuths work?

OAuth concept:
   - Resource owner - that mostly you or whoever authenticating eventually authorizing your application
   - Resource server - what you are grating access to (ask the resource owner to acess to). The resource server validates the token and provides access.
   - Grant type - how the application is asking for access, this is the process for request, there are various flow.
   - Scope - what access that applciation is requesting
   - Authorization server - who that application is asking for access (brain of the process of validating the authorization, etc)
   - Token - how the application get that access (assuming the authentication is success, then you get a token). this could be JWT or opaque string.
   - Claims - the token relates to claims, this claims is embedded in the JWT token or token.
--------------

RFC 6749 OAuth Core
- Endpoints
   - /authorize  => this endpoint the end user (resource owner) interacts with to grant permission for the application to access the resource.
                 => could return an authorization code or an access token
   - /token => the application uses this endpoint to trade an authorization code or refresh token for an access token

---------
OpenID Connect Core
- Endpoints
   -   /userinfo  => appliation uses this endpoint to retrieve profile info about the authenticated user.
                  => This returns a spec-defined set of fields, depending on the permission (scope) requested.
-------
RFC 8414 OAuth Authorization server metadata discovery
Discovery endpoint 
  - /.well-known/oauth-authorization-server => application uses to retrive the configuration info for the authorization server
                                            => This returns spec-defined fields.
                                            => optional, check the Provider doc or spec.
----------
Token Introspection
Endpoint:
   - /introspect  => Endpoint used by application to learn more about token:
                  => whether it is active or not (not revoked or expired), additional info such as expiration time, scopes, etc.
-------
Token revocation
Endpoint:
  -  /revoke => Endpoint that application uses to deactivate (invalidate) token(s). Refersh tokens.
-------

## Grant Types:
 - Authorization Code
 - Implicit or Hybrid
 - Resource Owner Password
 - Client Credentials
 - Device Code
 - Authorization Code with PKCE

 Below question help us to choose the grant types:
   - are you authrozing on behalf of a user or service?
   - does the system in question have a web browser available on device of the client?
   - is the application entirely server side, or does it have a client-side component?

Decision tree for choosing grant type

```
                                                              For a user?
                                                               /     \
                                                         yes  /       \  no (used for service) like used for backend to interact
                                                             /         \  use Client Credential Grant type
                                                        Browser
                                                        available?
                                                        /      \
                                                   yes /        \ no
                                                     /           \ use Device Grant type
                                            Server-side only?
                                               /        \
                                          yes /          \ no (if it is client side)
                                            /             \ use Implicit Grant type (Formerly) Now knows as Authorization Code Flow with PKCE
                               Authorization code Flow
  (Occasionally: resource Owner password flow -> not for long term, mostly used for migrations)
```
-----
OAuth Scopes:
  - Scopes are set of permission we requested like read, write, etc. The permission can be compilcated as well.

In GitHub, we GitHub Scopes.
   - repo
   - public_repo
   - repo_deployment
   - repo:invite
   - write:repo_hook

In Google, Google Scopes.
  - Mostly URL based.

In Okta API Scopes
  - uses simple namespace. resource . action. 

Above three approach is valid, we can choose which one to use.

OpenID Connect Scopes:
 - openid (this should be included, followed by any of the below scopes)
 - profile
 - email
 - address
 - phone
check - https://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims

------

Check: https://developers.google.com/oauthplayground/
-----



     
 
