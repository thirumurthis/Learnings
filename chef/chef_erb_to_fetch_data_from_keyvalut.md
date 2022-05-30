 - In order to read the data from Azure key valut, in the efb file we can use az cli command.
 
 For example, we have an application.properties file.
  - The vm where the chef client executed should already bootstrapped with the az cli installation.
  
 if the appication.properites.erb file is placed within the template.
 - the recipe that invokes this template file will pass the variable.
 
 ```rb
   project_env = node.chef_environment
   # below just replace staging to dev and any -east to es in the environment name
   vaultName = "project-name-#{project_env.downcase.sub("staging","dev").sub("-east","es")}-keyvault
   
   # below is how the configuration from the data bags is read
   config = data_bag_item("project-#{project_env}","congiguration") 
   # accessing the data from the config.json file of databags for an key
   environment = config['envronment']
   
   # when invoking the template using template resource
   
   template "path/to/create/file" do
     user "app"
     group "app"
     source "application.properties.erb"
     variables({
       vault_name : vaultName                 # this value is created in this recipe using the environment, since each env has different key valut
     })
 ```
 
 ```
 <% vname=@vault_name %>
 $ appToken=<% `az keyvault secret show --valut-name="#{vaultname}" --name "name-of-key-in-keyvault" --query "value" --output tsv`.strip %>
 ```
