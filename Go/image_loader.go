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
package main

import (
	"archive/tar"
	"compress/gzip"
	"crypto/tls"
	"errors"
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"github.com/google/go-containerregistry/pkg/authn"
	"github.com/google/go-containerregistry/pkg/name"
	"github.com/google/go-containerregistry/pkg/v1/remote"
	"github.com/google/go-containerregistry/pkg/v1/tarball"
)

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

func GetTarFileNameFromGzFile(gzFilePath string) string {
	gzExtension := ".gz"

	if gzFilePath == "" || !strings.HasSuffix(gzFilePath, gzExtension) {
		fmt.Printf("file %s is not a gz file", gzFilePath)
		return ""
	}
	gzTarFilename := filepath.Base(gzFilePath)
	tarFileName := strings.TrimSuffix(gzTarFilename, gzExtension)
	return tarFileName
}

func UntarGzToFile(srcPath, destPath string) error {
	// 1. Open the source .tar.gz file
	file, err := os.Open(srcPath)
	if err != nil {
		return err
	}
	defer file.Close()

	// 2. Create a gzip reader to decompress the stream
	gzr, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzr.Close()

	// 3. Create the destination .tar file
	outFile, err := os.Create(destPath)
	if err != nil {
		return err
	}
	defer outFile.Close() // Ensure the output file is closed properly

	// 4. Copy the uncompressed data from the gzip reader to the output .tar file
	if _, err := io.Copy(outFile, gzr); err != nil {
		return err
	}

	// Manually close the outFile immediately after io.Copy to catch potential write errors
	if err := outFile.Close(); err != nil {
		return err
	}

	return nil
}

func ExtractTarGz(src, dest string, extractTar bool) error {
	file, err := os.Open(src)
	if err != nil {
		return err
	}
	defer file.Close()

	gzr, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzr.Close()

	if extractTar {
		tr := tar.NewReader(gzr)
		for {
			header, err := tr.Next()
			if err == io.EOF {
				break
			}
			if err != nil {
				return err
			}

			target := filepath.Join(dest, header.Name)
			switch header.Typeflag {
			case tar.TypeDir:
				os.MkdirAll(target, 0755)
			case tar.TypeReg:
				os.MkdirAll(filepath.Dir(target), 0755)
				outFile, _ := os.Create(target)
				io.Copy(outFile, tr) // Uses io.Copy for data extraction
				outFile.Close()
			}
		}
	} else {
		destTar := CreateTarFileNameFromGivenTarGzFilename(src)
		fmt.Println("extract the tar file name on dest ", destTar)
		targetDestPath := filepath.Join(dest, destTar)
		tarOutFile, err := os.Create(targetDestPath)
		if err != nil {
			log.Fatalf("cannot create the tar file from the gz format")
		}
		defer tarOutFile.Close()
		_, err = io.Copy(tarOutFile, gzr)

		if err != nil {
			log.Fatal("error occured during copy to tar")
		}
		if err := tarOutFile.Close(); err != nil {
			log.Fatal("error closing the file")
		}
	}
	return nil
}

func main() {

	username := flag.String("username", "", "artifactory username")
	password := flag.String("password", "", "artifactory password")
	insecure := flag.Bool("insecure", true, "enable when using TLS, insecure by default")
	imageTar := flag.String("image-tar", "", "the tar image saved using docker save -o image.tar nginx")
	repoPath := flag.String("repo-path", "", "the repo image path example nexus.local/local-docker/nexus:v26.1.0")
	extractGzPath := flag.String("extract-gz-path", "", "path to extract the tar.gz as tar file to uploade image")

	flag.Parse()

	gzSuffix := ".gz"

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

	if *extractGzPath == "" {
		log.Fatal("path to extract the tar.gz should be specified")
	}

	iTar := *imageTar
	if strings.HasSuffix(*imageTar, gzSuffix) {

		tarFileName := GetTarFileNameFromGzFile(*imageTar)

		fmt.Printf("Filename: %s\n", tarFileName)

		//err := ExtractTarGz(*imageTar, *extractGzPath, false)
		err := UntarGzToFile(*imageTar, fmt.Sprintf("%s/%s", *extractGzPath, tarFileName))
		if err != nil {
			log.Fatal("error untar gzip file")
		}

		iTar = fmt.Sprintf("%s/%s", *extractGzPath, tarFileName)
		_, err = FileExists(iTar)

		if err != nil {
			log.Fatalf("decompressed tar file doesn't exist %s", iTar)
		}

	}

	fmt.Println("loading image :", iTar, "repo", *repoPath)

	img, err := tarball.ImageFromPath(iTar, nil)

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


