### what is packer?
  - packer is a tool that allows to create custom images accross multitude of platforms.
    - we can create custom AWS AMI, custom Azure image, custom vagrant box through packer.
  
##### `Mutable infrastructure`:
  - inorder to deploy a traditional web application    
     - we need a VM (with either OS, Windows or Linux)  
     - over which the configuration like Os updates, patches, repos, firewall, code are applied.
  - The server is mutated with the configuration, whenever possible.
  - This is mangable when there are few server, but for 100's of server we need some tool manage the configuration that where tools like `Ansible`, etc. comes into picture.
  
  - In Mutable infrastucture below are the stages:
``` 
     Develop -> Deploy the server -> Apply configuration
```
 
##### `Immutable infrastructure`:
   - In Immutable infrastructure, 

```
     Develop -> Apply configuration -> deploy the server
```
  - This where the `packer` is used.
 
##### Packer can be used to create custom images as well.
  - With packer the application code, and the configuration code can be used to create image
  ```
                       | custom |
    (application  ) -> |  image | <- ( configuration )
       code            |        |       like, kernel
                                         update, etc
  ```
  
  - So the custom image already has the application code and all the configuration installed. 
  - In this case, we need to just deploy the `custom image`.
  - There is no needs to touch anything after the deploy.
  - This immutable, after deployment the server is not changed.
  
  - What happens if still need changes to the server?
     - Create a new custom image with the configuration.
     - just kill the server, and redeploy the new image.
     - architect the applicatio, for availablity during new image deployment.
         - use of load balancer, rolling up strategy like kubernetes.

##### Installation of `packer` is available for different OS, it is an executable.

  - Packer uses a configuration file in `json` format.
 
  - The different components that make up the packer config.
     - Builder
     - Provisioner
     - Post process
  - refer the documentation of [packer](https://www.youtube.com/watch?v=vOV74gevFgs).

##### Builder:
  
Example code:
    - create a folder
    - within the folder create a json file.
    - `packer` supports lots of platforms like AWS, Azure, Google cloud, Virtual box, Vagrant, etc.

Example: to build custom image using Amazon EC2 instance ami. (creating a image over ebs backed ec2 image)

##### Below example, will just create an ami, there are no special custom updated.
   - The access key info is hard coded here, this can be made secured. like using environment variables, etc.
   
```json
 {
   "builders" : [
    {
      "type" : "amazon-ebs", // type info get from doc
       "access_key" : "<access-id, from AWS>", // to access the image from AWS
       "secret_key" : "<secret key fetched from AWS settings>", //not in the AWS the access info is displayed only once.
       "region" : "<region where the AWS which is closest to you >",
       "ami_name": "name_of_new_ami", // name of the new ami that will be created and to be identified refer docs under ami 
       "source_ami":"<hex value of the image in AWS>",  //under run configuration on the docs. this is base image over which the configuration will be applied.
       "instane_type":"t2.micro", //the packer creates an ami, just deploying the EC2 instance, then adds configuration and finally creates the ami. In order to create the ami, packer will create and delete an EC2 instance.
       "ssh_username" :  "ec2_user" // this is the default user provided by the AWS (in some ubuntu box the user will be ubuntu check AWS docs)
      }
   ]
 }
```
   - To create a AMI navigate to the folder
```
  > packer build example.json
  ### This command will run an instance the AWS, login to AWS and it can be validated. isntance type used here is t2.micro the smallest simple prices instance.
  ### apply the configuration and then create ami
  ### finally deletes the instance.
```
##### Provisioners
    - This is where we can patch kernel.
    - Copy code repo, set up firewall, other customization can be done here.
     - Support for, `Ansible`, `Chef`,`shell`, `puppet`, etc.

   - Create a new folder
   - create a new json file.
   - Example: 
      - to add an nginx over the ubuntu aws ami using shell provisioner. (note pick the ami id (ami-<unique-id-value))

custom.json
```json
{
   "builders" : [
    {
      "type" : "amazon-ebs",
       "access_key" : "<access-id from AWS>",
       "secret_key" : "<secret key from AWS>",
       "region" : "<region closest to you >",
       "ami_name": "name_of_new_ami", 
       "source_ami":"<ami id of ubuntu>",  
       "instane_type":"t2.micro", 
       "ssh_username" :  "ubuntu", 
      }
   ],
   "provisioners" : [
    {
      "type" : "shell",
      "inline" : [
        "sleep 30",  
        "sudo apt update",
        "sudo apt install nginx -y"
      ]
    }
   ]
 }
```
 NOTE: `sleep 30` was provided to avoid a race condition, the ssh server starts immediately though OS still booting up.

```
 > packer build custom.json
```

#### To include a custom html file on the nginx server.
  - we use `File` provisioner to push the configuration to the nginx.
     - the File provisioner doc, requires target and destination.
  - The `nginx/sites-available/default` configuration provides the info where the default html file is present in most case it would be `/var/www/html`

custom-niginx.json  
```json
{
   "builders" : [
    {
      "type" : "amazon-ebs",
       "access_key" : "<access-id from AWS>", 
       "secret_key" : "<secret key from AWS>",
       "region" : "<region closest to you >",
       "ami_name": "name_of_new_ami", 
       "source_ami":"<ami id of ubuntu>",  
       "instane_type":"t2.micro", 
       "ssh_username" :  "ubuntu", 
      }
   ],
   "provisioners" : [
    {
      "type" : "shell",
      "inline" : [
        "sleep 30",  
        "sudo apt update",
        "sudo apt install nginx -y"
      ]
    },
    {
      "type": "file",
      "source" : "index.html", // our code in this html (this file is within the same folder as the json)
      "destination" : "/tmp/" // since html is root level access folder to over come move the file to tmp and then move it
    },
    {
      "type": "shell",
      "inline" : ["sudo cp /tmp/index.html /var/www/html/"]
    }
   ]
 }
```
 - use below command to build the packer image
```
  packer build custom-nginx.json
  # sometime there might be permission denied issue.
  # since the file provisionor has to use sudo user.
  # the file provisioner has a limitation, where the sudo access is not provided.
```
### How to use different cloud provider images to apply the configuration over the base image.
  - For example, 
     - apply the nignix and index.html over the Ubuntu AWS Ami
     - apply the nginx and index.html over the Ubuntu Azure image.
```json
{
   "builders" : [
    {
      "type" : "amazon-ebs",
       "access_key" : "<access-id from AWS>", 
       "secret_key" : "<secret key from AWS>",
       "region" : "<region closest to you >",
       "ami_name": "name_of_new_ami", 
       "source_ami":"<ami id of ubuntu>",  
       "instane_type":"t2.micro", 
       "ssh_username" :  "ubuntu", 
      },
      {
        "type" : "azure-rm",
        "client_secret" : "<secret>",
        "client_id": "<client id>",
        "subscription_id" : "<subscription id>",

        "image_publisher" : "Canonical", //refer packer doc for values
        "image_offer" : "UbuntuServer",
        "image_sku" : "18.04-LTS",
        "loction" : "West US",
        "os_type" : "Linux",
        "managed_image_name": "custom-azure-nginx",
        "mananged_image_resource_group_name": "demo-rg"
      }
   ],
   "provisioners" : [
    {
      "type" : "shell",
      "inline" : [
        "sleep 30",  
        "sudo apt update",
        "sudo apt install nginx -y"
      ]
    }
}
```
Note: To get the azure client_secret, other details
 - Got to portal, search and select the `Azure Active Directory`
    - To get `client id`
      - Select `App Registration`
        - click `New Registration`
        - give a name and rest default (single tenant) and click Register (say `packer-demo` is app registered)
        - upon saving `Application (client) id` will be displayed
    - To get the `secret key`:
        - go to `Certificates & secrets` menu
          - select `New client secret`. 
          - choose the expiration duration 1 year, 2 or never.
          - copy the secret key now.
    - To get subscription id
        - serach subscription, or click subscription
          - get the subscription id.
          
    - In Azure, we need to `provide persmission to packer` in order to create and destroy VM.
      - we need to create a `Role assignment` for the application created.
         - Under subscription, click `Access Control(IAM)` menu 
         - click Add, 
            - under the options select the Role (as owner), 
            - Assign access to (default value: Azure AD, user, group, or service principal)
            - select dropdown, pick the app registered (`packer-demo`)
            - save it.
       - you can create a resource group, where the images to be stored. This is specified in the json `mananged_image_resource_group_name`.

Execute the above json using  `packer build` command.
 - notice the AWS and Azure will setup with the custom image.
 
### `Post-processor`
   - tasks that runs after the image is configuration is applied.
   - we can create manifest file, create vagarant box, etc. refer doc.
custom.json
```json
{
   "builders" : [
    {
      "type" : "amazon-ebs",
       "access_key" : "<access-id from AWS>",
       "secret_key" : "<secret key from AWS>",
       "region" : "<region closest to you >",
       "ami_name": "name_of_new_ami", 
       "source_ami":"<ami id of ubuntu>",  
       "instane_type":"t2.micro", 
       "ssh_username" :  "ubuntu", 
      }
   ],
   "provisioners" : [
    {
      "type" : "shell",
      "inline" : [
        "sleep 30",  
        "sudo apt update",
        "sudo apt install nginx -y"
      ]
    }
   ],
    "post-processor" : [
    {
     "type" : "manifest",
     "output" : "out.json"
    },
    {
      "type" : "vagrant"
    }
    ]
}
```

##### using variables within json file in packer
```json
{
   "variables" : {
     "customname" : "nginx-web"
   },
   "builders" : [
    {
      "type" : "amazon-ebs",
       "access_key" : "<access-id from AWS>",
       "secret_key" : "<secret key from AWS>",
       "region" : "<region closest to you >",
       "ami_name": "{{ user `customname`}}", // the user variable will be used 
       "source_ami":"<ami id of ubuntu>",  
       "instane_type":"t2.micro", 
       "ssh_username" :  "ubuntu", 
      }
   ],
   "provisioners" : [
    {
      "type" : "shell",
      "inline" : [
        "sleep 30",  
        "sudo apt update",
        "sudo apt install nginx -y"
      ]
    }
   ],
    "post-processor" : [
    {
     "type" : "manifest",
     "output" : "{{user `customname`}}.json"  //reused
    }
  ]
}
```
##### The variables can also be passed from the files
```json 
## variables.json
  {
    "customname" : "nginx-web"
  }

### example.json DOESN'T require the variables defintion
```

Pass the variable json in the `packer build` command using ` -var-file` option.

```
> packer build -var-file=variables.json example.json
```

#### Using Environment variables
   - these are avialable only within this machine.

Create environment variables in Windows is 
```
> setx ACCESS_KEY_ENV <value>
```

To verify the new variables, type "env" in serach bar and the in the environment variables, user variable tab.

Create env in Linux machine
```
 $ export ACCESS_KEY_ENV=<value>
 
 ## To verify use 
 $ env | grep ACCESS_KEY_ENV
 $ printenv
```

Now in order to use the env in the example.json.
  - set the env variable to a user defined variables.
  - then use this witin the builder, etc section

```
{
  "variables" : {
    "acccess_key" : "{{env `ACCESS_KEY_ENV`}}
  },
  "builder": [{
    ...
    ... 
    access_key : "{{user `access_key`}}
    ...    
  }
  ]
  ...
}
```  
