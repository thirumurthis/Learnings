- To convert the base64 like in bash script we can use below alias

```
# function that takes arg and converts to base64
# keep the finction name different from the alias just to now have any issues
# also note the alias values is just single -
Function Base64-Conv { [Text.Encoding]::Utf8.GetString([Convert]::FromBase64String( $args )) }

Set-Alias -Name base64conv -Value Base64-Conv
```
