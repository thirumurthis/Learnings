package s3cliapp.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.security.cert.CertificateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class s3handler {
    
    private static final Logger logger = LoggerFactory.getLogger(s3handler.class);

    public s3handler(){}

        public String handler(s3object operation) {
        
            System.out.println(operation.toString());
        
        Region s3region = (operation.region().isEmpty() || operation.region().isBlank())?
                                  Region.US_EAST_1 : Region.of(operation.region());
        S3Client s3Client = null;
            try{

            TrustManager[] trustManagers = createTrustManagers(operation.certPath());

            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(operation.endpoint()))
                    .crossRegionAccessEnabled(false)
                    .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(operation.accessKey(), operation.secretKey())))
                    .region(s3region)
                    .forcePathStyle(true) // Many self-hosted or alternative platforms require this enabled.
                    .httpClientBuilder(ApacheHttpClient.builder().tlsTrustManagersProvider(() -> trustManagers)) // Passes the certificates
                    .build();

            switch (operation.operation()) {
                case "list":
                    Map<String,String> buckets = listBuckets(s3Client);
                    logger.info("Listing of buckets:");
                    if (buckets.isEmpty()){
                        System.out.println("No buckets found"); 
                    }else{
                       System.out.println("List of Buckets [name=<bucket-name>, created-on=<bucket-created-on>]");
                    }
                    buckets.forEach((k,v) -> System.out.println("[name="+k+", created-on="+v+"]"));
                    break;
                case "upload":
                    if(uploadFileToBucket(s3Client, operation.bucketName(), operation.file(), operation.contentType())){
                        System.out.println("File [ "+operation.file() +" ] uploaded successfully");
                    }else{
                        logger.error("Error occurred when uploading file");
                        System.out.println("Error occurred when uploading file");
                    }
                    break;
                case "create":
                    if(createBucket(s3Client, operation.bucketName())){
                        System.out.println("[ "+ operation.bucketName()+" ] created.");
                    } else {
                        System.out.println("[ "+ operation.bucketName() +" ] already exists.");
                    }
                default:
                    break;
            }
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                System.err.println("CRITICAL: Certificate initialization or KeyStore setup failed: " + e.getMessage());
        } catch (IOException e) {
                System.err.println("CRITICAL: Failed to read the certificate file at path: " + operation.certPath());
        } catch (S3Exception e) {
                // Server-side errors (e.g., Invalid Access Key, Signature Does Not Match, Bad Request)
                System.err.println("AWS/S3 API ERROR: The storage provider rejected the request.");
                System.err.println("Status Code: " + e.statusCode());
                System.err.println("AWS Error Code: " + e.awsErrorDetails().errorCode());
                System.err.println("Message: " + e.awsErrorDetails().errorMessage());
        } catch (SdkClientException e) {
            // Client-side errors (e.g., cannot resolve host, network timeout, SSL Handshake failed)
            System.err.println("CLIENT NETWORK ERROR: Could not reach or establish a secure connection to the server.");
            System.err.println("Details: " + e.getMessage());
            if (e.getCause() instanceof javax.net.ssl.SSLHandshakeException) {
                System.err.println("-> Hint: The server certificate is still rejected. Double check your custom .crt/.pem file.");
                }
        } finally {
            if (s3Client != null) {
                s3Client.close();
            }
        }

        return "Execution Completed!!!";
   }

    private Map<String, String> listBuckets(S3Client s3Client){

        ListBucketsResponse response = s3Client.listBuckets();
        
        Map<String, String> bucketResponse = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                 .withZone(ZoneId.of("UTC"));

        for (Bucket bucket : response.buckets()) {
            //System.out.println(" - " + bucket.name() + " (Created: " + bucket.creationDate() + ")");
            bucketResponse.computeIfAbsent(bucket.name(), v-> formatter.format(bucket.creationDate()));
        }
        return bucketResponse;
    }

    private boolean uploadFileToBucket(S3Client s3Client, String bucketName, String filePath, String contentType){

        boolean fileUploaded = false;
        createBucket(s3Client, bucketName);

        Path localFilePath = Paths.get(filePath);
        String objectPath = "upload/"+Paths.get(filePath).getFileName().toString();
        try {
         if (Files.notExists(localFilePath)) {
            // Manually trigger the exception to avoid running SDK code blindly
            throw new NoSuchFileException(localFilePath.toAbsolutePath().toString());
         }
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(objectPath)
        .contentType(contentType) // Change based on your file type (e.g., image/png, text/csv)
        .build();
        
        // 3. Upload using RequestBody.fromFile
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(localFilePath));

        System.out.println("File uploaded successfully!");

        fileUploaded = true;
        } catch (Exception exception){
            exception.printStackTrace();
        }

        return fileUploaded;
    }

    private boolean createBucket(S3Client s3Client, String bucketName) throws S3Exception{
        boolean bucketCreated = false;
        if (!doesBucketExist(s3Client, bucketName)) {
            System.out.println("Bucket does not exist. Creating bucket: " + bucketName);
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
            bucketCreated = true;
        } else {
            System.out.println("Bucket '" + bucketName + "' already exists. Skipping creation.");
            
        }
        return bucketCreated;
    }
        private  boolean doesBucketExist(S3Client s3Client, String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            // In some custom S3 environments, a 403 Forbidden might fire if 
            // you don't have head privileges but the bucket still exists.
            if (e.statusCode() == 403) {
                System.out.println("Warning: Access denied on HeadBucket, assuming bucket exists.");
                return true;
            }
            throw e;
        }
    }

    private TrustManager[] createTrustManagers(String certPath) 
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        
        try (FileInputStream fis = new FileInputStream(certPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("custom-s3-ca", cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            return tmf.getTrustManagers();
        }
    }
}
