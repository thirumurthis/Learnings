#### helm objects

Resources:

- Create a chart using `helm create example1`
- Issue install command `helm install [Resource-name] --debug --dry-run [chart-folder]` in our case
  ` helm install my-app --debug --dry-run example1/`

- In `_helpers.tpl`, the `Release.Name` is the value passed in the helm install command, `my-app`
- `fullnameOverride` - was not specified in `values.yaml` file (this was note changed for this explanation)
```
{{- define "example1.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

```
- Sample deployment yaml name:
```
###  In template 
metadata:
  name: {{ include "example1.fullname" . }}
  labels:
    {{- include "example1.labels" . | nindent 4 }}

### After install dry-run, console prints below 
kind: Deployment
metadata:
  name: my-app-example1  -----------------------------------> <Resource.Name>-<Chart.Name> - in this case 
  labels:
    helm.sh/chart: example1-0.1.0

```
 - `Release.Name`  - built-in object
 - `Release.Namespace` - will have the namespace which the chart is deployed. using `--namespace` will be used if not specified in the chart, the namespace will be where we are executing the charts.
 - `Release.isUpgrade` - this is set to true if the current operation is upgrade. If this is an upgrade we can use in charts.
 - `Release.isInstall` - this value is set to true, if the current operation is install. 
 - `Release.Revision` - this has the revision number. On first time the release number is 1. for upgrade, rollback the release will be incremeted
 - `Release.Serivce` - this has the value of which service is rendered in the template, this service name of the rendered will be set. 

#### Charts object
- The contents of the Chart.yaml file will accessible via this object.
```
### Chart.yaml
apiVersion: v2
name: example1              ------------------------------> This is the Chart.Name = example1
description: A Helm chart for Kubernetes


### _helpers.tpl
{{- define "example1.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}
```

### Files object
 - This provides access to all non-special files within the template.
 - For example, we can create our own file (eg. config.ini) within the charts folder
 - `NOTES.txt`,`_helpers.txt` - are files which have special meaning.
 - In this case we have to use some file with configuration.

- Under the example1 folder, create `config.toml`, and include some content, say my-config. (don't create inside chart folder)
- in the template folder you can create a Config.yaml file, and read the contents from the `config.toml` 
   - with below approach only files from the chart folder can be accessed, any files within template folder cannot be accessed.
```
apiVersion: v1
kind: ConfigMap
metadata:
   name: {{ .Release.Name }}-configMap
data: 
  simple: |-
         {{ .Files.Get  "config.toml" }}             ---------------> This is how to refere the content of the files
 
```
