
```
alias jwtio='jwtDecode(){
local TOKEN_CONTENT=$1;
jq -R '\'' split(".") | select (length >0)| .[0],.[1] | @base64d | fromjson '\'' <<< $1 ;
echo; };
jwtDecodeStart(){
 if [[ ( -z "$@" ) ]]; then
   echo "usage: jwtio <token string>";
   echo -e "command used :\n jq -R '\'' split(".") | select(length > 0) | .[0],.[1] | @base64d | fromjson '\'' <<< <jwt-token>";
 else
   echo "jq -R '\'' split(".")  | select(length > 0)| .[0],.[1] | @base64d | fromjson '\'' <<< $1 ;
   jwtDecode "$1";
fi; ); jwtoDecodeStart '
```
