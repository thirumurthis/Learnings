
```java
public class Crash{

final sstatic Unsafe UNSAFE = getUnsafe();

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

[oracle ref-1](https://www.oracle.com/technetwork/java/javase/felog-138657.html)

[Oracle ref-2](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/dumpfile.html)

[Other ref-3](http://fahdshariff.blogspot.com/2012/08/analysing-java-core-dump.html)

