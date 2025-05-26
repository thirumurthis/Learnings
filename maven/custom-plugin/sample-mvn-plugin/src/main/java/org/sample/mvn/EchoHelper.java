package org.sample.mvn;

import org.apache.maven.plugin.MojoExecutionException;

public interface EchoHelper {

    String getVersion(String command) throws MojoExecutionException;
}
