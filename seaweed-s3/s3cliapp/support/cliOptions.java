package s3cliapp.support;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "s3cli", mixinStandardHelpOptions = true, version = "s3cli 1.0",
        description = "s3cli operations create and list buckets, upload file.")
public class cliOptions implements Callable<Integer>{

    @Option(names={"--endpoint"}, defaultValue = "${env:S3_ENDPOINT}", required = true)
    private String endpointUrl;

    @Option(names={"--access-key"}, defaultValue = "${env:S3_ACCESS_KEY}", required = true)
    private String accessKey;

    @Option(names={"--secret-key"}, defaultValue = "${env:S3_SECRET_KEY}", required = true)
    private String secretKey;

    @Option(names={"--operation"}, defaultValue = "${env:S3_OPERATION}", required = true, description="operation list|create|upload")
    private String operation;

    @Option(names={"--region"}, defaultValue = "${env:S3_REGION:-us-west-1}", required = false)
    private String s3region;

    @Option(names={"--bucket"}, defaultValue = "${env:S3_BUCKET}", required = false, description="bucket name")
    private String bucketName;

    @Option(names={"--cert"}, defaultValue = "${env:S3_CERT_PATH}", required = false,description="certificate path of the S3")
    private String certPath;

    @Option(names={"--file"}, defaultValue="${env:INPUT_FILE}", required = false, description="file to upload when using upload operation")
    private String file;

    @Option(names={"--content-type"}, defaultValue="${env:S3_CONTENT_TYPE:-}", required = false, description="file to upload when using upload operation")
    private String contentType;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display help message")
    private boolean helpflag = false;

    private final s3handler s3clihandler; 

    public cliOptions(s3handler s3clihandler){
        this.s3clihandler = s3clihandler;
    }

    @Override
    public Integer call() throws Exception{
        try{
            s3object s3obj = new s3object(operation, endpointUrl, accessKey, secretKey, certPath, s3region, bucketName, file, contentType);
            System.out.println(s3clihandler.handler(s3obj));
        } catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println(sw.toString());
            System.out.println("---------------------");
            System.out.println(e.getMessage());
            return -1;
        }

        return 0;
    }
}
