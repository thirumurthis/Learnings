### install `nerdctl` CLI by downloading the windows binary from github page.
 - this is compatible with the Docker CLI.

For buidling an simple app and creating images follow below command.

- create a directory and copy the below contents, the go program should file should be named as `main.go`
```go
package main

import(
     "fmt"
     "time"
)

func main() { 
   for { 
     fmt.Println("Hello World!")
    
     time.Sleep(time.Second * 2)
   }
}
```
- store below content as `Dockerfile`
```docker
FROM golang:1.19-rc-alpine as builder
COPY main.go .
RUN go build -o /app main.go

FROM alpine:3.16

CMD ["./app"]
COPY --from=builder /app .
```

- Use ` nerdctl build -t <tag-name> <directory-of-the-dockerfile>

- Output, since the Dockerfile had a missing `.` in copy command it reported an issue.
```
C:\thiru\learn\k8s\k8s_k3d\go_k8s>nerdctl build -t go-main-1 .
[+] Building 0.1s (2/2) FINISHED
 => [internal] load .dockerignore                                                                                               0.1s
 => => transferring context: 2B                                                                                                 0.0s
 => [internal] load build definition from Dockerfile                                                                            0.1s
 => => transferring dockerfile: 185B                                                                                            0.0s
Dockerfile:2
--------------------
   1 |     FROM golang.19-rc-alpine as builder
   2 | >>> COPY main.go
   3 |     RUN go build -o /app main.go
   4 |
--------------------
error: failed to solve: dockerfile parse error on line 2: COPY requires at least two arguments, but only one was provided. Destination could not be determined.
FATA[0000] unrecognized image format
```

