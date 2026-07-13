### Utility tool - Format Kubernetes image info with JQ and JBang


Was working on an productivity tool to list the image from Kuberentes in clean and simple table format.

In this article, have used `jq` utility and `JBang` with java code to read the inputstream and added logic to format the output.

The convienence here is the outputs can be piped easily. The jq is used to filter the deployment name, namespace and image info (both initContainers and container images).


Pre-requisites:
  - JBang CLI
  - Kubectl 
  - KinD 
  
To create a powershell script, the JBang code is compiled to jar and used it in script. When using in gitbash the java class file can be directly used.

In gitbash command prompt the output of kubectl is piped to Jq and finally jbang Formatter code. The output snapshot is at the bottom of this article.

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

<img width="2268" height="706" alt="image" src="https://github.com/user-attachments/assets/a856f138-6bf1-4711-9657-07fd18cf84a5" />

The output in powershell version 7+

<img width="2146" height="706" alt="image" src="https://github.com/user-attachments/assets/8ddded70-0485-4235-a791-378eaf8622a0" />

In gitbash when executing the jbang with file directly the file will be compiled and command gets executed

<img width="2860" height="784" alt="image" src="https://github.com/user-attachments/assets/bdd75f43-483e-4fab-8fae-93a475c4d7c0" />

