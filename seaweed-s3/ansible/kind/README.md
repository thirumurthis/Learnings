### Installing kind version 

- Pass the latest version
  - Path: /mnt/c/path/ansible
```
ansible-playbook kind-install.yaml --tags kind-install -e "version=v0.32.0"
```