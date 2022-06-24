
public class GetRuntime{
  
 public static void main(String...args){
    Runtime runtime = Runtime.getRuntime();
    NumberFormat format = NumberFormat.getInstance();
    long maxMemory = runtime.maxMemory();
    long allocatedMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long mbsize = 1024*1024;
    System.out.println ("Free Memory " + format.format(freememory / mbsize) + "MB");
   
 }
  
}
