

if ($null -eq $env:KUBECONFIG) {
	Write-Host ""
    Write-Host "[KUBECONFIG variable does not exists or is empty]"
	Write-Host "In powershell $env:KUBECONFIG=\"path/to/kube/config\""
} else {
 kubectl get deployment -A -o json | jq -r ' .items[] | [(.metadata.name), (.metadata.namespace), (.spec.initContainers[]?.image | split(\"/\") | .[-1]), (.spec.template.spec.containers[].image | split(\"/\") | .[-1] )] | @csv ' | java -jar jars/Formatter.jar Formatter
}