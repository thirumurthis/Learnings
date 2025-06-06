#### set the below command in the windows powershell profile path `> notepad $PROFILE`, if the file is not available create and save it.


#### to decode
```cmd
Function Base64-Decode {
Write-Output $args
[Test.Encoding]::Utf8.GetString([Convert]::FromBase64String( $args ))
}

Set-Alias -Name base64-d -Value Base64-Decode
```

##### Usage

```
> base64-e hello
aGVsbG8=
```


#### to encode
```cmd

Function Base64-Encode{
   param([string]$args)
   Write-Output $args
   [Convert]::ToBase64String([System.Text.Encoding]::UTF.GetBytes($args))
}
Set-Alias -Name base64-e -Value Base64-Encode
```

##### usage

```
> base64-d aGVsbG8=
hello

```
