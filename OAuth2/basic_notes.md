
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

 
