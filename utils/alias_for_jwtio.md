### with jq installed in the system, with the below alias configured in `~/.bashrc` we can pass jwt token and will decode for us
### instead of using the internet we use the alias command

```sh

alias jwtio='jwtDecode(){
jq -R '\'' split(".") | select (length > 0) | .[0],.[1] | @base64d | fromjson '\'' <<< $1 ;
echo; };
jwtDecodeStart(){
  if [[ ( -z "$@" ) ]]; then
     echo "Usage: jwtio <token>";
     echo -e "Used command : \n jq -R '\'' split(".") | select (length > 0) | .[0],.[1] | @base64d | fromjson '\'' <<< <jwt-token>";
  else
     echo "jq -R '\'' split(".") | select (length > 0) | .[0],.[1] | @base64d | fromjson '\'' <<< $1 ";
     jwtDecode "$1" ;
  fi;
}; jwtDecodeStart '
```

##### usage:

```
$ jwtio eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
jq -R ' split(.) | select (length > 0) | .[0],.[1] | @base64d | fromjson ' <<< eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022
}
```
