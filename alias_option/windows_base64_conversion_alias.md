- To convert the base64 like in bash script we can use below alias

```
# function that takes arg and converts to base64
# keep the finction name different from the alias just to now have any issues
# also note the alias values is just single -
Function Base64-decode { [Text.Encoding]::Utf8.GetString([Convert]::FromBase64String( $args )) }

Set-Alias -Name base64decode -Value Base64-decode

# function that encodes
Function Base64-encode { 
   param([string]$plainText)
   [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($plainText))
}
Set-Alias -Name base64encode -Value Base64-encode

```

example 

```
base64encode hello
aGVsbG8=
base64decode aGVsbG8=
hello
```
