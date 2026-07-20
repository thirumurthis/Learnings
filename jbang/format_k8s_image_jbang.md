### Utility tool - Format Kubernetes image info with JQ and JBang


Working on Kubernetes resources deploying the application often wanted to create an utility tool to format the few Kubernetes resources in specific table format to improve productivity. All I needed is to list the images from Kubernetes deployment in clean and simple table format of both initContainers and containers. To achive this had used JQ and JBang. There could possibly be different options to achieve this, the details that helped me is documented in this article.

The convenience with this approach is the outputs are piped easily. The kubectl get deployment is converted to json and piped to JQ with filter conditions to create a csv formatted output which would be deployment name, namespace and image information of both initContainers and container at the end as comma separated values. This is piped to JBang code which includes the logic to transform the output to presentable format. 

To follow along below are the pre-requisites that needs to be installed in the machine

 - JBang
 - Kubectl
 - KinD (used to deploy the Kubernetes cluster)
 - Java JDK

In order to work with the Powershell script, the JBang code is compiled to jar the command are shown below. For Powershell script the JQ command output is piped to the Java command which calls the JBang java class. In case of gitbash the JBang java class file is directly called using the jbang CLI. Refer the output snapshot at the bottom of this article.

Below is the command piped one after the other and works in Gitbash since the output of JQ is finally piped to the JBang CLI. This command doesn't directly work in command prompt.

```sh
kubectl get deployment -A -o json | jq -r ' .items[] | [(.metadata.name), (.metadata.namespace), (.spec.initContainers[]?.image | split("/") | .[-1]), (.spec.template.spec.containers[].image | split("/") | .[-1] )] | @csv ' | jbang Formatter.java
```

The JBang java code, the input to the code below is the will be comma selerated csv format like below 

```
"apicurio-registry-operator-v3.2.1","apicurio-registry","apicurio-registry-3-operator:3.2.1"
```

The init container images will be part of the CSV format added to the end. The output format doesn't diffirentiate the initcontainer images or container images, but groups and lists under the deployment name. 

```java
///usr/bin/env jbang $0 "$@" ; exit $?

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Formatter {

    static int SPACING = 2;
    static String DEPLOY_NAME="DEPLOYMENT NAME";
    static String NAMESPACE="NAMESPACE";
    static String IMAGE_NAME="IMAGE NAME";
    static String VERSION="VERSION";
    static int[] fieldLengthArray = {DEPLOY_NAME.length(),NAMESPACE.length(),
                                     IMAGE_NAME.length(),VERSION.length()};

    public static void main(String[] args){

        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            String inputLine;

            List<Metadata> metadata = new ArrayList<>();
            // each line will includes the deployment name, namespace, image1, image2
            // includes init container info 
            while((inputLine = in.readLine()) != null){

                List<String> str = Arrays.asList(inputLine.replace("\"","").split(","));

                String deploymentName = str.get(0);
                String namespace = str.get(1);

                List<Map<String, String>> imgInfoList = new ArrayList<>();

                for(int i=2; i < str.size(); i++){
                    String imgName = str.get(i).split(":")[0];
                    String img = (imgName != null ? imgName.split("/")[imgName.split("/").length -1]:"");
                    String ver = str.get(i).split(":")[1];

                    Map<String, String> imageInfo = new HashMap<>();

                    imageInfo.put(img, ver);
                    imageInfo.computeIfAbsent(img, value -> ver);
                    imageInfo.computeIfPresent(img, (key,value)->ver);

                    imgInfoList.add(imageInfo);
                }
                metadata.add(new Metadata(deploymentName, namespace, deDupImageInfo(imgInfoList)));
            }

            maxFieldLength(metadata);
            //add spacing to adjust the field length for each element
            fieldLengthArray = Arrays.stream(fieldLengthArray).map(element -> element + SPACING).toArray();

            String resultOutputFormat = "|%1$-"+fieldLengthArray[0]+"s|%2$-"+fieldLengthArray[1]+"s|%3$-"+fieldLengthArray[2]+"s|%4$-"+fieldLengthArray[3]+"s|\n";
            System.out.format(resultOutputFormat, DEPLOY_NAME, NAMESPACE, IMAGE_NAME, VERSION);
            System.out.format(resultOutputFormat, "--------------","----------","----------","-------");

            printDetails(metadata, resultOutputFormat);

        } catch(IOException exception){
            exception.printStackTrace();
        }
    }

    public record Metadata(String deploymentName, String namespace, List<Map<String,String>> imageInfo){};

    /**
     * Method will add the image info to a array used to compute the field leangth
     * so it could be displayed in the output in formatted way
     * @param metadata
     */
    public static void maxFieldLength(List<Metadata> metadata){
        metadata.forEach( item -> {
            item.imageInfo.forEach( img -> {
                fieldLengthArray[0] = Math.max(fieldLengthArray[0], item.deploymentName.length());
                fieldLengthArray[1] = Math.max(fieldLengthArray[1], item.namespace.length());   

                for(String key: img.keySet()){
                    fieldLengthArray[2] = Math.max(fieldLengthArray[2], key.length());   
                    fieldLengthArray[3] = Math.max(fieldLengthArray[3], img.get(key).length());   
                }
            });
        });
    }

    /**
     * Method used to print the image info in the screen if init container 
     * to indent the deployment name
     */
    public static void printDetails(List<Metadata> metadata, String format){
        metadata.forEach(item -> {
            boolean hasImages = (item.imageInfo.size()>1);
            AtomicInteger counter = new AtomicInteger();

            item.imageInfo.forEach(img -> {
                for(String key: img.keySet()) {
                    if (hasImages && counter.get() >= 1 ) {
                        System.out.format(format, " -- ", item.namespace, key, img.get(key));
                    } else{
                        System.out.format(format, item.deploymentName, item.namespace, key, img.get(key));
                        counter.getAndAdd(1);
                    }
                }
            });
        });
    }

    /**
     * Method to remove the duplicate image info from the deployment
     * @param listOfFields
     * @return
     */
    public static List<Map<String, String>> deDupImageInfo(List<Map<String, String>> k8sImageInfoList){

        Set<Map<String, String>> seenMaps = new HashSet<>();
        List<Map<String, String>> deDupedList = new ArrayList<>();

        for (Map<String,String> map: k8sImageInfoList){

            if(seenMaps.add(map)){
                deDupedList.add(map);
            }
        }
        return deDupedList;
    }
}

```

To create the jar from the above JBang code. Save the above file in Formatter.java and use below command. Folder are created to group the files.

```sh
$ mkdir -p jars/classes

$ javac -d jars/classes Formatter.java
$ jar cvfe jars/Formatter.jar Formatter -C jars/classes .
```

To create the powershell script, save as k8sFormatterCmd.ps1. Note when executing the below command makes sure to update the Jar path.

```bash
if ($null -eq $env:KUBECONFIG) {
	Write-Host ""
    Write-Host "[KUBECONFIG variable does not exists or is empty]"
	Write-Host "In powershell $env:KUBECONFIG=\"path/to/kube/config\""
} else {
	
	if ($PSVersionTable.PSVersion.Major -lt 7) {
     kubectl get deployment -A -o json | jq -r ' .items[] | [(.metadata.name), (.metadata.namespace), (.spec.template.spec.initContainers[]?.image | split(\"\/\") | .[-1]), (.spec.template.spec.containers[].image | split(\"\/\") | .[-1] )] | @csv ' | java -jar jars/Formatter.jar Formatter

   } else {
      kubectl get deployment -A -o json | jq -r ' .items[] | [(.metadata.name), (.metadata.namespace), (.spec.template.spec.initContainers[]?.image | split("\/") | .[-1]), (.spec.template.spec.containers[].image | split("\/") | .[-1] )] | @csv ' | java -jar jars/Formatter.jar Formatter

   }
}
```

Before executing the command make sure to set the KUBECONFIG environment variable on powershell using `$env.KUBECONFIG="/path/of/k8s/config"`

The output in powershell version less than 7.x

<img width="2276" height="702" alt="image" src="https://github.com/user-attachments/assets/d071533c-4120-4a91-9dc0-ace69b9924b0" />

The output in powershell version 7+

<img width="2218" height="714" alt="image" src="https://github.com/user-attachments/assets/bb7bf588-8d08-45af-9356-34f281dc68ba" />


In gitbash when executing the jbang with file directly the file will be compiled and command gets executed

<img width="2860" height="784" alt="image" src="https://github.com/user-attachments/assets/bdd75f43-483e-4fab-8fae-93a475c4d7c0" />

