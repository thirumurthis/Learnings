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

/**
* Formatter.java
*
kubectl get deployment -A -o json | jq -r ' .items[] | [(.metadata.name), (.metadata.namespace), (.spec.initContainers[]?.image | split("/") | .[-1]), (.spec.template.spec.containers[].image | split("/") | .[-1] )] | @csv ' | jbang Formatter.java
  
output: 
"apicurio-registry-operator-v3.2.1","apicurio-registry","apicurio-registry-3-operator:3.2.1"
"sample-app-deployment","apicurio-registry","apicurio-registry:3.2.1"

Use below command for development
echo '"apicurio-registry-operator-v3.2.1","apicurio-registry","apicurio-registry-3-operator:3.2.1"' |jbang Formatter.java
echo '"apicurio-registry-operator-v3.2.1","apicurio-registry","apicurio-registry-3-operator:3.2.1"' |java -jar jars/Formatter.jar Formatter

Use to compile and create jar
$ mkdir -p jars/classes

$ javac -d jars/classes Formatter.java
$ jar cvfe jars/Formatter.jar Formatter -C jars/classes .

In powershell set env vriable of kind running in local wsl2
$env:KUBECONFIG=\"\\wsl$\Ubuntu-26.04\home\<user>\.kube\config"

*/

public class Formatter {

    static int[] fieldLengthArray = {0,0,0,0};
    static int SPACING = 2;
    static int FIELD_LENGTH = 4;

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
            System.out.format(resultOutputFormat,"DEPLOYMENT NAME","NAMESPACE","IMAGE NAME","VERSION");
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

                for(String key: img.keySet()){
                    fieldLengthArray[0] = Math.max(fieldLengthArray[0], item.deploymentName.length());
                    fieldLengthArray[1] = Math.max(fieldLengthArray[1], item.namespace.length());   
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
