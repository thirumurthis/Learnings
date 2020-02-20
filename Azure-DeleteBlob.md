```java
package boeing.ahm.app;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.EnumSet;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.file.DeleteShareSnapshotsOption;


public class ListBlobAzureTEST {

	public static void main(String...args) {
		CloudBlobClient blobClient;
		CloudBlobContainer container1 = null;

		try {
			blobClient = ListBlobAzureTEST.getBlobClientReference();
			container1 = blobClient.getContainerReference( "<Container-name>"); //conatiner name
			
		    for (ListBlobItem item : container1.listBlobs("", true, EnumSet.of(BlobListingDetails.SNAPSHOTS), null, null)) {
			//for (ListBlobItem item : container1.listBlobs()) { // this can also be used.
				//URI snapshotURI = item.getUri(); // the url value.

				if(item.getUri().toString().startsWith("<If-the-file-startes-with-specific-string,url-of-the-blob>")
						&& item.getUri().toString().endsWith(".blob")) {
					System.out.println("deleting... "+item.getUri().toString());
          if(toDelete)
					 ((CloudBlockBlob)item).delete(); //will delete the blobk, NOTE: container can also be deleted. just use container1.delete();
				}

			}
		} catch (InvalidKeyException | RuntimeException | IOException | URISyntaxException | StorageException e) {

			e.printStackTrace();
		}
	}


	public static CloudBlobClient getBlobClientReference() throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {


		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse("<accesskey-connections-tring>");
		}
		catch (IllegalArgumentException|URISyntaxException e) {
			System.out.println("\nConnection string specifies an invalid URI.");
			throw e;
		}
		catch (InvalidKeyException e) {
			System.out.println("\nConnection string specifies an invalid key.");
			throw e;
		}

		return storageAccount.createCloudBlobClient();
	}

}
```
