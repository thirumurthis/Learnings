### Installing azure-cli

```

# upgrade the yum packaes
# yum also has option --exclude=<package*>, 
# in my project, we need some packages not to be updated to the latest version
# so we used --exclude=elasticserach*

execut 'yum_upgrade' do
  command 'sudo yum -y update' 
end 

yum_repository 'microsoft-repo' do
  description 'Mcirosoft RPM repo'
  baseurl 'https://packages.microsoft.com/yumrepos/azure-cli'
  gpgkey 'https://pacakges.microsoft.com/keys/microsoft.asc'
  action :create
end

package 'azure-cli'
 
```


### Creating a azure file share and automate the mount to VMs using chef-cookbook.
 - Create a Azure storage blog
 - Create a File share and a directory
 - Create a service prinicipal, perform Role assignment
 - Create a key vault and store the values in Secrets
    - with appid, tenant id and the secret value

To mount the directory `/myproject/data/projectdata`

```yml

# define root directory
rootdir = ::File.join('/myproject','/data')

# get the environment 
env = node.chef_environment

# define storage account name
storageAcntName=<provide-storage-accnt-env-specific-one>
storageAcntNameKey=<provide-storage-key>
vaultName=<name-of-the-vault>

# define the blob storage url
storageurl="//#{storageAcntName}.file.core.windows.net/<filesharename>/<directory-in-fileshare>"

# unmount the existing path myproject/data
# for idompotent execution we check the fstab 

mount 'unmount_dir' do
  device "unmount -l /myproject/data"
  action : unmount
  not_if "grep #{storageurl} /etc/fstab"
end

# create the directory if not exists

directory 'mydata' do
  path: ::File.join(rootdir,'projectdata')
  owner 'demo'
  group 'demo'
  mode '0755'
  action : create
  ignore_failure: true
end

# install the cfis utils 

execute 'cifs-mount-utils' do
  command 'yum install cifs-utils -y'
end

# create the credentials in smbcredentials

directory '/etc/credentials' do
  owner 'root'
  group 'root'
  mode '0755'
  action :create
  ignore_failure true
end

execute 'get_storageAccount_secrets' do
  command "az login -i; echo \"username=#{storageAcntName}\" >> /etc/credentials/#{storageAcntName}.cred; echo \"password=$(az keyvault secret show --vault-name #{vaultName} --name #{storageAcntNamekey} --query value --output tsv)\" >> /etc/credientials/#{storageAcntName}.cred "
  
  not_if "grep #{storageurl} /etc/fstab"
end

# mount the fileshare 

mount 'projectdata' do
  device "//#{storageurl}"
  device_type: device
  fstype 'cifs'
  username #{storageAcntName}
  password " " 
  options ["nofail","vers=3.0","credientials=/etc/credientials/#{storageAcntName}.cred","uid=demo","gid=demo","mfsymlinks"]
  mount_point "#{rootdir}/projectdata"
  action [:mount,:enable]
  not_if "grep #{storageurl}  /etc/fstab"
end
```
