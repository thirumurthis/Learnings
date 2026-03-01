package main

import (
	"crypto/tls"
	"flag"
	"fmt"
	"log"
	"net/http"

	"github.com/google/go-containerregistry/pkg/authn"
	"github.com/google/go-containerregistry/pkg/name"
	"github.com/google/go-containerregistry/pkg/v1/remote"
	"github.com/google/go-containerregistry/pkg/v1/tarball"
)

/*
create a folder and add the main
$ mkdir image-handler
$ cd image-handler
$ go mod init image-handler
$ mkdir cmd/image-handler
$ cd cmd/image-handler
$ touch main.go
// copy paste below content
//use the docker compose to sping up the nexus with slef singed cert
// donwlaod the image to loca tar like 
$ docker save -o busybox.tar busybox:latest
// if different image is used then update the below code path and image tar file
// run the command below to run the go - assuming username and password are stored env variable
$ go ./cmd/image-handler -username $USERNAME -password $PASSCREDS
*/

func main() {

	username := flag.String("username", "", "artifactory username")
	password := flag.String("password", "", "artifactory password")
	insecure := flag.Bool("insecure", true, "enable when using TLS, insecure by default")

	flag.Parse()

	fmt.Println("Image loading...")
	img, err := tarball.ImageFromPath("busybox.tar", nil)

	if err != nil {
		log.Fatalf("failed to load tar : %v", err)
	}

	ref, err := name.ParseReference("nexus.local/my-docker/busybox:v26.1.0")

	if err != nil {
		log.Fatalf("invalid reference: %v", err)
	}

	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: *insecure,
		},
	}
	if *username == "" || *password == "" {

		fmt.Println("Image loading without credentials")
		err = remote.Write(ref, img, remote.WithTransport(transport))
	} else {
		fmt.Println("Image loading with credentials")
		err = remote.Write(ref, img,
			remote.WithAuth(&authn.Basic{
				Username: *username,
				Password: *password,
			}),remote.WithTransport(
				transport),
			)
	}

	if err != nil {
		log.Fatalf("failed to push image: %v", err)
	}
}
