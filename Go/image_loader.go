package main

import (
	"crypto/tls"
	"errors"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"

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

func FileExists(filePath string) (bool, error) {

	_, err := os.Stat(filePath)
	if err == nil {
		return true, nil
	}

	if errors.Is(err, os.ErrNotExist) {
		return false, err
	}

	return false, fmt.Errorf("File not exist or Error occured during file check")
}

func main() {

	username := flag.String("username", "", "artifactory username")
	password := flag.String("password", "", "artifactory password")
	insecure := flag.Bool("insecure", true, "enable when using TLS, insecure by default")
	imageTar := flag.String("image-tar", "", "the tar image saved using docker save -o image.tar nginx")
	repoPath := flag.String("repo-path", "", "the repo image path example nexus.local/local-docker/nexus:v26.1.0")

	flag.Parse()

	if *imageTar == "" {
		log.Fatal("image tar should be provided, using flag -image-tar image.tar")
	}

	if *repoPath == "" {
		log.Fatal("repo path to upload the image should be provided, using flag -repo-path nexus.local/local-docer/nexus:v2026.1.0")
	}

	_, fileError := FileExists(*imageTar)

	if fileError != nil {
		log.Fatalf("Image tar file doesn't exists %v", fileError)
	}

	fmt.Println("loading image :", imageTar, "repo", *repoPath)
	img, err := tarball.ImageFromPath(*imageTar, nil)

	if err != nil {
		log.Fatalf("failed to load tar : %v", err)
	}

	ref, err := name.ParseReference(*repoPath)

	if err != nil {
		log.Fatalf("invalid reference: %v", err)
	}

	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: *insecure,
		},
	}
	if *username == "" && *password == "" {

		fmt.Println("Image loading without credentials")
		err = remote.Write(ref, img, remote.WithTransport(transport))
	} else {
		fmt.Println("Image loading with credentials")
		err = remote.Write(ref, img,
			remote.WithAuth(&authn.Basic{
				Username: *username,
				Password: *password,
			}), remote.WithTransport(
				transport),
		)
	}

	if err != nil {
		log.Fatalf("failed to push image: %v", err)
	}
}

