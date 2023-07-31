## Project creation

- Create a directory for the project, say `demo`
- Within that directory create another directory, `design-pattern`
- From the directory `demo` from a terminal, issue `git mode init design-pattern`

- Create a file called `main.go` under the `design-pattern` folder.
- Below are different patterns where each will be invoked from `main.go`

# Builder pattern (BuilderPattern.go)
```go
package main

import (
	"fmt"
	"net/http"
	"strings"
)

type RequestBuilder struct {
	Method string
	URL    string
	Header http.Header
	Body   string
}

func NewRequestBuilder() *RequestBuilder {
	return &RequestBuilder{
		Header: make(http.Header),
	}
}

func (b *RequestBuilder) SetMethod(method string) *RequestBuilder {
	b.Method = strings.ToUpper(method)
	return b
}

func (b *RequestBuilder) SetURL(url string) *RequestBuilder {
	b.URL = url
	return b
}

func (b *RequestBuilder) SetHeader(key, value string) *RequestBuilder {
	b.Header.Add(key, value)
	return b
}

func (b *RequestBuilder) SetBody(body string) *RequestBuilder {
	b.Body = body
	return b
}

func (b *RequestBuilder) Build() (*http.Request, error) {
	req, err := http.NewRequest(b.Method, b.URL, strings.NewReader(b.Body))
	if err != nil {
		return nil, err
	}

	req.Header = b.Header

	return req, nil
}

func mainForBuilder() {
	builder := NewRequestBuilder().
		SetMethod("POST").
		SetURL("http://example.com").
		SetHeader("Content-Type", "application/json").
		SetBody(`{"key": "value"}`)

	req, err := builder.Build()
	if err != nil {
		panic(err)
	}

	fmt.Printf("%+v\n", req) // This prints the request object
}
```
## FactoryPattern - Logger (FactoryPattern.go)
```go
package main

import (
	"fmt"
	"os"
	"strings"
)

type Logger interface {
	Log(message string)
}

type LogToConsole struct{}

func (cl *LogToConsole) Log(message string) {
	fmt.Println(message)
}

type LogToFile struct {
	file *os.File
}

func (fl *LogToFile) Log(message string) {
	fmt.Fprintln(fl.file, message)
}

type LogFactory struct{}

func (lf *LogFactory) GetLog(logType string) (Logger, error) {
	switch logType {
	case "console":
		return &LogToConsole{}, nil
	case "file":
		file, err := os.Create("log.txt")
		if err != nil {
			return nil, err
		}
		return &LogToFile{file: file}, nil
	default:
		return nil, fmt.Errorf("unsupported logger type")
	}
}

func mainForFactory(loggingType string) {
	logFactory := &LogFactory{}

	if !(strings.EqualFold("Console", loggingType) || strings.EqualFold("file", loggingType)) {

		fmt.Println(loggingType)
		fmt.Println("Console and File are the type supporter")
		return
	}
	logger, err := logFactory.GetLog(strings.ToLower(loggingType))
	if err != nil {
		panic(err)
	}
	msg := fmt.Sprintf("Hello from %s logger!", strings.ToLower(loggingType))
	logger.Log(msg)
}

//Below is where we reuse the variables log in each if block
func mainForFactoryWithIf(logType string) {
	loggerFactory := &LogFactory{}

	loggingType := strings.ToLower(logType)
	if loggingType == "console" {
		log, err := loggerFactory.GetLog("console")
		if err != nil {
			panic(err)
		}
		log.Log("Hello from console logger!")
	}

	if loggingType == "file" {
		log, err := loggerFactory.GetLog("file")
		if err != nil {
			panic(err)
		}
		log.Log("Hello from file logger!")
	}
}
```

## FactoryPattern - Calculate add and substract (FactoryPattern2.go)
```go
package main

import (
	"fmt"
	"strings"
)

type OperationFactory struct{}

type Operate interface {
	Operate(val1 int32, val2 int32) int32
}
type Add struct{}

type Substract struct{}

func (add *Add) Operate(val1 int32, val2 int32) int32 {
	return val1 + val2
}

func (sub *Substract) Operate(val1 int32, val2 int32) int32 {
	return val1 - val2
}

func (calc *OperationFactory) Operate(operation string) (Operate, error) {
	switch operation {
	case "add":
		return &Add{}, nil
	case "substract":
		return &Substract{}, nil
	default:
		return nil, fmt.Errorf("unsupported operation")
	}
}

func mainForCalcuate(operation string, val1 int32, val2 int32) {

	operationFactory := &OperationFactory{}

	op := strings.ToLower(operation)

	fmt.Printf("inside mainForCalcuate - %s %d %d\n", operation, val1, val2)
	if !(op == "add" || op == "substract") {

		fmt.Println("add and substract are the type supporter")
		return
	}

	if op == "add" {
		calc, err := operationFactory.Operate(op)
		if err != nil {
			panic(err)
		}
		fmt.Printf("%d %s %d = %d", val1, op, val2, calc.Operate(val1, val2))
	}

	if op == "substract" {
		calc, err := operationFactory.Operate(op)
		if err != nil {
			panic(err)
		}
		fmt.Printf("%d %s %d = %d", val1, op, val2, calc.Operate(val1, val2))
	}
}
```

## Adapter Pattern (AdapterPattern.go)
```go
package main

import "fmt"

type LegacyLogger struct{}

func (l *LegacyLogger) LogMessage(message string) {
	fmt.Println("Legacy logger: " + message)
}

type NewLogger interface {
	Log(message string)
}

type NewSystemLogger struct{}

func (l *NewSystemLogger) Log(message string) {
	fmt.Println("New system logger: " + message)
}

type LoggerAdapter struct {
	legacyLogger *LegacyLogger
}

func (a *LoggerAdapter) Log(message string) {
	// Adapting the interface here:
	a.legacyLogger.LogMessage(message)
}

func logMessage(message string, logger NewLogger) {
	logger.Log(message)
}

func mainForAdapter() {
	legacyLogger := &LegacyLogger{}
	loggerAdapter := &LoggerAdapter{legacyLogger: legacyLogger}
	newLogger := &NewSystemLogger{}

	logMessage("Hello, world!", loggerAdapter)
	logMessage("Hello, world!", newLogger)
}
```

## Composite Pattern (CompositePattern.go)

```go
package main

import "fmt"

// FileSystemNode : a component which can be a file or a directory
type FileSystemNode interface {
	GetSize() int
}

// File : a leaf node in the file system
type File struct {
	size int
}

// GetSize : returns size of the file
func (f *File) GetSize() int {
	return f.size
}

// Directory : a composite node in the file system
type Directory struct {
	children []FileSystemNode
}

// GetSize : returns total size of the directory (including all children)
func (d *Directory) GetSize() int {
	size := 0
	for _, child := range d.children {
		size += child.GetSize()
	}
	return size
}

// AddChild : adds a new child node to the directory
func (d *Directory) AddChild(child FileSystemNode) {
	d.children = append(d.children, child)
}

func mainForComposite() {
	file1 := &File{size: 200}
	file2 := &File{size: 300}
	dir := Directory{}
	dir.AddChild(file1)
	dir.AddChild(file2)

	fmt.Println(dir.GetSize()) // Output: 500
}
```

## main.go
```go
package main

import (
	"fmt"
	"os"
	"strconv"
)

// To run the file, just use `go run .` from the go-dp-app/ folder
func main() {
	//Below fetch all the command line arguments
	//argsWithProg := os.Args
	cmdArgProvided := os.Args[1]

	fmt.Println(cmdArgProvided)

	if cmdArgProvided == "Builder" {
		mainForBuilder()
	}
	if cmdArgProvided == "Factory" {
		mainForFactory("Console")
	}

	if cmdArgProvided == "Composite" {
		mainForComposite()
	}

	if cmdArgProvided == "Adapter" {
		mainForAdapter()
	}

	if cmdArgProvided == "Calculate" {
		operation := os.Args[2]
		val1 := os.Args[3]
		val2 := os.Args[4]
		intVal1, err := strconv.ParseInt(val1, 10, 32)

		if err != nil {
			panic(err)
		}
		intVal2, err := strconv.ParseInt(val2, 10, 32)
		if err != nil {
			panic(err)
		}
		fmt.Printf("in Main %s %d %d\n", operation, intVal1, intVal2)
		mainForCalcuate(operation, int32(intVal1), int32(intVal2))
	}
}
```

## To run the app
- Navigate to `demo/design-pattern` folder, and issue below command
```
$ go run . Adapter
```

- To execute the FactoryPattern3.go
```
$ go run . Calculate Add 9 9
```

- To execute the BuilderPattern.go
```
$ go run . Builder
```
