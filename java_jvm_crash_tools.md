
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



![image](https://user-images.githubusercontent.com/6425536/82088091-fc9ae000-96a5-11ea-96a9-6fd0bfc8a3dc.png)



[oracle ref](https://www.oracle.com/technetwork/java/javase/felog-138657.html)

[Oracle ref](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/dumpfile.html)

[ref1](http://fahdshariff.blogspot.com/2012/08/analysing-java-core-dump.html)

