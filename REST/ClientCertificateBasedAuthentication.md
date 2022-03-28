### Client Certificate based Authentication
 - If we have the CA signed private certificate.
   - First we need to create a (X509) public key and share it with the appropriate API Endpoint.
   - Once the certificate is added to the API Endpoint, we can access it.


Client Authentication:
 - Instead of using username password, we can use certifcate based authentication.

- To Debug the flow we can use below jvm arguments
```
-Djavax.net.debug=ssl:handshake:verbose:keymanager:trustmanager -Djava.security.debug=access:stack
```

- Accesing from RestTemplate code with SSLContext.

  -Pre-requsites: Add the private key to the keystore using keytool.

```java
package com.demo.sslrest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class APIHttpsEndPoint {

/*
BELOW CODE IS AN EXAMPLE WHERE AN ***HTTPS*** REST ENDPOINT IS EXPOSED, WHERE WE CAN FIRE A QUERY
GET THE LINK TO DOWNLOAD THE FILE IN THE FIRST URL RESPSONE

USING THE RESPONSE RECEIVED WE DOWNLOAD THE SPECIFIC FILE
*/
   
    public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, Exception {
    	APIHttpsEndPoint apiendpoint=new APIHttpsEndPoint();
    	
    	//get REST RestTemplate initialized with correct cert/keystore 
    	 RestTemplate restTemplate = apiendpoint.getRestTemplate();
    	 final HttpHeaders headers = new HttpHeaders();
 
        //Create a new HttpEntity
         final HttpEntity<String> entity = new HttpEntity<>(headers);
         String endpointUrl = "https//domain.com/path/to/fetch/response URL TO DOWNLOADABLE LINK";
    
         //Execute method to writing your HttpEntity to the request
         ResponseEntity<String> response = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
    	
         System.out.println(repsonse); //prints the repsone code 200 OK or 401 Unauthorize error
         System.out.println(response.getBody().toString());
	 //If the response is successful we can udpate download (if the REST endpoint provides downloadable content)
	 
    	if (response.getStatusCode().equals(HttpStatus.OK)) {
	    //Since the response is an Json object - we can use jackson to downlaod the content
            ObjectMapper mapper = new ObjectMapper();

            //declare the json node 
            JsonNode jsonNode;

            try {
                jsonNode = mapper.readTree(response.getBody());
		/*
		 if the response is like 
		 ... 
		  "data" : {
		     "downloadUrl" : "https://domain.com/path/to/download/file",
		     ...
		  }
		*/
                String url = jsonNode.get("data").get("downloadUrl").asText();
   
                String filename="/home/my@path/test/temp/downloadedfile-demo.zip";

                RequestCallback requestCallback = request -> {
                    // Set Accept header
                    request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
                  };

                ResponseExtractor<File> responseExtractor = getResponseExtractor(filename);
                File zipFile = restTemplate.execute(URI.create(url), HttpMethod.GET, requestCallback, responseExtractor);
                System.out.println("Downloaded completed at - "+zipFile.toURI());
 
             } catch (IOException e) {
            	System.out.println("Error parsing  JSON response request "+e.getMessage());
             }
        }
    }
    
    public RestTemplate getRestTemplate() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, Exception {

        char[] password = "<keystorepassword>".toCharArray(); //should flow from applicaton.properties
        
        TrustStrategy trustStrategy= (X509Certificate[] chain,String authType)->true;

        javax.net.ssl.SSLContext sslContext = SSLContextBuilder.create()
    	         .loadKeyMaterial(keyStore("/my/path/to/keystore-cert.jks", password,"JKS"), password) //PCKS12 is the latest keystore format
    	         //.loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();  //use this for self-signed certificate
    		  .loadTrustMaterial(null, trustStrategy).build();

    	    HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
    
         ClientHttpRequestFactory requestFactory =
              new HttpComponentsClientHttpRequestFactory(httpClient);

    	    return  new RestTemplate(requestFactory);
	}
   
    private KeyStore keyStore(String file, char[] password, String storeType) throws Exception {
       KeyStore keyStore = KeyStore.getInstance(storeType);
        File key = ResourceUtils.getFile(file);
        if(key.exists()) {
        	System.out.println("file exists "+key.getAbsolutePath());
        }
        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        return keyStore;
    }
}

//Use spring utility to donwload the file, if the URL  streams the data in response
// arguments are the file name and directory 
    private static ResponseExtractor<File> getResponseExtractor(String filenamepath) {
		// Streams the response to file instead of loading it all in memory
		ResponseExtractor<File> responseExtractor = response -> {
		    // Create path to where we want to write file
		    // directory where the file needs to be downloaded
		    Path path = Paths.get(filenamepath);

		    // Write the response to the file path
		    Files.copy(response.getBody(), path, StandardCopyOption.REPLACE_EXISTING);
		    return path.toFile();
		};
		return responseExtractor;
	}
   
```

 NOTES: on `PEM` and `DER`:
 

 `X.502` certiticate has two format
  - PEM (base64 ASCII) format (PEM are usually with .crt, .pem, .cer and .keys (for private keys))
  - DER (binary)

To view the content of PEM file use

```
openssl x509 -in <certificate.pem> -text -noout
```
Note: certificate.pem, can be certificate.crt

#### To convert a PEM to DER format
```
openssl x509 -outform der -in <certificate.pem> -out certificate.der
```
To view the content of the DER file
```
openssl x509 -inform der -in <Certificate.der> -text -noout
```
if you use pem file and try to convert then this will report an issue

#### Convert PEM to PCKS#12 (pkcs12  or pfx) extension .p12/.pfx.
```
openssl pkcs12 -export -out <certificate.pfx> -in <certificate.cer> -certfile <cert.crt>
```

#### Converting DER to PEM

openssl x509 -inform der -in <certificate.der> -out <certificate.pem>

To convert the DER cert to PKCS#12, that needs to be converted to PEM.

Reference link 
 - [1](https://dzone.com/articles/extracting-a-private-key-from-java-keystore-jks)
 - [2*](https://www.thesslstore.com/blog/client-authentication-certificate-101-how-to-simplify-access-using-pki-authentication/)
 - [3](https://medium.com/@itzgeoff/using-a-custom-trust-store-with-resttemplate-in-spring-boot-77b18f6a5c39)
