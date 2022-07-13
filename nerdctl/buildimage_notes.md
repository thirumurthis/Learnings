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

- Build process using `nerdctl`
   - below is in case of compilation erro

```
nerdctl build -t go-main-1 .
[+] Building 38.9s (10/11)
 => [internal] load .dockerignore                                                                                                                         0.0s
 => => transferring context: 2B                                                                                                                           0.0s
 => [internal] load build definition from Dockerfile                                                                                                      0.0s
 => => transferring dockerfile: 189B                                                                                                                      0.0s
 => [internal] load metadata for docker.io/library/alpine:3.16                                                                                            1.4s
 => [internal] load metadata for docker.io/library/golang:1.19-rc-alpine                                                                                  2.1s
 => [auth] library/golang:pull token for registry-1.docker.io                                                                                             0.0s
 => [internal] load build context                                                                                                                         0.1s
 => => transferring context: 201B                                                                                                                         0.0s
 => [stage-1 1/2] FROM docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c                              0.5s
 => => resolve docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c                                      0.1s
 => => extracting sha256:2408cc74d12b6cd092bb8b516ba7d5e290f485d3eb9672efc00f0583730179e8                                                                 0.4s
 => [builder 1/3] FROM docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696                   34.0s
 => => resolve docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696                            0.1s
 => => sha256:3e71d2990a0d8ee73e6bbcb96520f62e2a0df06902815ad586694e639cea7860 122.27MB / 122.27MB                                                       36.4s
 => => sha256:292a6a0331a24f07f341e5d8150f632313aa9c043bb6f37a652be7f5baca7bcd 156B / 156B                                                               36.4s
 => => sha256:30c4a77219572e69864cd3069b7b47e55202681be151bd7394f9cb6645b5687b 0B / 153B                                                                 36.4s
 => => sha256:ea60b727a1ce729d031ec1e4f2521a246e71bdb72d3fd9c9c04711cce80e1722 0B / 271.82kB                                                             36.4s
 => => extracting sha256:ea60b727a1ce729d031ec1e4f2521a246e71bdb72d3fd9c9c04711cce80e1722                                                                 0.1s
 => => extracting sha256:30c4a77219572e69864cd3069b7b47e55202681be151bd7394f9cb6645b5687b                                                                 0.1s
 => => extracting sha256:3e71d2990a0d8ee73e6bbcb96520f62e2a0df06902815ad586694e639cea7860                                                                10.3s
 => => extracting sha256:292a6a0331a24f07f341e5d8150f632313aa9c043bb6f37a652be7f5baca7bcd                                                                 0.0s
 => [builder 2/3] COPY main.go .                                                                                                                          2.0s
 => ERROR [builder 3/3] RUN go build -o /app main.go                                                                                                      0.7s
------
 > [builder 3/3] RUN go build -o /app main.go:
#0 0.617 # command-line-arguments
#0 0.617 ./main.go:12:32: '_' must separate successive digits
#0 0.617 ./main.go:12:34: syntax error: unexpected newline in argument list; possibly missing comma or )
------
Dockerfile:3
--------------------
   1 |     FROM golang:1.19-rc-alpine as builder
   2 |     COPY main.go .
   3 | >>> RUN go build -o /app main.go
   4 |
   5 |     FROM alpine:3.16
--------------------
error: failed to solve: process "/bin/sh -c go build -o /app main.go" did not complete successfully: exit code: 2
FATA[0039] unrecognized image format
```

- Complete code

```
>nerdctl build -t go-main-1 .
[+] Building 4.1s (13/13) FINISHED
 => [internal] load .dockerignore                                                                                                                         0.0s
 => => transferring context: 2B                                                                                                                           0.0s
 => [internal] load build definition from Dockerfile                                                                                                      0.0s
 => => transferring dockerfile: 189B                                                                                                                      0.0s
 => [internal] load metadata for docker.io/library/alpine:3.16                                                                                            1.4s
 => [internal] load metadata for docker.io/library/golang:1.19-rc-alpine                                                                                  1.4s
 => [auth] library/alpine:pull token for registry-1.docker.io                                                                                             0.0s
 => [auth] library/golang:pull token for registry-1.docker.io                                                                                             0.0s
 => [internal] load build context                                                                                                                         0.0s
 => => transferring context: 201B                                                                                                                         0.0s
 => CACHED [stage-1 1/2] FROM docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c                       0.1s
 => => resolve docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c                                      0.1s
 => CACHED [builder 1/3] FROM docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696             0.1s
 => => resolve docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696                            0.1s
 => [builder 2/3] COPY main.go .                                                                                                                          0.0s
 => [builder 3/3] RUN go build -o /app main.go                                                                                                            0.9s
 => [stage-1 2/2] COPY --from=builder /app .                                                                                                              0.1s
 => exporting to oci image format                                                                                                                         0.9s
 => => exporting layers                                                                                                                                   0.4s
 => => exporting manifest sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9                                                         0.0s
 => => exporting config sha256:acd416cc5361011fbd32a67dc0749bc0f029a031427735b283db1bc0a1c7e831                                                           0.0s
 => => sending tarball                                                                                                                                    0.5s
unpacking docker.io/library/go-main-1:latest (sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9)...done
```

- List the image created

```
C:\thiru\learn\k8s\k8s_k3d\go_k8s>nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED          PLATFORM       SIZE       BLOB SIZE
alpine        latest    686d8c9dfa6f    3 days ago       linux/amd64    5.9 MiB    2.7 MiB
go-main-1     latest    ab83037c8160    7 minutes ago    linux/amd64    7.6 MiB    3.7 MiB
```

- To push the build we need to use below command 

```
C:\thiru\learn\k8s\k8s_k3d\go_k8s>nerdctl push go-main-1:latest
INFO[0000] pushing as a reduced-platform image (application/vnd.docker.distribution.manifest.v2+json, sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9)
manifest-sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9: waiting        |--------------------------------------|
config-sha256:acd416cc5361011fbd32a67dc0749bc0f029a031427735b283db1bc0a1c7e831:   waiting        |--------------------------------------|
elapsed: 1.4 s                                                                    total:   0.0 B (0.0 B/s)
FATA[0001] server message: insufficient_scope: authorization failed
```

- to remove image use

```
> nerdctl rmi <image-sha>
```

- to build image to execute in kubernetes use `--namespace k8s.io`

```
C:\thiru\learn\k8s\k8s_k3d\go_k8s>nerdctl --namespace k8s.io build -t mygoapp:v1.0 .
[+] Building 2.2s (13/13) FINISHED
 => [internal] load build definition from Dockerfile                                                                                      0.0s
 => => transferring dockerfile: 189B                                                                                                      0.0s
 => [internal] load .dockerignore                                                                                                         0.0s
 => => transferring context: 2B                                                                                                           0.0s
 => [internal] load metadata for docker.io/library/alpine:3.16                                                                            1.5s
 => [internal] load metadata for docker.io/library/golang:1.19-rc-alpine                                                                  1.5s
 => [auth] library/alpine:pull token for registry-1.docker.io                                                                             0.0s
 => [auth] library/golang:pull token for registry-1.docker.io                                                                             0.0s
 => [stage-1 1/2] FROM docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c              0.1s
 => => resolve docker.io/library/alpine:3.16@sha256:686d8c9dfa6f3ccfc8230bc3178d23f84eeaf7e457f36f271ab1acc53015037c                      0.1s
 => [internal] load build context                                                                                                         0.0s
 => => transferring context: 29B                                                                                                          0.0s
 => [builder 1/3] FROM docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696    0.1s
 => => resolve docker.io/library/golang:1.19-rc-alpine@sha256:ee6074afda6d870c67201d3a2d26b39cd8ad787ca374f7f4271f54bf40848696            0.1s
 => CACHED [builder 2/3] COPY main.go .                                                                                                   0.0s
 => CACHED [builder 3/3] RUN go build -o /app main.go                                                                                     0.0s
 => CACHED [stage-1 2/2] COPY --from=builder /app .                                                                                       0.0s
 => exporting to oci image format                                                                                                         0.5s
 => => exporting layers                                                                                                                   0.0s
 => => exporting manifest sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9                                         0.0s
 => => exporting config sha256:acd416cc5361011fbd32a67dc0749bc0f029a031427735b283db1bc0a1c7e831                                           0.0s
 => => sending tarball                                                                                                                    0.4s
unpacking docker.io/library/mygoapp:v1.0 (sha256:ab83037c81606dd648de95cddf6cd101179d7ae5f5ee163176d6a197af68efd9)...done

```

```
> nerdctl --namespace k8s.io images
REPOSITORY         TAG           IMAGE ID        CREATED           PLATFORM       SIZE         BLOB SIZE
mygoapp            v1.0         ab83037c8160    57 seconds ago    linux/amd64    7.6 MiB      3.7 MiB
```
