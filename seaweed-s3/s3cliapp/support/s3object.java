package s3cliapp.support;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public record s3object(String operation, String endpoint, String accessKey, String secretKey,
                       String certPath, String region, String bucketName, String file, String contentType) {
    
    public s3object{
        Objects.requireNonNull(operation,"operation can't be null");
        Objects.requireNonNull(endpoint,"endpoint can't be null");
        Objects.requireNonNull(accessKey,"accessKey can't be null");
        Objects.requireNonNull(secretKey, "secretKey can't be null");
        if (operation.isBlank() || endpoint.isBlank() || accessKey.isBlank() || secretKey.isBlank()){
            throw new IllegalArgumentException("operation, endpoint, accessKey and secretKey are mandatory");
        }

        if (operation.equalsIgnoreCase("upload")){
            Objects.requireNonNull(file, "for [upload] operation [file] should be provided");
            Objects.requireNonNull(bucketName,"for [upload] operation [bucket] should be provided");
            Objects.requireNonNull("contentType","for [upload] operation [content-type] should be provided");
            if (file.isBlank() || bucketName.isBlank()){
                    throw new IllegalArgumentException("for upload operation bucketName - file and content type (application/pdf, etc) are mandatory");
            }
            if(contentType.isBlank() || contentType.isEmpty()){
                Path path = Paths.get(file);
                String fileName = path.getFileName().toString();
                int lastDotIdx = fileName.lastIndexOf(".");
                String ext = (lastDotIdx > 0 )? fileName.substring(lastDotIdx+1):"";
                
                List <String> imgExts = List.of( "png", "jpeg", "jpg", "gif", "svg");
                List <String> fileExts = List.of("pdf","xml","json");
                List <String> webExts = List.of("htm","html","css","js","txt");

                if(fileExts.stream().anyMatch(ext::equalsIgnoreCase)){
                   contentType = "application/"+ext;
                } else
                if(webExts.stream().anyMatch(ext::equalsIgnoreCase)){
                    contentType = "text/"+ext;
                    if (ext.equalsIgnoreCase("htm") || ext.equalsIgnoreCase("html")){
                        contentType= "text/html";
                    }
                    if (ext.equalsIgnoreCase("js")){
                        contentType = "text/javascript";
                    }
                    if(ext.equalsIgnoreCase("txt")){
                        contentType = "text/plain";
                    }

                } else
                if(imgExts.stream().anyMatch(ext::equalsIgnoreCase)){
                   contentType = "image/"+ext;
                   if (ext.equalsIgnoreCase("svg")){
                    contentType = "image/"+ext+"+xml";
                   }
                } else {
                   contentType = "application/octet-stream"; // Fallback safe default
                }
                
            }

        }

        if(operation.equalsIgnoreCase("create")){
            Objects.requireNonNull(bucketName,"for [create] operation [bucket] should be provided");
         if (bucketName.isBlank()){
            throw new IllegalArgumentException("for create operation bucket name is mandatory");
           }

        }
    }

     @Override
    public String toString() {
         return String.format("Input provided for S3 service [operation='%s', endpoint='%s', accessKey='%s', secretKey='%s', certPath='%s', region='%s', bucketName='%s', file='%s', contentType='%s']", 
              this.operation,this.endpoint, this.accessKey,"*****",this.certPath,this.region,this.bucketName,this.file, this.contentType);
    }
    
}
