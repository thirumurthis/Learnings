
- Execute below command in Linux, where Azure cli is setup and working.
- 
```
$ sp_cert_name=<name-for-cert-in-service-principal>
$ NEW_SP_CERT=$(az ad sp credential reset --name <service-principal-name> --end-date $(date '+%Y-%m-%d' -d "+180 days") --append --credential-description $sp_cert_name --query password -o tsv);

$ echo $NEW_SP_CERT

$ az keyvault secret set --name <name-of-the-SECRET> --vault-name <name-of-key-valut> --value $NEW_SP_SECRET

```
