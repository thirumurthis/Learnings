
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
            -`-XX:GCHeapFreeLimit` => sets a lower limit on amount of space that should be free after the GC operation, percentation of maximum heap. default 2%.  (increasing value means more heap space will be reclaimed during GC operation)
            - Adjusting the two options prevents back-to-back full GCs.
              
  - __`Heap Histogram`__ ( gives object in the heap)
    - How to collect it?
     - `-XX:+PrintClassHistogram` option when starting the java process and SIGQUIT on Posix platform, SIGBREAK on windows 
     - `jcmd <process id/mainclass> GC.class_histogram  filename=histofile`
     - `jmap -histo pid`
     - `jhsdb jmap` (option on java 9)
     - `java Mission Control`
  
![image](https://user-images.githubusercontent.com/6425536/82108797-2247d980-96e6-11ea-9218-24f0cbd70898.png)

##### Tools to use for Native memory (out of memory error)
  
  - possible this might be within the JVM
  
  - Else might be out side the JVM
  
![image](https://user-images.githubusercontent.com/6425536/82108848-866a9d80-96e6-11ea-9753-7226b61a1657.png)

Platform related tool like pmap, libumen, valgrid (outside java)

Native Memory tracker :
   - is internal to the JVM, it can only track memory allocated by the JVM. (used internally by JVM)
   - It cann't track memory outside the JVM or by native libraries
  
  How to collect info?
  Start the java process for this with `-XX:NativeMemoryTracking=summary` or `-XX:NativeMemoryTracking=detail`, the output level is summary or details.
   
 Once started with that flag, then we can use the `jcmd <pid> VM.native_memory` to get the native memory usage detail.
   
 This command outputs, the memroy usage of different component with the JVM like heap, compilerspace, etc. 
   
 Gives an idea of which area is growing more memory.
   
 The NMT output can be gathered at different stages of time like a snapshot, and compared with each other setting one as base line.
   
 Memory analyzer using dump, __`Eclipse MAT`__ is community tool.
   
`Explicit GC invocations is also a case of memory leak.`
   

[oracle ref-1](https://www.oracle.com/technetwork/java/javase/felog-138657.html)

[Oracle ref-2](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/dumpfile.html)

[Other ref-3](http://fahdshariff.blogspot.com/2012/08/analysing-java-core-dump.html)

