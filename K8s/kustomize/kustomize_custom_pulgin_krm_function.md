## Custom plugin development with kustomize containerized KRM function with Go Lang

- This blog details custom plugin development, extending the Kustomize framework to with containerized KRM functions.

- The customized plugin developed in this blog, will add speified key value as annotation to the specified list of resource. 

Pre-requsites:

  - Understanding of kubernetes and deploying resources using YAML manifest
  - Basic understanding of GO Lang and installed locally for development. VS Code with Go plugin
  - Docker desktop installed and Kustomize CLI installed
  - Basic understanding on Kustomize usage, for more details check my previous blog [link here]https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize)


## Kustomize KRM function

- The [Kustomize documentation]((https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize) with example is used for adding annotation value.

- This blog will explain GO lang code development for the plugin and how to debug the in local, with sample test case.

### Basic of Go Language

  - Go Lang program should be defined within main package, function main()  is the entry point
  - Go Lang statement doesn't require any delimiter like semicolon used in Java, 
       - so when defining a function or block of code compiler idenbrackets should be at the same statement.
  - Refer below example
  - The function name first char uppercase indicates it is public function, lowercase is private function.
  - `struct` is a used to define custom data type in Go
  - The code below also includes a testcase which uses YAML Go library
       - The map of interface is used since the YAML manifest is not defined as struct data tyoe,  for unmarshalling the Kubernetes manifest rendered as stdout.

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
    // use := to initalize variable with value
    output := 0
    for i := 1; i <= 10; i++ {
        // use = to reassing values
        output = output + i
    }
    // in order to make any function in package to be public
    //the first char shoud be upper case
    fmt.Println("sum :-", output)
    fmt.Printf("sum:- %d", output)  
   }
  ```

### How the Kustomize plugin works?

  - We are using Containerized KRM function to build the custom plugin
  - The kustomize framework will read the resources and generate all into ResourceList kubernetes resource, the ResourceList is further utlizes by the framework passing to Generators and transformer to render the manifest.
  - Kustomize provides KYAML framework to parse the resource Yaml in Go Language
  - Once the Go code is built with the logic we need to create a container image
  - The container image will be used in the transformer Yaml like below
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
  - The transformer Yaml should be reference in the kustomization.yaml configuraton using `transformer:` tag
  - The input values will be passed in the `spec` tab, the Kustomize framework will pass this as stdin to the specified container and transforms it to specified logic

Plugin development:

  - For development we use a sample resource yaml which is Kubernetes ResourceList type.
  - In this case we define the input under `functionConfig:` tag, where we specify use the list to define to which resource the annotation to be applied
  - In the below yaml, the annotation value `holder: sample-io/add` will be added to ConfigMap and Service k8s resources only. The `kind: Deployment` will not include this annotation.

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

- The Go code uses kustomize `kyaml` framework in Go to read the Yaml file

- To setup the Go code from scratch, create a folder and issue `go mod init` command
- I created a folder named project and created a folder called source under it, then issued below command
```
# from the project direcrory 
> go mod init source 
> go mod tiny
```

- In order to write testing and use yaml package, I issued below command
```
# from the project directory
> go get gopkg.in/yaml.v3
> go get github.com/stretchr/testify/assert
> go mod tidy
```

- Below is the Go code with the logic
  - The `struct` type defined reperesents the `functionConfig` property `applyAnnotationTo` which stores the parsed resourceList by the kyaml framework
  - The kyaml framework injects the YAML data from the items tag in the function deinfed
  - For the resources specified in target, applyAnnotationTo the items list will be flitered and for the matching resource in kind we add annotation with key and value
  - To run the code and generate the Dockerfile (saved the below code `kustomizePlugin.go`) enable the `runAsCommand := true`
  - For debuging the code modify `runAsCommand := false`, and use the test case which calls the function
  ```
  > go run kustomizePlugin.go gen .
  ```

> **Info:-**
>
> Copy the Docker file from the `source` folder and move to `project` folder. 
> The `go mod init` creates the `go.mod` and `go.sum` under the `project` directory, and these files needs to be copied to image.
> The `go.mod` files defines the module info, which will be used in the container to build image
> The copy command needs to be updated to copy only the source/kustomizePlugin.go
>

### Code defining the core logic

> **Note:-**
> - The code main() function has `runAsCommand` boolean, when set to true this will execute the code that can generate Docker file
> 


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

- Dockerfile   
 
```
FROM golang:1.19-alpine as builder
ENV CGO_ENABLED=0
WORKDIR /go/src/
COPY go.mod go.sum /go/src/
RUN go mod download
COPY ./source/kustomizePlugin.go .
RUN go build -ldflags '-w -s' -v -o /usr/local/bin/function ./

FROM alpine:latest
COPY --from=builder /usr/local/bin/function /usr/local/bin/function
ENTRYPOINT ["function"]
```
- Docker command to generate the image in docker desktop

```
> docker build -t kustomize_dev:1.0.0 .
```

### Now to build the manifes with the container KRM function
 - With reference to the kustomize project structure defined in the [link](https://thirumurthi.hashnode.dev/manage-kubernetes-manifest-with-kustomize)
 - Create a folder `prod` under `overlay`
 
 - `overlay\prod\annotationTransformer.yaml`
 
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
 
 - `overlay\prod\kustomization.yaml`
 
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
 - ../../base/

nameSuffix: -prod

transformers:
  - annotationTransformer.yaml
```

- To render the mainfest file for prod

```
> kustomize build --enable-alpha-plugins prod\
```

- Output 

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
