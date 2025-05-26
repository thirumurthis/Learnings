package org.sample.mvn;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.sisu.Nullable;

import javax.inject.Inject;

/**
 * Example sample plugin version
 */
// annotation maven knows this is a mojo
@Mojo(name="echo",defaultPhase = LifecyclePhase.INITIALIZE)
public class SampleMvnPlugin extends AbstractMojo {

    /**
     * To run code, follow below steps
     * 1. install the project using below to local
     * $ mvn install
     * To run the plugin in local use below command in local
     * $ mvn {groupId}:{artifactId}:{goal}
     * $ mvn org.sample.mvn:sample-mvn-plugin:echo
     *
     * $ mvn org.sample.mvn:sample-mvn-plugin:echo
     * $ mvn org.sample.mvn:sample-mvn-plugin:echo -Dgit.command="git rev-parse --short HEAD"
     */

    // below is the command
    // in cli, this can be as -Dgit.command
    // in xml, this can be used as <command>
    /**
     * command used to print the git version
     */
    @Parameter(property = "git.command",defaultValue = "git rev-parse --short HEAD")
    private String command;

    //injecting maven project
    @Parameter(property = "project",readonly = true)
    private MavenProject project;

    private EchoHelper echoHelper;

    public SampleMvnPlugin(){
        this.echoHelper = new EchoHelperImpl();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String version = echoHelper.getVersion(command);

        // get the properties and add to the project
        project.getProperties().put("projVersion", version);
        getLog().info("Git hash version "+version);
    }

}
