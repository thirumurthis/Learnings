
I am building CI/CD for Azure Databricks, I have to build a python library and able to create the artifacts part of the ADO pipeline.

Below is the option used for CD, part of the ADO CI/CD.

Create a Release , and select appropriate stages.

The first stage has below plugins

1. `Using python version 3.x`
    - with default selection
2. `Bash #`
    - inline, with below command
```
python -m pip install --upgrade pip setuptools wheel databricks-cli
``` 
3. `Configure Databricks`
 
[Refer Documentation](https://marketplace.visualstudio.com/items?itemName=riserrad.azdo-databricks)

   - configured the Workspace URL and the  Databrick token
 
 Note: 
   When using the bash command instead of the Configure Databricks plugin, like in this [link](https://docs.microsoft.com/en-us/answers/questions/113672/39databricks-configure-token39-command-waiting-for.html) it didn't work for me.
   ```
     databricks configure --token <<EOF
     https://urloftheworkspace
     token
     EOF
   ```
 Also, wihtout the Step 2, pip install the Step 3 didn't work either.
 
4. `Bash #`
 - Since the artifacts are deployed to local agent pool, by the pipeline process.
 - 
  Note: In our case, we had to create a python wheel for custom library and install that part of the Databricks cluster.
  
 - So we used databricks cli

```sh
databricks fs rm -r dbfs:/FileStore/jars/libraries/project-lib/ --profile AZDO
databricks fs cp -r myproject/lib1/dist/ dbfs:/FileStore/jars/libraries/lib1/ --profile AZDO
databricks fs cp -r myproject/lib2/dist/ dbfs:/FileStore/jars/libraries/lib2/ --profile AZDO
# get the list of workspace
workspaces=`databricks workspace ls --profile AZDO`
MYPROJECT_SPACE1=`echo $workspaces | grep -w SPACE1 || true`  # Note: SPACE1 is the folder that i need to deploy the artifacts in workspace

#if the workspace exists then delete it 
if [ ! -z "$MYPROJECT_SPACE1" ]; then 
        echo "To deploy the **** MYPROJECT_SPACE1 ***"
        databricks workspace rm -r //SPACE1 --debug --profile AZDO
fi
databricks workspace import_dir SPACE1/dist // --profile AZDO

# note the jobs/dist was created part of the pipe line in ADO
# the job json configuration is pushed to this location

for jobfile in MYPROJECT/jobs/dist/*.json; do
       echo "Creating  $jobfile"
       # using jq plugin to parse the json 
       jobname=`cat $jobfile | jq -r .name`
       
       # fetch job 
       jobitemlst=`databricks jobs list --profile AZDO`
       # we check if the job is already available, if so then we reset the job
       # else we create a new one. here.
       jobitemlst=`echo "$jobitemlst" | grep $jobname || true`
      if [[ -z $jobitemlst ]]; then
              echo "creating job"
              databricks jobs create --json-file $jobfile --profile AZDO
       else
              while IFS= read -r jobitem; do   #Note the while loop reads the content of the jobitemlst
                     jobstate=($jobinfo)
                     jobid=${jobstate[0]}
                     echo "reset a job: $jobid"
                     databricks jobs reset --job-id "$jobid" --json-file $jobfile --profile AZDO
                done <<< "$jobitemlst"
       fi
 done
```

Note: since i was using the Windows Agent pool, and Base # was using the git bash.
The / is automatically refernced to `C:/Program Files/Git/SPACE1` and causing databrics cli error.
Refer the Stack overflow [link](https://stackoverflow.com/questions/71580686/databricks-cli-in-git-bash-on-windows-returns-error-message-invalid-parameter)


with reference to the [link](https://levelup.gitconnected.com/continuous-integration-and-delivery-in-azure-databricks-1ba56da3db45)
