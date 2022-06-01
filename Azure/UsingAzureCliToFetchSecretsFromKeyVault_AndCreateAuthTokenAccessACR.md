### Assume we installed the azure CLI in the Linux machine

### How we are going to fetch the token from the Azure key vault using AZ cli

```
# below command will lsit the accounts 
$ az account list -o table

## note the out put will be in table format and the IsDefault flag True indicates the active subscription is that.
```
- First lets set the subscription in the local VM

```
$ az account set -s <subscription_name>
```

- Fetch the secret from the key valult. Lets create a key vault and assume we set few secrets there. 
  - Key vault is a key value pair, where there can be more than one secrets stored, and eithe enable or disable.

```
$ ACR_SP_SCRT = $(az keyvault secret show --vault-name "<Name_of_the_keyvault>" --name "<name-of-the-key-in-keyvault>" --query "value" --output tsv)
## above will output the sceret stored in the keyvault
```
- In order to fetch APP ID 
```
$ ACR_SP_APPID = $(az keyvault secret show --vault-name "<Name_of_the_keyvault>" --name "<name-of-the-key-in-keyvault>" --query "value" --output tsv)
## above will output the sceret stored in the keyvault
```

- Encode the info
```
ACRAUTH = ${echo "$ACR_SP_APPID:$ACR_SP_SCRT" | base64 --wrap 0)
```

- In order to access the ACR (azure container registry) from the GitLab cli we need to create auth token and pass it as variable.
- below is hte format of the auth token
```
{"auths" : { "myacr.azurecr.io" : { "auth" : "$ACRAUT" } } }
```

- Create a variable at the Git project level and add the value under the variable of Gitlab -> settings -> CI/CD -> variables.

