package com.k8s.mcp.k8s.service;

import com.k8s.mcp.k8s.K8sController;
import com.k8s.mcp.k8s.data.K8sNamespaceInfo;
import com.k8s.mcp.k8s.data.K8sPodInfo;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class K8sService {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sService.class);

    ApiClient client;
    CoreV1Api api;
    public K8sService() {
        try {
            client = Config.defaultClient(); // for local use
        } catch (IOException e) {
            LOGGER.error("exception occurred accessing client ",e);
            throw new RuntimeException(e);
        }
        Configuration.setDefaultApiClient(client);
       api = new CoreV1Api();
    }

    @Tool(name="get_pods_from_given_namespace",
            description = "This function will return the list of pods for a given namespace from the kubernetes cluster")
    public K8sPodInfo getPods(String namespace){


        K8sPodInfo podInfo;
        if (namespace == null || namespace.isBlank()){
            namespace = "default";
        }
        try {
            V1PodList podList = api.listNamespacedPod(namespace).execute();
            List<String> podNames = podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .toList();
            podInfo = new K8sPodInfo(namespace,podNames);
        } catch (ApiException e) {
            LOGGER.error("Exception listing pod",e);
            throw new RuntimeException(e);
        }

        return podInfo;
    }
    @Tool(name="get_all_pods_in_cluster",
            description = "This function will list all the pods from the cluster")
    public List<K8sPodInfo> getAllPodsFromCluster(){

        List<K8sPodInfo> allPodInfo = new ArrayList<>();
        try{
            Map<String,List<String>> nsPodNames = new HashMap<>();

            V1PodList podList = api.listPodForAllNamespaces().execute();
            podList.getItems().forEach(pod -> {
                                String podName = pod.getMetadata().getName();
                                String ns = pod.getMetadata().getNamespace();
                                nsPodNames.putIfAbsent(ns, new ArrayList<String>());
                                nsPodNames.get(ns).add(podName);
                            }
                    );


           nsPodNames.forEach((ns,pods)->{
               allPodInfo.add(new K8sPodInfo(ns,pods));

           });

        } catch (ApiException e) {
            LOGGER.error("Exception accessing all pods from namespace", e);
            throw new RuntimeException(e);
        }
        return allPodInfo;
    }

    @Tool(name="get_all_namespace",
    description = "This function will return list of the namespaces from the kubernetes cluster")
    public K8sNamespaceInfo getNamespaces(){
        K8sNamespaceInfo k8sNamespaceInfo;
        try {
            V1NamespaceList namespaceList = api.listNamespace().execute();
            List<String> nameSpaces = namespaceList.getItems().stream()
                    .map( ns -> ns.getMetadata().getName())
                    .toList();
            k8sNamespaceInfo = new K8sNamespaceInfo(nameSpaces);
        } catch (ApiException e) {
            LOGGER.error("Exception fetching namespace ",e);
            throw new RuntimeException(e);
        }
        return k8sNamespaceInfo;
    }
}
