### Client Certificate based Authentication
 - If we have the CA signed private certificate.
   - First we need to create a (X509) public key and share it with the appropriate API Endpoint.
   - Once the certificate is added to the API Endpoint, we can access it.


Client Authentication:
 - Instead of using username password, we can use certifcate based authentication.


- Accesing from RestTemplate code with SSLContext.

  -Pre-requsites: Add the private key to the keystore using keytool.

```java
package com.demo.sslrest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;


public class APIHttpsEndPoint {

   
    public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, Exception {
    	APIHttpsEndPoint apiendpoint=new APIHttpsEndPoint();
    	
    	//get REST RestTemplate initialized with correct cert/keystore 
    	 RestTemplate restTemplate = apiendpoint.getRestTemplate();
    	 final HttpHeaders headers = new HttpHeaders();
 
        //Create a new HttpEntity
         final HttpEntity<String> entity = new HttpEntity<>(headers);
         String endpointUrl = "https://eec.uat.bfst.digitalaviationservices.com/connectedAircraft/storage/v2/icao/BBD/BOEING/65800/obeds/file/BON779XZ_REPORT_ACMFR-ACMFREP2_20191126183908+0000.zip";
    
         //Execute method to writing your HttpEntity to the request
         ResponseEntity<String> response = restTemplate.exchange(endpointUrl, HttpMethod.GET, entity, String.class);
    	
         System.out.println(repsonse); //prints the repsone code 200 OK or 401 Unauthorize error
         System.out.println(response.getBody().toString());
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
