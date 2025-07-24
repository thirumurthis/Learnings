package com.k8s.mcp.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@RestController
//@RequestMapping("/v1")
public class K8sController {

    private static Logger log = LoggerFactory.getLogger(K8sController.class);

    ApiClient client;
   public K8sController() {
       try {
           client = Config.defaultClient(); // for local use
       } catch (IOException e) {
           log.error("exception occurred accessing client ",e);
           throw new RuntimeException(e);
       }
       Configuration.setDefaultApiClient(client);
   }

    //@GetMapping("/pod")
    public List<String> getPods(@RequestParam(required = false) String namespace){

        // get the pods from the backend
        CoreV1Api api = new CoreV1Api();
        List<String> podNames;
        if (namespace == null || namespace.isBlank()){
            namespace = "default";
        }
        try {
            V1PodList podList = api.listNamespacedPod(namespace).execute();
             podNames = podList.getItems().stream().map(
                    item -> item.getMetadata().getName())
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (ApiException e) {
            log.error("exception listing pod",e);
            throw new RuntimeException(e);
        }

        return podNames;
    }
}
