## Set Git Bash profile in Windows Terminal and enable kubectl auto completion

### Pre-requisites

#### Docker Desktop Kubernetes cluster

  - The new version of Docker Desktop for Windows provides feature to enable Kubernetes cluster
  - For this blog I have enabled the Docker K8S cluster, this is very easy to enable
      - From Docker Desktop settings under Kubernetes, enable it. Clicking Apply which will install the cluster
      - Use the `wsl -l` to check if the ubutnu distro is installed. If not install it from the Microsoft Store.
      - Also, I had configured WSL2 Ubuntu 22.04 Distribution instead of using the Docker Default WSL
      - The Ubuntu distribution can be set in the Docker Desktop from Settings -> Resources -> WSL
      - The advantage is that we can mount volumes from WSL to K8S cluster

#### kubectl 

- `kubectl` installed in the machine
   - In my case I installed the kubectl using  **Chocolatey** windows package manager
   - Open command prompt as administrator user and issue the command `> choco install kubernetes-cli`

#### Git Bash CLI 
 
- Git Bash CLI can be installed form the GitHub website
  
#### Windows Terminal

- `Window Terminal` installed in the machine
    - Windows Terminal is a application like PowerShell, Bash, cmd prompt, etc. It support multiple browser and we can create profiles and perform customizations.
    - To learn more about check the [Microsoft Learn documentation](https://learn.microsoft.com/en-us/windows/terminal/)          

    - Installation steps:
       - We can install `Windows Terminal` directly from the Microsoft Store, just search and install it.
       - To install it manually, download the bundle `WindowsTerminal*.msixbundle` from the GitHub release page. Refer the [GitHub terminal project link](https://github.com/microsoft/terminal)
          - Issue the command `Add-AppxPackage <downloaded-msixbundle>` 
          - Manual installation requires `VC++ v14 Desktop Framework Package` to be installed separately

### Create a profile for Git Bash in Windows Terminal
  
-  Open Windows Terminal, click the v near the tab + symbol. 

    -  1. Select the `Settings` 
    -  2. Click the `Add a new profile`
 
![image](https://user-images.githubusercontent.com/6425536/192129820-00d3e7a1-2768-4295-8dd8-68894d292692.png)

- Below snapshot displays the values I used to create the profile.
   - We need to provide the path of the Git Bash.exe
   - We can add the path to the windows icon if needed
   - I unchecked the source path, which was selected by default. In my case I wanted the git bash to point to userprofile path when opened in Windows Terminal
   - I left the title blank, other options also blank
   - Click Save

![image](https://user-images.githubusercontent.com/6425536/192128107-7b652bb4-dc1c-44b1-a5d5-dcec3d50eb34.png)

- Click the `+` near the tabs at the top of Windows Terminal, now it will list the newly created GitBash profile.

![image](https://user-images.githubusercontent.com/6425536/192128283-3cbe7897-a182-4f74-a0e8-8cee2df1f4d8.png)

 - Once the Git Bash is opened in Windows terminal, lets try `kubectl` command to list the context from `kubeconfig`.
- Command to list the context is `kubectl config get-contexts`, I had multiple context, refer snapshot below

![image](https://user-images.githubusercontent.com/6425536/192130241-0b84b744-8407-4080-8299-dba2ce4a0a17.png)


### Instruction to set Kubectl alias and auto completion feature 

- I am setting up the kubectl alias as k
 - Navigate to `C:\Program Files\Git\etc\profile.d` in windows
 - Open the `alias.sh` file and update the below contents to the bottom of the file

```
alias k=kubectl
source <(kubectl completion bash)
complete -o default -F __start_kubectl k
```

- Alternatively we can create a `.bashrc` file under the `C:/Users/<user-name>/` (open cmd prompt and use the command `echo %USERPROFILE%` to display the current user path this where we need to create the .bashrc file and copy paste the content if the file doesn't exists) 

### Validate kubectl alias and auto completion works

 - Open up Windows Terminal, click `+` near the tabs on top, select the GitBash Profile
 - Type `k -n kube-s` and hit tab, you should see the namespace getting auto-filled in this case `kube-system`.

![image](https://user-images.githubusercontent.com/6425536/192129151-bd7704e9-b960-4431-951d-57a5a69cc22a.png)

  - After hitting tab with ,get pods, below result is displayed

![image](https://user-images.githubusercontent.com/6425536/192129192-677faa8e-0def-471f-b960-3db86c9415ae.png)

- The auto complete feature will also work on the pod names, for example

```
# Create a simple nginx pod
$ k run nginx --image=nginx

# Type, below command and hit tab if nginx is the only pod it will auto complete it.
$ k -n default logs pod/n
```
