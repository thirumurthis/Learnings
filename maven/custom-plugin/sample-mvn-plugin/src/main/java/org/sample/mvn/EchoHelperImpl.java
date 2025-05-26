package org.sample.mvn;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

// inject from jsr-330
// note this is equivalent to spring @Component
//@Named
//@Singleton
public class EchoHelperImpl implements EchoHelper{

    @Override
    public String getVersion(String command) throws MojoExecutionException {

        StringBuffer sb = new StringBuffer();
        try{
            Process process = Runtime.getRuntime().exec(command);

            Executors.newSingleThreadExecutor()
                    .submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream()))
                            .lines()
                            .forEach(sb::append));
            int exitCode = process.waitFor();

            if(exitCode != 0){
                throw new MojoExecutionException("Exception occurred executing command "+command + "with exit code: "+exitCode);
            }
        } catch (IOException | MojoExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
