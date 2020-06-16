
```java
public class Crash{

final static Unsafe UNSAFE = getUnsafe();

public static void chrash(int x){
  UNSAFE.putInt(0x99,x); // address 99 is not accessible which crash the program
}

public static void main(String argd []){
  crash(0x42);
}

}

```

```java
  public class Crashint{
  
  int[] a = new int[1];
  
  final static Unsafe UNSAGE = getUnsafe();
  
  public int crash(){
   retrun a.length();
   }
   
   public static void main(String [] args){
      ChrashInt c = new CrashInt();
      // writing the value this crash 
      Unsafe.putLong(c,12L , 0xbadbabe);
      c.crash();
    }
  }
```
`
$ javap -c class-name-with-package-name
`

```
The hs_err_pid<>.log contains Instruction section with a reference.

Use gdb or https://onlinedisassembler.com to convert the isntruction and see what happened.

if the segmentation fragment was J then it is Jit issue 
```
#### Out of Memory 

```
// out of memory in metaspace java 8+
using below will provide a complete error file and core file.
$ java -XX:+CrashOnOutOfMemoryError ...

Events .. in some case provide insight on what happened
```

##### Serviceability Agent to analyze the code file
```
// java 9 - java hotspot debugger(jhsdb)
$ jhsdb hsdb --exe  <path-to-java>/java --core <core-file-name>
 Will open and GUI
 
```
The API for hotspot debugger package `sun.jvm.hotspot.tools.Tool` can be used to access each C++ object in the hotspot.

for executing the agent enable the module in java9
```
$ java --add-modules jdk.hotspot.agent --add-exports .. <class name> <java-locaton> <core file name>

There should be only one represenation of CPP class represenation of JAVA in the heap.
```

##### hs_err_pid .log `internal exception` provides the last 10 exception event occured before vm crashed.

```java
// to reproduce the CPP class representation crash

public class CrashOutOfMemory{
  public static void main(int cnt) throws Exception{
     byte[] = getByteCodes("CrashOutOfMemory");
     
     // define custom class loader
     MyClassLoader mcl = new MyClassLoader();
     for(int i;i<cnt;i++){
       mcl.mydefineClass("<package>.CrashOutOfMemory",buf,0,buf.length);
       }
       catch(LinkageError le){
          if(i==0) thorw new Exception ("first iteration");
          }
       }
  }
  public static void main (String [] args) throws Exception{
    crash(args.length >0 ? Integer.parseInt(arg[0]:1000);
    }
}
```
##### crash in libjvm.so -> crash might caused during compilation
In this case there will be replay_pid file.
```
 // The below will be able to reproduce the error
 $ java -XX:+ReplayCompiles -XX:ReplayDataFile=/home/replay_pid<pid>.log
 // use the command opton DoEscapeAnalysis
 
 $ java -XX:+ReplayCompiles -XX:-DoEscapeAnalysis -XX:ReplayDataFile=/home/replay_pid<pid>.log

```

![image](https://user-images.githubusercontent.com/6425536/82088091-fc9ae000-96a5-11ea-96a9-6fd0bfc8a3dc.png)

------------------------
------------------------
### out of memory

  - Long GC pauses
  - Excessive GC 
  
##### Java Heap Memory
   - Full GC is not able to claim any space in Old Gen of the heap could be configuration issue
   - Heap size might be small
   
 Step to resolve:
    - Increase the heap size.
    Even after this there is a memory issue persist there is a possibility of memory leak.

If are able to connect to the running process, 
  - use Jconsole to the live process and monitor it. 

If `not` able to connect to the running process,
   - collect GC logs and then analyze, and see the usage of heap memory. 
   
   - To increase the heap size is `-Xmx` this is the maximum heap size option.
   - Recommendation is to set this value same as `-Xms` which sets the initial heap size.
   
Diagnoising the Heap :
  - GC logs
    - GC logs tells us about `Excessive GCs` and `Long GC pauses`
      - How to collect?
         - Java 9 :  G1: -Xlog:gc*, gc+phases=debug:file=garbagecollect.log
                     Non-G1: -Xlog:gc*:file=garbagecollect.log
         - older java:  -XX:+PrintGCDetails, -XX:+PrintGCTimeStamps, -XX:+PrintGCDateStamps, -Xloggc:<garbagecollect log file name>
   
Below image, the GC process is not able to free up Old gen memory space. This means the old gen memory size is not sufficient to hold the data at runtime.

![image](https://user-images.githubusercontent.com/6425536/82109410-6fc64580-96ea-11ea-965e-5c1e5486e02e.png)

Full GC's check the GC log for back to back full GC's.
   
GC logs showing long pauses, represented in the below image. in below case, it takes 50 sec for GC.
 
![image](https://user-images.githubusercontent.com/6425536/82109470-f4b15f00-96ea-11ea-8080-5ddf9723667a.png)
  
 -__`Heap dump`__
  - How to collect Heap dump?
     - use `-XX:+HeapDumpOnOutOfMemoryError` option when starting the java application.
     - Other tools like `jcmd <pid/mainclass> GC.head_dump dump.dmp`
       - `jmap -dump:format=b,file=snapshot.jmap <pid>`
       - Jconsole, usign MBean Hotspot diagnostic
       - Java Mission Control Hotspot diagnostics or MBean Diagnosticcommands
     - `parallel collector` can continuously collects or reclaim space in heap space, though returns are minimal. In this case we can instruct GC not to put much effort where gain is minimum. `parallel collector` delays application restart on its own.
       - We can set time limit, `-XX:GCTimeLimit` and `-XX:GCHeapFreeLimit`
           - `-XX:GCTimeLimit` => sets upper limit in amount of time the GC can spend in % of time (default value is 98%) Decreasing this value will reduce the time spent in GC.
           - `-XX:GCHeapFreeLimit` => sets a lower limit on amount of space that should be free after the GC operation, percentation of maximum heap. default 2%.  (increasing value means more heap space will be reclaimed during GC operation)
            - Adjusting the two options prevents back-to-back full GCs.
              
  - __`Heap Histogram`__ ( gives object in the heap)
    - How to collect it?
     - `-XX:+PrintClassHistogram` option when starting the java process and SIGQUIT on Posix platform, SIGBREAK on windows 
     - `jcmd <process id/mainclass> GC.class_histogram  filename=histofile`
     - `jmap -histo pid`
     - `jhsdb jmap` (option on java 9)
     - `java Mission Control`
  
![image](https://user-images.githubusercontent.com/6425536/82108797-2247d980-96e6-11ea-9218-24f0cbd70898.png)

 __`Explicit full GCs`__
   - is invoked by system.gc()
   - Warning: The diagnostic tool used not unknowingly creating explicit GC's.
      - if java app runs with -XX:+PrintclassHistogram, and upon sending SIGQUIT might trigger a Full GC Class histogram/Object histogram.
   - `-XX:+DisableExplicitGC` - will compeletly disable the explicit GC, when application started with this option.
   
   VisualVM is retired and moved out of JDK.
#### Memory leak with PermGen/Metaspace:

 PermGen - not available in Java 8+, the metaspace is used to store class related info.

`-XX:MetaspaceSize=m -XX:MaxMetaspaceSize=n` if this is not provided all of the native memory is allocated to the application or process.

##### Memory leak with `Compressed class space`:
`Compressed class space` - This is a part of metaspace, this is logical region inculded within the metaspace. Not physically though within metaspace.
  - If UseCompressedClassPointers is enabledm then two separate areas of `native memory` are used for class metadata.
      - By default `UseCompressedClassPointers` is on, if UseCompressedOops is ON 64 bit JVM.
   - 64 bit class pointers are represented by 32 bit offsets.
   - 32 bit offsets can be used to reference class-metadata stored in the compressed class space (CCS).
   - By default 1GB of address space is reserved of CCS, configured using CompressedClassSpaceSize.
   - `MaxMetaspaceSize` sets an upper limit on the total commited size on both of these regions. (commited size of CCS + commited size of Meta space <= MaxMetaspaceSize)

If we didn't use the CCS option, in java heap the object  which contains class pointers in 64 bit platform are represented using 64 bit pointers.

This 64 bit pointers is used to refer the class metadata in metaspace. when the CCS is enabled, then the class pointers are referred as 32 bit offset. This can be used refer class metadata in metaspace. 

Advantage is it save space in java heap since it is 32 bit offset.

`+UerCompressedClassPointers` - will be displayed in the Metaspace on th GC logs, with committed.

- How to collect the PermGen/MetaSapce diagnostic data?
   - along with the GC logs option include option `-verbose:class` when starting java applicaton or `-XX:+TraceClassLoading -XX:+TraceClassUnloading`.
   - `jmap -clstats` (JDK 8+)
   - Heap Dumps (analyze using `Eclipse MAT`)
   - `jcmd <pid> GC.class_stats` (JDK 8+)
 
Note: 
 - When collecting class loader related memory problem, the class needs to be unloaded make sure the option `-Xnoclassgc` option is NOT used. (using this option will not unload the class from the metaspace and it will reach max space quickly)
 - When using `-XX:+CMSClassUnloadingEnabled` (in java 6 or 7) This makes sure the classes are unloaded during concurrent marking cycles.
 

From the image below sample GC logs, the Out of memory didn't thorow due to Metaspace (where 6GB space is available only 4GB of it is utlized)

The out of memory in this case is due to CCS. There is no room to load more classes.

 ![image](https://user-images.githubusercontent.com/6425536/82111837-97bea480-96fc-11ea-85c4-3ae91d1b8d94.png)

###### Error `CodeCache` is full. Compiler has been disabled. (This is warning, but this caused performance issue, the application will not stop)
 - CodeCache is the memory pool to store the compiled code generated by the JIT compilers.
 - There is no specific OutOfMemoryError thrown when CodeCache is full.
 - An emergency CodeCache cleanup is done when it becomes full.
    - After cleanup all the classes will be compiled again.
 There will be performance degradation. The codecache size needs to be optimal.
 - The codecache size can be configured, using `ReservedCodeCacheSize` which will be maximum size.

##### Out of memory for Native memory

 - This out of memory issue is because JVM not able to allocate from native memory
 - The out of memory might due to other process on the system utilizing the memory.
 - Basically the JVM cannot make room for more native heap, possible fix would be to reduce the Java Heap, Metasapce, number of threads and/or their stck sized. Reducing the number of process running at that time.

Tools to use for Native memory (out of memory error) kind of 
  - possible this might be within the JVM
  - Else might be out side the JVM
  
![image](https://user-images.githubusercontent.com/6425536/82108848-866a9d80-96e6-11ea-9753-7226b61a1657.png)

Platform related tool like pmap, libumen, valgrid (outside java)

How to collect the data and analyse it
#### Native Memory tracker tool
   - is internal to the JVM, it can only track memory allocated by the JVM. (used internally by JVM)
   - It cann't track memory outside the JVM or by native libraries
  
  How to collect info?
  - Start the java process for this with `-XX:NativeMemoryTracking=summary` or `-XX:NativeMemoryTracking=detail`, the output level is summary or details.
  - Once started with that flag, then we can use the `jcmd <pid> VM.native_memory` to get the native memory usage detail.
  - This command outputs, the memroy usage of different component with the JVM like heap, compilerspace, etc. 
   
 Gives an idea of which area is growing more memory.
   
 The NMT output can be gathered at different stages of time like a snapshot, and compared with each other setting one as base line.
   
 Memory analyzer using dump, __`Eclipse MAT`__ is community tool.
   
`Explicit GC invocations is also a case of memory leak.`
   
-----------

[oracle ref-1](https://www.oracle.com/technetwork/java/javase/felog-138657.html)

[Oracle ref-2](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/dumpfile.html)

[Other ref-3](http://fahdshariff.blogspot.com/2012/08/analysing-java-core-dump.html)


[Link describing the native memory and metaspace vs heap info pictorical](https://stackoverflow.com/questions/39675406/difference-between-metaspace-and-native-memory-in-java)

[Link to metaspace](http://karunsubramanian.com/websphere/one-important-change-in-memory-management-in-java-8/)
