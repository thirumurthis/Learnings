// class unzip the file from starter.spring.io and loads to intellij idea

// alias juof='java /path/of/java/Unzip.java $1'

import java.io.*;
import java.util.function.*;
import java.util.*;


void main(String ... args) throws Exception{

    if(args.length <= 0){
        System.out.println("No arguments passed!");
        System.exit(-1);
    }
    var zFile = new File(args[0]);
    var fullPath = zFile.getAbsolutePath();
    var basePath = zFile.getParent();
    var fldr = new File(fullPath.substring(0,fullPath.lastIndexOf(".")));

    IO.println("The zip full path "+fullPath+ " will unzip to "+ basePath);
    
    //unzip /path/of/zip -d /path/dir
    ProcessBuilder processBuilder = new ProcessBuilder().command("unzip",fullPath,"-d",basePath);
    processBuilder.inheritIO().start().waitFor();

    String ideaExe="C:\\Users\\thiru\\AppData\\Local\\Programs\\IntelliJ IDEA Community Edition\\bin\\idea64.exe";
    for (var f : Set.of("pom.xml","build.gradle")){
        var buildFile = new File(fldr, f);
        if(buildFile.exists()){
            IO.println("Opening file in IDEA location - "+buildFile.getParent());
            ProcessBuilder pBuilder =  new ProcessBuilder()
            //.command("cmd.exe","/c","start","cmd")
            //.directory(buildFile.getParent())
            .command(ideaExe,buildFile.getParent());
            pBuilder.redirectErrorStream(true)
            .inheritIO()
            .start()
            .waitFor();
        }
    }
}