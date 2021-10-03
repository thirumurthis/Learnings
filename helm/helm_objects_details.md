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
- in the template folder you can create a Config.yaml file, and read the contents from the `config.toml` - works only for toml file
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

- `Files.Get` 
- `Files.GetBytes` - when dealing with binary data, like png,etc.
- `Files.Glob` - This function returns list of file that matches file patterns, we can loop through
- `Files.Lines` - To read line by line
- `Files.AsSecrets` - To encode with base64.
- `Files.AsConfig` - returns files content as yaml.

#### Values Object
 - The values are passed to template from the values.yaml file and from user supplied value files. Default most of the values will be empty.

#### Capabilities object
 - This provides information about the what capablities the kubernetes cluster supports.
 - `Capabilities.APIVersions` - provides the API versions 
 - `Capabilities.APIVersions.Has $version` - this returns whether the specific version is present in K8s or not
 - `Capabilities.KubeVersion.Major`
 - `Capabilities.KubeVersion.Minor`
 
#### Templates Object
  - Contains information about the current template that being executed.
  - `Name`
  - `Basepath`

## Template functions and pipeline
 - Template functions:
    - There are many built-in function which can be used to transform the data provided in values chart.
    - Syntax usage: `function_name arg1 arg2...`
    - Different template function and example:
    - `quote`, 
    - `upper`,
    - `lower`,
    - `nindent`,
    - `indent`,
    - `repeat`,
    - `include`, - similar to import in java
    - `required`, - makes the properties to be provided in Values.yaml as mandatory
    -  `default`, - adds a default value to the properties if not provided in Values.yaml the default value will be used
    -  `toYaml`, - the convert the content to yaml
    -  `eq` 
   ```
   #### values.yaml, has below content 
   favorite:
      car:
        - make : BMW

    ### using in template function-usage.yaml, etc
     apiVersion: v1
     kind: ConfigMap
     metadata:
        name:  {{ .Release.Name }}-configMap
     data:
       value1: "function example"
       car: {{ quote .Values.favourite.car.make }}  -------------> this will create a double quotes before and after the string
     
   ```
 - Pipelines
    - Like unix/Linux where the output of once command to another.
 ```
   #### values.yaml, has below content 
   favorite:
      car:
        - make : BMW

    ### using in template function-usage.yaml, etc
     apiVersion: v1
     kind: ConfigMap
     metadata:
        name:  {{ .Release.Name }}-configMap
     data:
       value1: "function example"
       car: {{ .Values.favourite.car.make | quote }}  -------------> this will create a double quotes before and after the string
  ```

  ### Flow control:
  - This is a control structure the ability to control the flow of template generation.
     - `if/else` - for conditional block creation 
     - `with` - to specify scope
     - `range` - a for-each type loop

 ###### If/else:
  - Basic structure:
  ```toml
   {{ if CONDITION1 }}
     # Perform someaction
   {{ else if CONDITION2 }}
     # Perform someaction
   {{ else }}
      # Default action
    {{ end }}
  ```
  - The CONDITION1 is set to false in case, if boolean false, zero, an empty string, a nil (empty or null), an empty collection (map,slice,tuple,dict,array)
  - Example:
```
   #### values.yaml, has below content 
   favorite:
      car:
        - make : BMW

    ### using in template function-usage.yaml, etc
     apiVersion: v1
     kind: ConfigMap
     metadata:
        name:  {{ .Release.Name }}-configMap
     data:
       value1: "function example"
       {{ if eq .Values.favourite.car.make "BMW" }} 
        car: true
       {{ end }}
  ```
   - NOTE: In above case the if block will print out in new line, since if itself takes line space, to fix that we can do below
   ```
   Option 1:
   {{- if eq .Values.favourite.car.make "BMW" }}
   
   {{- end}}
   
   Option2:
   **{{- if eq .Values.favourite.car.make "BMW" }}
   
   **{{- end}}
   ```

#### with
  - used for scope defintion
  - `.` is always current scope

```
{{ with VARIABLE }}
  ## restricted scope
{{ end }}   // When this statement is reached, the next will make the . scope is used.
```
  - example:

```
### values.yaml
favourite:
    car:
      model: bmw
      
### in template file
   {{- with .Values.favourite.car }}  ---------------> note that the values.yaml only top level of is used
      model: {{ .model | default "toyota" | upper | quote }}
   {{- end }}
```
#### Range
- to iterate through map, list, arrays, dict, etc.
- Example:
```
 ## values.yaml
 
 cars:
   - toyota
   - kia
   - honda
   - hyundai
   
 ## template yaml file
 
   cars: |- 
     {{- range .Values.cars }}  -----------------> Specifying the scope within the cars 
       - {{ . | title | quote }}
     {{- end }}
```

### Helm comments
   - Template commenting sytax that is not evaluated by Helm engine
   ```
   {{/* {{- with .Values.favourite.car.model }}
   
   {{- end }} */}}
   ```
   - Using yaml comment syntax `#`. but this will be printed and include in the templates as well.
   - Note the yaml comments are also evaluated.

### Variables
 - Variables are less fequently used in templates
 - used to simplify code, to make better use of `with` and `range`
 - format ` {{- $variableName := values -}}`
 - the variable can be accessed by any other scopes including current scope .
 ```
 {{- with .Values.favourite.car }}
   car: {{ .model | default "toyota" | upper | quote }}
   release : {{ .Release.Name }}  ----------------------- > This will fail since it is local scoped, cannot access global scope or ,
 {{- end }}
 ```
- The above can be fixed, by declaring the release in variables

```
{{- $relName := .Release.Name -}}
{{- with .Values.favourite.car }}
   car: {{ .model | default "toyota" | upper | quote }}
   release : {{ $relName }}   
 {{- end }}
 
 ## use command helm install --debug --dry-run app-release-v1 example2/ to render output
```
 #####  Using variable in `range`
```
## values.yaml
favourite:
    car:
      model: bmw

cars:
   - Toytoa
   - Ford
   - Honda
   - Kia
   
## simple template yaml
apiVersion: v1
kind: ConfigMap
metadata:
    name: {{ .Release.Name }}-configMap
data:
   {{- $relName := .Release.Name -}}
   {{- with .Values.favourite.car }}
     model: {{ .model | title| upper | quote }}
     release: {{ $relName }}
   {{- end }}
     cars: |-
       {{- range $index, $car := .Values.cars }}  -------------> dont use -}} it causes issue at the end
        {{ $index }}: {{ $car }}
       {{- end }}
```

### Named Templates 
 - `NOTES.txt` and `helpers.tpl` are not kubernetes specific files.
 - These files are not rendered as kubernetes manifests
 - Creating named templates:
    - name the file such a way to begins with an underscore
- Declaring and using templates

```
## to define

{{ define "MYTEMPLATE.NAME" }}
  # body of the template here
{{ end }}

## using it in tempalte file 
metadata:
   name: {{ .Release.Name }}-configMap
   {{- template "MYTEMPALTE.NAME" }}
```

- Example:

```
# custom named template - and scope is global
# defining the same named template mulitple times, the last will be used
{{- define "example2.lables" }}
  labels: 
     generator: helm
     date: {{ now | htmlDate }}
{{- end}}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ .Release.Name }}-cmap
  {{- template "example2.lables" }}
data: 
    {{- range $key, $val := .Values.cars }}
      {{ $key }}: {{ $val | quote}}
    {{- end }}

```
- output (note the labels should be spelled correctly
```
# Source: example2/templates/function.yaml
# custom named template - and scope is global
# defining the same named template mulitple times, the last will be used
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-release1-cmap
  labels:
     generator: helm
     date: 2021-10-03
data:
      0: "Toytoa"
      1: "Ford"
      2: "Honda"
      3: "Kia"
```
