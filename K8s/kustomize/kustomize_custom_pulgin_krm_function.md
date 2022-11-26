## Develop Kustomize custom plugin with containerized KRM function using Go

- In this blog we will see how to develop custom plugin for Kustomize using containers KRM function. 

- The customized plugin in this blog, will add annotation with specified key and value to set of resources mentioned under the `target:` tag of the `functionCondig:`. The annotation information will be defined in a YAML file and referred in the `kustomization.yaml` configuration using `transformers:` tag. Some of this information might be not clear at this step.


Pre-requisites:

  - Basic understanding of kubernetes and deploying resources using YAML manifest
  - Basic understanding of GO Lang. Installed locally for development. VS Code with Go plugin is easy to configure an use as IDE.
  - Docker desktop installed 
  - Kustomize CLI installed

- To understand what Kustomize is and how to use to mange manifest check my previous blog [link here](https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize)


### How Kustomize plugin works?

  - With containerized KRM function kustomize framework will read the manifest and generate all resource into as Kubernetes resource ResourceList.
 - The ResourceList is further utilized by the framework by passing it to Generators and transformer pipeline finally rendering  the manifest.

  - Kustomize provides `kyaml` framework which is used to parse the ResourceList manifest in Go Language
  
#### Work flow

  - Using `kyaml` Go framework build the custom plugin with necessary logic.
  - Create container image, and push to docker hub or private image registry.
  - Create a transformer yaml file with the configuration required for the plugin logic. The container reference will be provided in this file as annotation.
 - Below is sample transformers file were the image is referenced.

  ```yaml
    apiVersion: transformers.example.co/v1 # any name that is unique
    kind: ValueAnnotator                   # any name to identify as transformer
    metadata:
      name: notImportantHere
      annotations:
        config.kubernetes.io/function: |
          container:
            image: example.docker.com/my-functions/valueannotator:1.0.0  # container image name
    value: 'important-data'
  ```

  - The transformer file should be reference in the `kustomization.yaml` configuration using `transformer:` tag
  - The Kustomize framework will use this file and send it as stdin to the container, the output manifest will be updated with the logic defined in containre code.

#### Kustomize KRM function

- The [Kustomize documentation](https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize) explains an example on adding annotation value to resources. This blog uses the same approach but shows how to debug the code during development with a test case.

Kustomize provides a kyaml framework in GO language, with which we can build the custom plugin.

![image](https://user-images.githubusercontent.com/6425536/204107814-84c8d9fa-b41d-4f3f-bd64-c5a014eee065.png)


#### Plugin development:

  - For local development we use a sample YAML which defines Kubernetes ResourceList type.

  - Since the kustomize framework uses the data under the `functionConfig:` tag of the ResourseList resource as input to the container in stdin, we define the inputs to which set of resources the annotation value needs to be added.
  - In the below yaml, we have defined the annotation value `holder: sample-io/add` this will be added to ConfigMap and Service resources only, not to `kind: Deployment` listed in the `items` tag

```yaml
apiVersion: config.kubernetes.io/v1
kind: ResourceList
functionConfig:
  annotationList:
  - target:
       applyAnnotationTo:
        - ConfigMap
        - Service
    key: holder
    value: sample-io/add
items:
 - kind: ConfigMap
   apiVersion: v1
   metadata:
      name: webconfig
      namespace: demo
   data:
      key: sample
 - apiVersion: v1
   kind: Service
   metadata:
      name: websvc
      namespace: demo
   spec:
      selector:
        app: web
      type: ClusterIP
      ports:
      - name: svcport
        protocol: TCP
        port: 80
        targetPort: 5000
 - apiVersion: apps/v1
   kind: Deployment
   metadata:
      name: fe-deployment
      labels:
        app: web
   spec:
      replicas: 1
      selector:
        matchLabels:
          app: web
      template:
        metadata:
          labels:
            app: web
        spec:
          containers:
          - name: web
            image: nginx:1.14.2
            ports:
            - containerPort: 80
```

### Go project setup

- To initialize Go workspace from scratch, create a folder and issue `go mod init` command. This will create `go.mod` and `go.sum` files.
- In below example I created a folder named `project` and the source code under folder named `source`.

```
# from the project directory issued below command
# for my project structure
> go mod init source 
> go mod tiny
```

- For developing test case installed testing and yaml package, for which issued below command

```
# from the project directory
> go get gopkg.in/yaml.v3
> go get github.com/stretchr/testify/assert
> go mod tidy
```

- Code Logic

  - The `struct` type defined in the code represents information under the `functionConfig` property. 
  - The kyaml framework parse and injects the YAML data in the function argument that is passed to the Filter function. Our core logic should defined in this function and passed to the processor.
  - With the list of items passed by the kyaml framework, with the information under the `functionConfig` we filter the matching resource and add the annotation with the specified key and value.


### Code 

> **Note:-**
> - The code main() function has `runAsCommand` with true will execute the code  block that can generates Dockerfile automatically with the command `go run kustomizePlugin.go gen .`
> - Below code can generate the Dockerfile for us
> ```
>   cmd := command.Build(p, command.StandaloneDisabled, false)
>   //automatically generates the Dockerfile for us
>   command.AddGenerateDockerfile(cmd)
>   if err := cmd.Execute(); err != nil {
>		os.Exit(1)
>   }
> ```
> - `framework.Execute()` takes reader and writer object and the the ResourceList YAML file can be read and passed as input. Make development easy
> ```
>     if error := framework.Execute(p, byteReadWriter); error != nil {
>      panic(error)
>    }
> ```
>

- Code with the custom plugin logic, save this file as `kustomizePlugin.go`

```go
package main

import (
	"fmt"
	"os"

	"sigs.k8s.io/kustomize/kyaml/fn/framework"
	"sigs.k8s.io/kustomize/kyaml/fn/framework/command"
	"sigs.k8s.io/kustomize/kyaml/kio"
	"sigs.k8s.io/kustomize/kyaml/yaml"
)

type AnnotationConfig struct {
	Target framework.Selector `yaml:"target,omitempty"`
	Key    string             `yaml:"key,omitempty"`
	Value  string             `yaml:"value,omitempty"`
}

type AnnotationConfigs struct {
	AnnotationList []AnnotationConfig `yaml:"annotationList,omitempty"`
}

func process(byteReadWriter *kio.ByteReadWriter, runAsCommand bool) {
	config := new(AnnotationConfigs)

	//function that will be passed to the kustomize framework execute function
	fn := func(items []*yaml.RNode) ([]*yaml.RNode, error) {
    
		// string data type null check
		if config.AnnotationList == nil || len(config.AnnotationList) == 0 {
			return nil, fmt.Errorf("value is required and can't be empty")
		}

		for _, annotationConfig := range config.AnnotationList {

			filterItemsByResourceType, err := annotationConfig.Target.Filter(items)
			if err != nil {
				return nil, err
			}
			// the range returns index, actual object
			// PipeE function is from the kyaml framework
			for _, filterResource := range filterItemsByResourceType {
				err := filterResource.PipeE(yaml.SetAnnotation(annotationConfig.Key, annotationConfig.Value))
				if err != nil {
					return nil, err
				}
			}
		}
		return items, nil
	}
    // create a processor using the which will hold the actual logic of filtering the
    // resources in the -items of the resourceList and creates annotation to matching 
    // resource defined in the functionConfig section
	p := framework.SimpleProcessor{Config: config, Filter: kio.FilterFunc(fn)}

	/*
		framework.Execute() is the entrypoint for invoking configuration functions built
		with this framework from code. Execute reads a ResourceList
		from the given source, passes it to a ResourceListProcessor,
		and then writes the result to the target.
		STDIN and STDOUT will be used if no reader or writer respectively is provided.
	*/
  if !runAsCommand {
    if error := framework.Execute(p, byteReadWriter); error != nil {
      panic(error)
    }
  }    
    /*
      With the below code it would be difficult to debug the code in local (IMO)
      We can pass in the input directly, with execute we can pass the reader and writer
      So I have used a flag to NOT run this block of code 
      But the kustomize documentation demonstrates this approach
    */
	if runAsCommand {
        /*
         command.Build() returns a cobra.Command to run a function.
         The cobra.Command reads the input from STDIN, invokes the provided processor,
         and then writes the output to STDOUT.
        */
		cmd := command.Build(p, command.StandaloneDisabled, false)
		//automatically generates the Dockerfile for us
		command.AddGenerateDockerfile(cmd)
		if err := cmd.Execute(); err != nil {
			os.Exit(1)
		}
	}
}

func check(e error) {
	if e != nil {
		fmt.Print(e)
		panic(e)
	}
}

func main() {

	runAsCommand := false
	byteReadWriter := &kio.ByteReadWriter{}
	process(byteReadWriter, runAsCommand)
}
```

- Simple Test case to validate the output

```go
package main

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"gopkg.in/yaml.v3"
	"sigs.k8s.io/kustomize/kyaml/kio"
)

/*
to install yaml package
go get gopkg.in/yaml.v3
go get github.com/stretchr/testify/assert
go mod tidy
*/
func TestAnnotationPlugin(t *testing.T) {

	runAsCommand := false
	inputFile, err := os.Open("resources.yaml")
	outputFile := bytes.Buffer{}
	check(err)
	fmt.Print("reading file..")

	byteReadWriter := &kio.ByteReadWriter{
		Reader: inputFile,
		Writer: &outputFile,
	}
	process(byteReadWriter, runAsCommand)

	outputAsStr := strings.TrimSpace(outputFile.String())
	fmt.Println(outputAsStr)

	readYamlStr := make(map[interface{}]interface{})

	err2 := yaml.Unmarshal([]byte(outputAsStr), &readYamlStr)
	if err2 != nil {
		log.Fatal(err2)
	}

	substr := "holder: sample-io/add"
	configMap := "ConfigMap"
	service := "Service"
	deployment := "Deployment"
    
    // initialize a map
	output := make(map[string]bool)
	output[configMap] = false
	output[service] = false
	output[deployment] = false
    
	for k, v := range readYamlStr {

		if k == "items" {
			for _, value := range v.([]interface{}) {
				manifest, err := yaml.Marshal(&value)
				if err != nil {
					log.Fatalf("error: %v", err)
				}
				//fmt.Printf("---:\n%s\n\n", string(manifest))

				if strings.Contains(string(manifest), substr) &&
					(strings.Contains(string(manifest), service) ||
						strings.Contains(string(manifest), configMap)) {
					if strings.Contains(string(manifest), configMap) {
						output[configMap] = true
					}
					if strings.Contains(string(manifest), service) {
						output[service] = true
					}
				}
			}
		}
	}
	assert.True(t, output[service])
	assert.True(t, output[configMap])
	assert.False(t, output[deployment])
	defer inputFile.Close()
}
```

### Run Go program

- Use below command to run the Go code, which will also generate the Dockerfile only when the function `main()` `runAsCommand` is set to true.

  ```
  > go run kustomizePlugin.go gen .
  ```

- Dockerfile generated and updated to create the image


> ** Additional note :-**
>
> - Move the Dockerfile generated under `source` folder to `project` folder. 
> - The `go.mod` and `go.sum` is under the `project` directory and these files  are move to the image during creation process
> - Update the Docker `COPY` command to copy only the file `source/kustomizePlugin.go`
> 

```
FROM golang:1.19-alpine as builder
ENV CGO_ENABLED=0
WORKDIR /go/src/
COPY go.mod go.sum /go/src/
RUN go mod download

# update the path where the kustmizePlugin.go code is present
COPY ./source/kustomizePlugin.go .
RUN go build -ldflags '-w -s' -v -o /usr/local/bin/function ./

FROM alpine:latest
COPY --from=builder /usr/local/bin/function /usr/local/bin/function
ENTRYPOINT ["function"]
```

- Docker command to generate the image, in below example it mage is only pushed to local Docker Desktop

```
> docker build -t kustomize_dev:1.0.0 .
```

### Using the KRM function custom plugin container

- Once the image is created successfully, we can add a `prod` folder under `overlay` check my previous blog [link](https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize)
 
 - `overlay\prod\annotationTransformer.yaml` defining the input to wich resource the annotation should be added. In this case to service only
 
```yaml
apiVersion: transformers.customplugin.co/v1
kind: ResourceAnnotator
metadata:
  name: demo-plugin
  annotations:
    config.kubernetes.io/function: |
      container:
        image:  kustomize_dev:1.0.0
annotationList:
  - target:
       applyToResources:
        - Service
    key: holder
    value: sample-io/add
```
 
 - `overlay\prod\kustomization.yaml` reference the transformer file path
 
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
 - ../../base/

nameSuffix: -prod

# reference the path of the transformer file that uses the container
transformers:
  - annotationTransformer.yaml
```

### Output 

- To render the manifest file for prod with below command
- By default the custom plugins can't be used with kustomize CLI, we need to use `--enable-alpha-plugins` option

```
> kustomize build --enable-alpha-plugins prod\
```

- Rendered prod yaml file

```yaml
apiVersion: v1
data:
  APP_NAME: demo_app
  ENV_CODE: DEV
kind: Secret
metadata:
  annotations:
    holder: sample-io/add
  name: demo-app-secret-prod
type: Opaque
---
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    holder: sample-io/add
  labels:
    app: nginx
  name: demo-app-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - image: nginx:latest
        name: nginx
        ports:
        - containerPort: 80
```

## Bonus
### Basics of Go Language

  - Go Lang program should be defined within main package, function main()  is the entry point
  - Go Lang statement doesn't require any delimiter like semicolon used in Java
       - By including the brackets at end of the function or block of code compiler will be identifying it as function or block. Refer below code example
  - The function name first char uppercase indicates it is public function, lowercase is private function.
  - `struct` is a used to define custom data type in Go
  - The code below also includes a testcase which uses YAML Go library
       - We don't define any data type to unmarshall the generated Kubernetes manifest, we use map of interface and iterate and assert based on the required output.

  ```go
  package main

  // we need to import the package using below syntax,
  // fmt package contains utilities to print to stdout like print
  import(
     "fmt"
  )
  
  // as mentioned above the function main should end with bracket { 
  // else Go will report error
  function main(){

    // alternate use of below code is var output = 0 
    // use := to initialize variable with value
    output := 0
    for i := 1; i <= 10; i++ {
        // use = to reassign values
        output = output + i
    }
    // in order to make any function in package to be public
    //the first char shoud be upper case
    fmt.Println("sum :-", output)
    fmt.Printf("sum:- %d", output)  
   }
  ```
