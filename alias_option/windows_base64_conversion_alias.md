- To convert the base64 like in bash script we can use below alias

```
# function that takes arg and converts to base64
Function Base64-Conv { [Text.Encoding]::Utf8.GetString([Convert]::FromBase64String( $args )) }

Set-Alias -Name base64conv -Value Base64-Conv
```
