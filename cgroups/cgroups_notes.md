### control groups (cgroups)

cgroups v2 is the latest

- on boot, systemd mounts v2 hierarchy at `/sys/fs/cgroup` (or `/sys/fs/cgroup/unified`).
- the pusedo file system is mounted on the above location (one of the location)
- the file system type is `cgroup2`
- The cgroup version2 is sometime known as `unified` hierarchy. Because groups are associated to one hierarchy.


to view the version, below command should return 1.

```
grep -c cgroup  /proc/mounts
```

What are control groups?
Principle:
  - A mechanism to hierarically grouping processes (tree of cgroups)
  - A Set of controllers (kernel components) that manage, control and monitor process in cgroups
     - some kind of control over the group of process
  - interface is done using pesudo-filesystem
  - cgroup manipulation takes form of filesystem operation 
     - via shell command
     - programmatically
     - via management daemon (ex. systemd)
     - via containrs frameworks tools like docker, LXC

what to do with cgroups? what do cgroups allow to do?

- Limit resource usage of group
   - like limit % of CPU available to group, limit amount of memory that group can use
- Prioritize group for resource allocation
   - like favour the group for network bandwidth
- Resource accounting
   - like measure resources used by process, how much memory it is consuming
- Freeze a group
   - Freeze, restore and checkpoint a group

Terminology:
- Control group: a group of process that are being bound together for the purpose of resource management.
- (Resource) controller: kernel components that controls or monitors process in a cgroup
   - eg: memory contorller limits memory usage cpu controller limits CPU usage
   - Also known as subsystem
- cgroup are arranged in hierarchy
   - each cgroup can have zero or more child cgroups
   - child cgroup inherit control settings from parents

File system interface:
- cgroup filesystem directory structure defines cgroups + cgroup hierarchy 
  - i.e. use mkdir/ rmdir(equivalent shell command) to create cgroup
- each subdirectory contains automatically created files 
   - some are used to manage the cgroup itself
   - some are controller-sepecific
- Files in cgroup used to
   - define/display membership of cgroup
   - control behaviour of process in cgroup 
   - Expose information about process in cgroup (like resource usage status)


PID controller: (hands-on)
```
$ cd /sys/fs/cgroup

$ sudo bash 
$ mkdir mycgrp

$ cat /sys/fs/cgroup/cgroup.controllers
cpuset cpu io memory hugetlb pids rdma

# to check if the PID controller is enabled
$ cat /sys/fs/cgroups/cgroup.subree_control
cpuset cpu io memory hugetlb pids rdma

# we are going to move the current shell to that cgroup
# that is done by writting a PID inside a file on the directory
$ echo $$ > mycgrp/cgroup.procs

$ cat /proc/$$/cgroup
0::/myscgrp

# to read the pids of the member process.
# lists the pid of child process and the shell process
$ cat mycgrp/cgroup.procs
<list pid>
# echo $$
<list pid>

$ cat mycgrp/pids.current

# to list the max number of child process in cgroup
$ cat mycgrp/pids.max
max

# override the number of max process to 5
$ echo 5 > mycgrp/pids.max

# run below command to see the process is limited to 5
$ for j in (seq 1 5); do sleep 60 & done 
# there will be error and it will try couple of time and gives up


# from the root pid (and different shell) we can list the number of pids created by the for loop
# make sure the run the look and below command on another shell will display 5 
$ cat /sys/fs/cgroup/mycgrp/pids.current 

# to remove the cgroup remove the directory, below ill not allow to delete it will say device busy
$ rmdir /sys/fs/cgroup/mycgrp

# if the above command from previous shell where the mycgrp directory was created, first move the current shell to parent cgroup
$ echo $$ > /sys/fs/cgroup/cgroups.proc
# after executing the above command, we can be able to rmdir
```

Note, the first attempt to delete the cgroup created was not deleting because shell is a member of cgroup we are trying to remove. so we move the shell to the root cgroup and try deleting. It is not necessary to delete the files inside that directory before hand.

Example from the online from linuxcontiner.org

```
demo@incus-os:~$ mkdir mycgrp
demo@incus-os:~$ echo $$ > mycgrp/cgroup.procs
demo@incus-os:~$ cat /proc/$$/cgroup
0::/system.slice/incus-agent.service
demo@incus-os:~$ echo $$ > mycgrp/cgroup.procs
demo@incus-os:~$ cat /proc/$$/cgroup
0::/system.slice/incus-agent.service
demo@incus-os:~$ cat /proc/$$/cgroup
0::/system.slice/incus-agent.service
demo@incus-os:~$ cat mycgrp/cgroup.proccs
cat: mycgrp/cgroup.proccs: No such file or directory
demo@incus-os:~$ cat mycgrp/cgroup.procs 
1477
demo@incus-os:~$ echo $$
1477
```

There are different controllers

 - cpu controller - limit and measure CPU usage by a group of process;
   Cpu controller operates on two modes: 
      - propotional-weight division (default)
      - bandwidth control
   These modes can be intermingle at different levels in hierarchy

Propotional-weight division

```
                       /  (root)
                       |
    ----------------------------------------------
   |                   |                          |
   a                   b                          c
  shares=1024        shares=2048                 shares=1024
                       |
               -----------------
               |                |
               x                y
            (sahres=1000)      (sahres=4000)
               

```
   `cpu.weight` - file in each group defines relative share of CPU received by that group
   Example: denominator (A + B + C )
      Proccesses in b get 2048 / (1024 +2048 +1024) = 1/2 of cpu time
      Processes in a and c each get  1024/(1024+2048+1024) = 1/4 of cpu time
      Processes in x get 2048/(1024+2048+1024) * 1000/(1000+4000) = 1/2 * 1/5 = 1/10  of cpu time
      Processes in y get 2048/(1024+2048+1024) * 4000/(1000+4000) = 1/2 * 4/5 = 4/10  of cpu time

      out of the total, the cgroups based in the weight (shares) will get the total time of cpu for that process

Bandwidth 

```
                       A  
                   (quota=50000)
                       |
    ----------------------------------------------
   |                   |                          |
   p                   q                          r
  quota=20000       quota=40000                 quota=10000
                       |
               ---------
               |                
               x                
            (quota=30000)      
               
```

- Bandwith control strictly limits CPU (quota/period) granted to a group (even if no other competitors for cpu)
- assume that period is 100000 in all cgroups
- process under A will get max of 50% of one cpu
- process under q will get max of 40% of the cpu
- sibilin cgroups under A are oversubscribed (won't get 70% of cpu)

cpuset
- cpuset control cpu and memory affinity
  - Pin cgroup to one cpu/subset of cpu (or memory nodes)
  - Dynamically manage placement of application components on system with large number of cpus and memory nodes
     - non-uniform memory access (NUMA) systems

