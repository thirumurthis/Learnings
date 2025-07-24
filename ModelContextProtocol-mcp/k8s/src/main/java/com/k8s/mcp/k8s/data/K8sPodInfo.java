package com.k8s.mcp.k8s.data;

import java.util.List;

public record K8sPodInfo(String namespace, List<String> podNames) {
}
