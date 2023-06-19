Helm tricks and tips:

Generate set of secrets from Values with list of values


Setting up the chart

```
# helm create <chart-name>
$ helm create demo1
```

Using named-template

- Lets define a set of secrets in values.yaml file like below,

```yaml
secrets:
  frontend:
    app-name: my-fe-app
    database: false
  backend:
    app-name: my-be-app
    database: true
```

Create a template file for the secrets which will read the secrets from values file
- Name the file as `_secrets.yaml` with below content
  - the named-template is defined for secrets which started with define function
  - from the values.yaml file the secrets values is read and iterated to create the secret manifest
  - the file name for the named template starts with `_`.

```yaml
{{- define "demo1.secrets" -}}
{{- range $secName, $secMap := .Values.secrets }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ $secName }}
  labels:
    app.kubernetes.io/name: {{ $secName }}
    helm.sh/chart: {{ include "demo1.chart" $ }}
    app.kubernetes.io/managed-by: {{ $.Release.Service }}
type: Opaque
data:
{{- range $key, $val := $secMap }}
  {{ $key }}: {{ $val | b64enc }}
{{- end }}
---
{{- end -}}
{{- end -}}

```

- The `template\secrets\secrets.yaml` file content which will render the named template
  - Below is the only content that goes, since we are using named template reference
 
```yaml
{{ include "demo1.secrets" .}}
```

- To generate the templated output for specific file, use below command
  - Note: below command using .\template\secrets.yaml didn't work in powershell
 
```
> helm template . -s template\secrets.yaml
```

- output generated

```yaml
---
# Source: demo1/templates/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: backend
  labels:
    app.kubernetes.io/name: backend
    helm.sh/chart: demo1-0.1.0
    app.kubernetes.io/managed-by: Helm
type: Opaque
data:
  app-name: YmUtYXBw
  version-name: YmV0YS1yZWxlYXNl
---
# Source: demo1/templates/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: frontend
  labels:
    app.kubernetes.io/name: frontend
    helm.sh/chart: demo1-0.1.0
    app.kubernetes.io/managed-by: Helm
type: Opaque
data:
  app-name: ZmUtYXBw
  version-name: YWxwaGEtcmVsZWFzZQ==
```

- Create ConfigMap fetching data from configuration files.

- Like in Spring boot applicaiton, the configuration can be set in application.yaml, by including
  this in a folder, we can use helm to read those file using `File` function
  
We place the configuration file in a forlder `configuration`

- Below content in the file named `appconfig.yaml`

```yaml
appName: my-app
version: 1.0
```

- `application.yaml`

```yaml
server.port: 8090

management.endpoint.health.show-details: "ALWAYS"
management.endpoints.web.exposure.include: "*"
```

- the content of the `_configmap.yaml`

```yaml
{{- define "demo1.configmap_file" -}}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "demo1.fullname" . }}
  labels:
    app.kubernetes.io/name: {{ include "demo1.name" . }}
    helm.sh/chart: {{ include "demo1.chart" . }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
data:
{{ toYaml (.Files.Glob "configuration/*").AsConfig | indent 2 }}
{{- end -}}
```

- Define `template\configMap.yaml`, with the named tempalte

```yaml
{{ include "demo1.configmap_file" .}}
```


- helm template command

```
$ helm template . -s template\configMap.yaml
```

- Output 

```yaml
---
# Source: demo1/templates/configMap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: release-name-demo1
  labels:
    app.kubernetes.io/name: demo1
    helm.sh/chart: demo1-0.1.0
    app.kubernetes.io/managed-by: Helm
data:
  |-
    appconfig.yaml: "appName: my-app\r\nversion: 1.0\r\n"
    application.yaml: "server.port: 8090\r\n\r\nmanagement.endpoint.health.show-details:
      \"ALWAYS\"\r\nmanagement.endpoints.web.exposure.include: \"*\""
```

- Values to be override the values.yaml to be provided

```yaml
demo1:
   appName: demo-app
   chart: demo1-chart
```

- Override values in local named template

- Store the named template to _deployment.yaml
  - The metadata section includes, way's to print the overrided value

```yaml
{{/*
Simple defintion
*/}}
{{- define "demo1.deployment" -}}
{{- $demo1 := dict "Values" .Values.demo1 -}} 
{{- $noDemo1 := omit .Values "demo1" -}} 
{{- $overrides := dict "Values" $noDemo1 -}} 
{{- $noValues := omit . "Values" -}} 
{{- with merge $noValues $overrides $demo1 -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "demo1.fullname" . }}
  labels:
    {{- range $key, $val := $demo1.Values }}
    {{ $key }}: {{ $val | quote }}
    {{- end }}
    custom.label: {{ $demo1.Values.chart | quote}}
    debug.info: {{ printf "%s" ($demo1.Values.appName | quote) }}
    debug.statement: {{ $demo1.name | quote }}
    app.kubernetes.io/name: {{ include "demo1.name" . }}
    helm.sh/chart: {{ include "demo1.chart" . }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
{{- end -}}
```

- Output after executing the command

```
$ helm template . -s templates\deployment.yaml --debug
```

```yaml
---
# Source: demo1/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: release-name-demo1
  labels:
    appName: "demo-app"
    chart: "demo1-chart"
    custom.label: "demo1-chart"
    debug.info: "demo-app"
    debug.statement:
    app.kubernetes.io/name: demo1
    helm.sh/chart: demo1-0.1.0
    app.kubernetes.io/managed-by: Helm
```

The structure of the directory works,

```
C:\LESSON1\DEMO1
│   .helmignore
│   Chart.yaml
│   values.yaml
│
├───charts
├───configuration
│       appconfig.yaml
│       application.yaml
│
└───templates
    │   app_service.yaml
    │   configMap.yaml
    │   deployment.yaml
    │   NOTES.txt
    │   secrets.yaml
    │   _configmap.yaml
    │   _deployment.yaml
    │   _helpers.tpl
    │   _secrets.yaml
    │
    └───tests
            test-connection.yaml
```
- All content of the file

- _configmap.yaml

```yaml
{{- define "demo1.configmap_from_file" -}}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "demo1.fullname" . }}
  labels:
    app.kubernetes.io/name: {{ include "demo1.name" . }}
    helm.sh/chart: {{ include "demo1.chart" . }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
data:
{{ toYaml (.Files.Glob "config-repo/*").AsConfig | indent 2 }}
{{- end -}}
```

- _deployment.yaml

```yaml
{{/*
Simple defintion
*/}}
{{- define "demo1.deployment" -}}
{{- $demo1 := dict "Values" .Values.demo1 -}} 
{{- $noDemo1 := omit .Values "demo1" -}} 
{{- $overrides := dict "Values" $noDemo1 -}} 
{{- $noValues := omit . "Values" -}} 
{{- with merge $noValues $overrides $demo1 -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "demo1.fullname" . }}
  labels:
    {{- range $key, $val := $demo1.Values }}
    {{ $key }}: {{ $val | quote }}
    {{- end }}
    custom.label: {{ $demo1.Values.chart | quote}}
    debug.info: {{ printf "%s" ($demo1.Values.appName | quote) }}
    debug.statement: {{ $demo1.name | quote }}
    app.kubernetes.io/name: {{ include "demo1.name" . }}
    helm.sh/chart: {{ include "demo1.chart" . }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
{{- end -}}
```

- _secrets.yaml

```yaml
{{- define "demo1.secrets" -}}
{{- range $secretName, $secretMap := .Values.secrets }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ $secretName }}
  labels:
    app.kubernetes.io/name: {{ $secretName }}
    helm.sh/chart: {{ include "demo1.chart" $ }}
    app.kubernetes.io/managed-by: {{ $.Release.Service }}
type: Opaque
data:
{{- range $key, $val := $secretMap }}
  {{ $key }}: {{ $val | b64enc }}
{{- end }}
---
{{- end -}}
{{- end -}}
```

- app_service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ include "demo1.fullname" . }}
  labels: 
    app.kubernetes.io/name: {{ include "demo1.name" . }}
    helm.sh/chart: {{ include "demo1.chart" . }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
spec:
  type: {{ .Values.service.type }}
  ports:
{{ toYaml .Values.service.ports | indent 4 }}
  selector:
    app.kubernetes.io/name: {{ include "demo1.name" . }}
````

- configMap.yaml

```yaml
{{ include "demo1.configmap_from_file" .}}
```

- deployment.yaml

```yaml
{{ include "demo1.deployment" . }}
```

- secrets.yaml

```yaml
{{ include "demo1.secrets" .}}
```

- values.yaml

```yaml
secrets:
  frontend:
    app-name: fe-app
    version-name: alpha-release
  backend:
    app-name: be-app
    version-name: beta-release

demo1:
   appName: demo-app
   chart: demo1-chart

service:
  type: NodePort
  ports:
  - port: 443
    targetPort: 8443
    nodePort: 30443
```

- configuration/appconfig.yaml

```yaml
appName: my-app
version: 1.0
```

- configuration/application.yaml

```yaml
server.port: 8090

management.endpoint.health.show-details: "ALWAYS"
management.endpoints.web.exposure.include: "*"
```
