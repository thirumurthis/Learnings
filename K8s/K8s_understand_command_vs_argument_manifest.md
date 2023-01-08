### Define a command and arguments when you create a Pod
When you create a Pod, you can define a command and arguments for the containers that run in the Pod. To define a command, include the command field in the configuration file. To define arguments for the command, include the args field in the configuration file. The command and arguments that you define cannot be changed after the Pod is created.

The command and arguments that you define in the configuration file override the default command and arguments provided by the container image. If you define args, but do not define a command, the default command is used with your new arguments.

> **NOTE**
> The command field corresponds to entrypoint in some container runtimes. Refer to the Notes below.
> 
> This table summarizes the field names used by Docker and Kubernetes.
> Description	Docker field name	Kubernetes field name
> | The command run by the container	| Entrypoint | 	command |
> |--------------|-----|--------|
> |The arguments passed to the command| 	Cmd	| args |


When you override the default Entrypoint and Cmd, these rules apply:

- If you do not supply `command` or `args` for a Container, the defaults defined in the Docker image are used.
- If you supply a `command` but no `args` for a Container, only the supplied command is used. The default EntryPoint and the default Cmd defined in the Docker image are ignored.
- If you supply only `args` for a Container, the default Entrypoint defined in the Docker image is run with the args that you supplied.
- If you supply a `command` and `args`, the default Entrypoint and the default Cmd defined in the Docker image are ignored. Your `command` is run with your `args`.

| Image Entrypoint	| Image Cmd	| Container command	 | Container args |	Command run |
|--------|----------|------------|---------|----------|
| [/ep-1]	| [foo bar] |	<not set> |	<not set> |	[ep-1 foo bar] |
| [/ep-1]	| [foo bar]	| [/ep-2]	  | <not set>	| [ep-2] |
| [/ep-1]	| [foo bar]	| <not set>	| [zoo boo]	| [ep-1 zoo boo] |
| [/ep-1]	| [foo bar]	| [/ep-2]	  | [zoo boo]	| [ep-2 zoo boo] |
  

 Example:
  
  In this exercise, you create a Pod that runs one container. The configuration file for the Pod defines a command and two arguments:
  
  ```yaml
  apiVersion: v1
kind: Pod
metadata:
  name: command-demo
  labels:
    purpose: demonstrate-command
spec:
  containers:
  - name: command-demo-container
    image: debian
    command: ["printenv"]
    args: ["HOSTNAME", "KUBERNETES_PORT"]
  restartPolicy: OnFailure
  ```
  
 - The output shows the values of the HOSTNAME and KUBERNETES_PORT environment variables:
  
```
command-demo
tcp://10.3.240.1:443
```
  
### Use environment variables to define arguments
In the preceding example, you defined the arguments directly by providing strings. As an alternative to providing strings directly, you can define arguments by using environment variables:

```
env:
- name: MESSAGE
  value: "hello world"
command: ["/bin/echo"]
args: ["$(MESSAGE)"]
```
This means you can define an argument for a Pod using any of the techniques available for defining environment variables, including ConfigMaps and Secrets.

> **Note:**
> The environment variable appears in parentheses, "$(VAR)". This is required for the variable to be expanded in the command or args field.
>
  
### Run a command in a shell
In some cases, you need your command to run in a shell. For example, your command might consist of several commands piped together, or it might be a shell script. To run your command in a shell, wrap it like this:

```
command: ["/bin/sh"]
args: ["-c", "while true; do echo hello; sleep 10;done"]
```
  
 
