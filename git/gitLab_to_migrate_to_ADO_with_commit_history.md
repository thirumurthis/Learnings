- Clone the existing GitLab repo to local folder.

```
git clone https://gitlab......
```

- check the git remote configuration using 
```
git remote -v
```

- Set the remote configuration to point to new ADO Repo.
```
git remote add upstream https://azure-ado......
```

- Check if the remote configuration is updated using `git remote -v `
- Push the change to mirror commit history to the ADO repository

```
git push --mirror https://azure-ado.....
```
 When prompted, input the credentials (generated using the Azure portal)
 
 Troubleshoot the configuration, to cleare the credential cache from the machine
   - `git config --system --unset credential.helper`
