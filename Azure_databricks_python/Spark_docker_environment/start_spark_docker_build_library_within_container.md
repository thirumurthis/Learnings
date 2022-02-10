### Steps to run the container in local

```
$ az login 
$ az account list -o table
$ az account set -s <subscription>
$ az acr login --name <acr-name>

## navifate to the dockerfile path
$ docker build .

## create a tag for the build
$ docker -t spark-local:v1 build .


## runnning the docker instance, and open an bash terminal
$ docker run  -p 8888:8888 spark-local:v1 /bin/bash

## in order view the list of python package use below command
# pip list

## to check if the pyspark is installed using wihtin the container
# pyspark
```

## Setting up jupyter notebook in the container
Once the docker container is started usign `$ docker run  -p 8888:8888 spark-local:v1 /bin/bash`
Note: 8888 is the port used by Jupyter notebook.

Install jupyter lab libarary
```
$ pip install jupyter
$ pip install jupyterlab
```

### once installed, issue 
```
# - is root prompt
# jupyter lab --allow-root --ip 0.0.0.0 --no-browser
```
#### The above command will display the token info and url 
http://127.0.0.1:8888/lab?token=<token-for-continer>

if the process is started in background using `nohup` then cat to nohup to fetch the token.

-------

### Installing python lib using wheel
 - Install the wheel package
```
pip install wheel
```

### Shell script to create the whl python library

```sh
#!/bin/bash

while getopts ":ctb" input; do
   case "${input}" in 
       c)
          echo "clean"
          rm -rf ./*.egg-info ./build ./dist test-report.xml
          ;;
       t)
          echo "test phase"
          python -m pytest -junit-xml=test-report.xml --disable-warnings
          processRet=$?
          if [ $processRet -ne 0]; then
             echo "Test not succeed"
             exit $processRet
          fi
          ;;
        b)
          echo "build package"
          rm -rf ./*.egg-info ./build ./dist
          set -o allexport
          source ../infonote.env      # CONTENT in this file will be just APP_VERSION=0.0.1
          set +o allexport
          python librarysetup.py -q bdist_wheel  # python file used for building python library
          processRet=$?
          if [ $processRet -ne 0]; then
             echo "Test not succeed"
             exit $processRet
          fi
          ;;
         *)
            echo "invalid argument"
            exit 1
          ;;
     esac
  done
 
 shift $((OPTIND-1))
```
- infonote.env
```
  APP_VERSION=0.0.1
```

- librarysetup.py
```py
from setuptools import setup, find_packages
import os

def get_version():
  try:
    return os.environ['APP_VERSION']
  except KeyError:
    print ("missing env variable APP_VERSION")
    raise exception
    
setup(
    name= "my-custom-lib",
    version = get_version(),
    packages = find_packages()
 )
```

- If for some reason the env variable is not identified, then use `export APP_VERSION=0.0.1` and `issue `pip3 install -e`

Once the library are created under the dist folder, we can use the below command to deploy locally
Note: In order to install the custom library into our local python environment (either virtual python) use below command
make sure to be within the file librarysetup.py location
```
$ pip install -e
or 
$ pip3 install -e
```
