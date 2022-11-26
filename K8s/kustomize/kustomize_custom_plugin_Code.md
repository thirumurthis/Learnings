- main
- project/soruce/kustomizePlugin.go
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
	p := framework.SimpleProcessor{Config: config, Filter: kio.FilterFunc(fn)}

	/*
		Execute is the entrypoint for invoking configuration functions built
		with this framework from code. See framework/command#Build for a
		Cobra-based command-line equivalent. Execute reads a ResourceList
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
	 Build returns a cobra.Command to run a function.

	 The cobra.Command reads the input from STDIN, invokes the provided processor,
	 and then writes the output to STDOUT.

	 The cobra.Command has a boolean `--stack` flag to print stack traces on failure.
	 By default, invoking the returned cobra.Command with arguments triggers "standalone" mode.

	 In this mode: - The first argument must be the name of a file containing the FunctionConfig.
	 - The remaining arguments must be filenames containing input resources for ResourceList.Items.
	 - The argument "-", if present, will cause resources to be read from STDIN as well.
	  The output will be a raw stream of resources (not wrapped in a List type).
	  Example usage: `cat input1.yaml | go run main.go config.yaml input2.yaml input3.yaml -`

	  If mode is `StandaloneDisabled`, all arguments are ignored, and STDIN must contain a
	 Kubernetes List type. To pass a function config in this mode, use a ResourceList as the input.
	 The output will be of the same type as the input (e.g. ResourceList).
	 Example usage: `cat resource_list.yaml | go run main.go`

	 By default, any error returned by the ResourceListProcessor will be printed to STDERR.
	 Set noPrintError to true to suppress this.
	*/
	if runAsCommand {
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

	/*
		In oder to develop the Kustomize plugin locally
		Uncomment the below lines comment out the so code can be
		debugged locally
	*/
	/*
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
		defer inputFile.Close()
	*/
}
```
- test
- project/source/kustomizePlugin_test.go

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
				//fmt.Printf("--- m dump:\n%s\n\n", string(manifest))

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
- project/source/resource.yaml

```yaml
apiVersion: config.kubernetes.io/v1
kind: ResourceList
functionConfig:
  annotationList:
  - target:
       applyToResources:
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

- project/dockerfile

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

```
> go mod init source
> go mod tiny
```
