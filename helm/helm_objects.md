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
