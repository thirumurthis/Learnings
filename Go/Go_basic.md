Go lang

Every Go code lives inside a package. All the files inside the folder should be in this package.

The package main is a special package that make your code into a compile into an executable and not a library.

Since most of the package are inside other package we need to Import it, in the below code we will be import fmt package.

```
package main

import (
  "fmt"
)

func main() {
    fmt.Println("Hello world")
}
```

The `func` is a keyword for function, this is used to define the function. This gets no foramtting, like python indetnation.

the `main()` is the name of the functon, the function starts with `{` which is the boundary and should be placed 

the line within the function main() calls the println from the fmt package. We always specify the package name when invoking the method in the package. The `Println` function starts with the Captial letter to indicate the function is called from the impacted package.

Every package that is invoked from the external package will start with the upper case.

String in Go are UTF-8 standard. The Go code doesn't have any semicolon, ";".

To setup the enviornment:-
In visual studio code, install the Go plugin.
Next, Press Cntrl + Shift + p, type Go: Install/update tools and select all the options and wait till the plugins

Once the plugins and modules are installed successfully. Then create a folder `go-app`.

Open up the terminal the, `go mod init go-app` in this case i created a folder called `go-app`.

Create the Hello.go with the above content.

In visual Studio code, now use Run -> Run without debugging option.

check the Debug console in the terminal section to view the results.

### using Go Cli

command to compile and run a go program

```
> go run Hello.go
```

To build executable, use below command

```
> go build Hello.go
# in windows it created a exe
> Hello.exe 
Hello world
```
To see diffrent options within go command use

```
> go help
{mod - module maintenance}
{test- run test}
```

Data types:

Integer types in Go
 
 singed :- int, int8, int16, int32, int64
 unsigned :- uint, uint8, uint16, uint32, uint64

Float type in Go:

   float32, float64

```
package main

import (
	"fmt"
)

func main() {

    /* define the data type */
	var x int
	var y int
    
    /* declare the value to variable*/
	x = 1
	y = 2

	/*. %v refers to the value of the variable to be printied
	  %T to print the data type of the variable ..*/
      
	fmt.Printf("x=%v, type of %T\n", x, x)
	fmt.Printf("x=%v, type of %T\n", y, y)

	mean := (x + y) / 2.0

	fmt.Printf("result:- %v, type of %T\n", mean, mean)
    
	var a float32
	var b float32

	a = 10.0
	b = 10.5
	fmt.Printf("a:- %v, type of sum %T\n", a, a)
	fmt.Printf("a:- %v, type of sum %T\n", b, b)
	sum := a + b
	fmt.Printf("sum:- %v, type of sum %T\n", sum, sum)

    /* Conanical variable definition and declration */
    
    c := 12
    
    p1, p2 := 10.2,10.5  /* using this approach will be of same data type we can't use float and int - throws comilation error */
    
    /* we can't use unsed variables in Go program.*/
}
```

### Control statement in Go

if and if-else 

```
    a := 10
	if a > 5 {
		fmt.Println("X is bigger ")
	} else {
        fmt.Println("X is NOT bigger ")
    }

     if a > 8 || a < 12 {
       fmt.Println("X is between 8 and 12 ")
     }
```

if can be used to intialize in the condition as well

```
   a := 10.0
   b := 25.0
   
   if fraction := a/b; fraction > 0.5 {
      fmt.Println("fraction is bigger")
   }
```

#### switch case statement

```
n := 2

switch n {
   case 1: 
     fmt.Println("one ")
   case 2:
     fmt.Println("two ")
   default:
     fmt.Println("default value")
}
```

Switch with conditional:

```
n := 15

switch{
   case n > 20: 
     fmt.Println(" great 20")
   case n > 10 :
     fmt.Println(" great 10")
   default:
     fmt.Println("default value")
}

//outputs great 10
```
#### for loop

```

for i :=0; i< 3; i++ {
   fmt.Println(i)
}
```

### String 

Like in Java, this will be within double quotes.
String is immutable in Go as well.

to print the length or byte, we can use len which is like python

```
title := "this is book title"

fmt.Println(title)
fmt.Println(len(title))
fmt.Printf("title[0] = %v type %T \n", title[0], title[0])

// title[0] = 123 // this will throw exception since string i immutable

## output 
this is book title
18
title[0] = 116 type uint8 
```

##### slice in string  

```
	fmt.Println(title[:4])
	fmt.Println(title[4:])
    
    ## output 
    this
 is book title
```

##### concatenation
 - using + 
```
fmt.Println("TEXT "+ title[4:])

### output 
TEXT  is book title
```

##### Unicode can work , we can also use raw string using `

```
test := `
        This will be for 
        testing the sample
        ...
        `
fmt.Println(test)

```

#### Slice - this is the List or Array in the Go

```
items := []string{"one","two","three"}  //zero indexed like java

fmt.Println(len(items)) // lenght of the array

fmt.Println(items[1]) //prints two

fmt.Println(items[1:]) //prints two

// traditional looping

for i :=0; i< len(items); i++ {
   fmt.Println(items[i])
}

// using range

for i:= range items {
  fmt.Println(items[i])
}

	// using the index and iterated value with range
	for indx, name := range items {
		fmt.Printf("%s at %d \n", name, indx)
	}

	//_ i special variable which can be undefined
	for _, name := range items {
		fmt.Printf("%s\n", name)
	}

	//we can append item to the items

	items = append(items, "four")
	fmt.Println(items)

```
 
### Map

```
	vehicle := map[string]int16{
		"Auto":    3,
		"Bicycle": 2,
		"Car":     4,
		"Lorry":   6, // Needs the last comman for multi-line
	}

	fmt.Println(len(vehicle))

	// to get the value
	fmt.Println(vehicle["Car"])

	fmt.Println(vehicle["Truc"]) // prints 0 since value not exists

	value, exists := vehicle["SUV"]

	if !exists {
		fmt.Println("Not exists") // in this case this will be printed
	} else {
		fmt.Println(value)
	}
## output 

4
4
0
Not exists
```

 - delete the values 
 
 ```
 
 	vehicle := map[string]int16{
		"Auto":    3,
		"Bicycle": 2,
		"Car":     4,
		"Lorry":   6, // Needs the last comman for multi-line
	}

 	delete(vehicle, "Car")

	val, exists := vehicle["Car"]

	if !exists {
		fmt.Println("Not exists")
	} else {
		fmt.Println(val)
	}

### output 
Not exists
```

- iterating 

```

for key := range vehicles {
   fmt.Println(key) //prints the index
}

for key,value := range vehicles {
  fmt.Printf("%s => %d \n",key,value) //prints the key and value
}
```


### Define a function

```
func main (){
  val := add(1,2)
  fmt.Println(val)
}

func add(a int, b int) int {
   return a +b 
}
```

- returning more than one value in function

```
func main () {
   div, mod := divmod(7,2)
   fmt.Printf("div=%d, mod=%d", div, mod)
}
func divmod(a int, b int) (int, int){
  return a /b , a%b
}
```
- passing slice or arrays

```
func main () {
   values := []int{1,2,3,5} // this slice is kind of pointer, so changes will be refleced globally
   
   double(values,2)
   fmt.Println(values) // the output will be [1,2,6,5] only the index 2 will be dobuled
}

func double (values []int , idx int){
   values[idx] *= 2
}
```

- Passing pointer in go like C, but in Go this is much safer

```
func main (){
  val := 100
  doublePtr(&val)
  fmt.Println(val)  // 200
}

func doublePtr(n *int){
    *n *=2
}
```

##### Go function can return more than one values which is used to return errors

```
func main (){

   s1, err := sqrt(4.0)
   
   if err != nil {
      fmt.Printf("ERROR : %s\n", err)
    }else{
      fmt.Println(s1)  
    }
}

//error is a built-in type in go
func sqrt(n float64) (float64, error){

  if n < 0 {
      reutrn 0.0, fmt.Error("negative number passed")
  }
  return math.Sqrt(n),nil
}
```

##### Go has garbage collector so  memory manged
other resources like files, socket, etc needs to be closed 

### Use of defer 

- defer is a deferred function meaning the function with defer keyword will be called only when that function using the defer exists.

- in the below example, the defer will executed only when the worker() function exists
  - S othe Workeer will be printed first then B and A.

```

func main(){
   worker()
}

func worker(){
   r1, err := acqure("A")
   
   if err!= nil {
     fmt.Println("ERROR ",err)
     return
   }
   
   defer release(r1) // use of defer command
   
   r2, err := acqure("B")
   
   if err!= nul {
     fmt.Println("ERROR ",err)
     return
   }
   
   defer release(r2) // use of defer command
   
   fmt.Println("worker")
   
}

func acquire(name string)(string, error){
   return name, nil
}

func release (name string){
   fmt.Printf("Cleaning up %s \n",name)
}

### output  - the defer is called reverse order
Worker
cleaning up B
cleaning up A 
```

- Fetch the header Content-Type from URL from response

```

func main() {
  	ctype, err := contentType("https://linkedin.com")

	if err != nil {
		fmt.Println("Error occurred", err)
	} else {
		fmt.Printf(ctype)
	}
}

func contentType(url string) (string, error) {

	res, err := http.Get(url)

	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	ctype := res.Header.Get("Content-Type")

	if ctype == "" {
		return "", fmt.Errorf("Can't find content type header")
	}
	return ctype, nil
}

# output 
text/html; charset=utf-8
```

#### Struct
 - In most of the case we can use the built-in data type
 - In some case we need custom data type which is where we need struct
 
```
package main

import (
    "fmt"
    "time"
)

type Stock struct {
       Symbol string
       price float32
       updateTime time.Time
}

func main (){
    mfst = Stock{"MFST",250.00, time.Now()}  //we can add time as well .Add(7*24 * time.Hour)
    
    fmt.Println(mfst)

    //To print more details on the stock
    fmt.Printf("%#v\n",mfst)
    
    //to read single field use .
    fmt.Println(mfst.Symbol)
    
    // another option to define struct
    
    intc := Stock{
         Symbol: "INTC",
         price: 33.00, //we didn't set the update time. which is ok
     }
     
     fmt.Printf("%#v\n", intc)
     
     // another option
     
     var ba Stock
     fmt.Printf("%#v\n", ba)
}
``` 

##### How are the private and protected fields defined?
 - In Go every properties that starting with the UpperCase is accessible outside the package.
 - if the variable is starting with lower case then it is only accessible within that package.
 - In go this called, exported and unexported symbols.
 

### Define a method on the struct


```
package main

import (
    "fmt"
    "time"
)

type Stock struct {
       Symbol string
       price float32
       updateTime time.Time
}

func (s Stock) TimeLeft() time.Duration{
  return s.UpdateTime.Sub(time.Now().UTC())
}

// we will change the price value
// so we need to pass the potiner
func (s *Stock) Add(value float){
   s.price += value
}

func main(){
    s := Stock("MFST", 250.0, time.Now())
    fmt.Println(s.TimeLeft())
    s.Add(4.0)
    fmt.Println(s.price)
}
```

#### How to create a constructor in Go

- In Go we write a function usually starting with NewXXXXX which will possibly return the new object value.

```
package main

import (
    "fmt"
    "time"
)

type Stock struct {
       Symbol string
       price float32
       updateTime time.Time
}

//more like a constructor we do validation
func NewStock(sybmol string, price float32, updateTime time.Time) (*Stock, error){
   if Sybmol == "" {
     return nil, fmt.Errorf("empty stock value")
   }
   
   if price < 0 {
      return nil, fmt.Error("price can't be negative")
   }
   
   if updateTime.Before(time.Now()){
      return nil, fmt.Errorf("Invalid time")
   }
   
   s := Stock{
      Symbol : symbol,
      price : price,
      updateTime: updateTime,
   }
   
   return &s, nil //Note reference is returned
}

func main (){
   updateTime := time.Now()
   
   s1, err := NewStock("MFST",250.0,updateTime)
   
   if err != nil {
      fmt.Println("Error occurred", err)
   }else{
      fmt.Printf("%#v\n",s1)
   }
   
   
   
}
```

#### About interfaces in Go

```
package main

import (
    "fmt"
    "math"
)

type Square struct {
   Length float64
}

func (s Square) Area() float64 {
   return s.Length* s.Length
}

func Circle struct {
   Radius float64
}

func (c Circle) Area() float64 {
   return math.Pi * c.Radius * c.Radius
}

func sumAreas(Shapes []Shape) float64{
   total :=0.0
   
   for _,shape := range shapes {
      total += shape.Area()
   }
   
   return total
}

//shape is an interface
// in here the interface contains the set of method that a type should implement
//both circle and square has the Area method

type Shape interface {
  Area() float64
}

func main(){

  s := Square{20}
  fmt.Println(s.Area())
  
  c := Circle{20}
  fmt.Println(c.Area())
  
  shapes := []Shape{s,c}
  
  shapeArea := sumArea(shapes)

}

```

#### GO supports generics as well

```
type Ordered interface {
   int | float64 | string
}

func min[T Ordered](values []T) (T, error){
   if len(values)==0 { // if array length is zero
      var zero T  // if value is zero define a value and return that
      return zero, fmt.Errorf("empty slice")
   }
   
   m := values[0]
    for _,v := range value[1:]{
      if v < m {
         m = v      
      }
    }
}

func main(){
   
    fmt.Println(min([]float64{3,6,9}))
    fmt.Println(min([]string{"B","D","A"}))
}
```
