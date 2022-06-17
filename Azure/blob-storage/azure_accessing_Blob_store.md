

```java


public void processBlob(){
      //getting the info from external configuration
      String accountName = getStorageAccountName();
      String accessKey = getAccessKey();
      String container = getContainerName();

      //create connection string
      String connString =  String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",accountName,accessKey);

      // create account
      CloudStorageAccount account=  CloudStorageAccount.parse(connString );

      // create client
      CloudBlobClient blobClient = account.createCloudBlobClient();

      // create container
      CloudBlobContainer container = blobClient.getContainerReference(container);

      // fetch item from list
      Iterable<ListBlobItem> blobItems = container.listBlobs();

      blobItems .forEach(item -> {

      if (item instanceof CloudBlockBlob){ 
             
         try {
              // need to check if the blob item exists first 
              UUID uuid = UUID.randomUUID();
                        
              CloudBlockBlob blob = null;
              
              //check if the blob exists before acquiring the lease
              
              if(((CloudBlockBlob) blobItem).exists()) {

                    	((CloudBlockBlob) blobItem).acquireLease(60, uuid.toString()); // acquire lease for 60 sec
                    	((CloudBlockBlob) blobItem).getMetadata().put(LEASE_ID, uuid.toString()); //update metadata with the id
                      
                      //download the blob
                     CloudBlockBlob blob =  blobClient.downloadBlobsInContainerByName((CloudBlockBlob)blobItem,"/src/data/download-from-blob/");
                      
                      //Do logic with the downloaded content
                      
                      //delete the blob
                      deleteBlob(blob);
               }
             } catch(Exception e){
                //log error message
                // ((CloudBlockBlob)blobItem).getName() - to print the blob name 
             }
       }
});

}

public void deleteBolb(CloudBlockBlob blob){

  // we can use the uuid variable directly, in here we use the metadata and apply it.
  String uuid = blobItem.getMetadata().get(LEASE_ID); 

   AccessCondition accessCondition = AccessCondition.generateLeaseCondition(uuid);
   
   // IN case if we need to release the lease uncomment below code 
   //blobItem.releaseLease(accessCondition);

   //delete the blob with the lease option
    blobItem.deleteIfExists(DeleteSnapshotsOption.NONE,accessCondition,null,null);
}

}
```
