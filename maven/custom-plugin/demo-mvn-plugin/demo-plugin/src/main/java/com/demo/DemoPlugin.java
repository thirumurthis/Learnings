package com.demo;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "demo",defaultPhase = LifecyclePhase.COMPILE)
public class DemoPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(property = "scope")
    String scope;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        List<Dependency> dependencies = project.getDependencies();

        long dependencyCount = dependencies.stream()
                .filter(dependency -> (scope == null || scope.isEmpty() || scope.equals(dependency.getScope())))
                .count();

        getLog().info("Dependency count "+ dependencyCount);

    }
}
