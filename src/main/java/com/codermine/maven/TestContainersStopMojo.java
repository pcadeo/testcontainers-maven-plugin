package com.codermine.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

@Mojo(name = "stop-containers", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class TestContainersStopMojo extends AbstractMojo{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "dockerComposeFilePath",required = true)
    String dockerComposeFilePath;

    public void execute(){

        if(!project.isExecutionRoot()){
            getLog().info("Execution in submodule, skipping containers operation.");
        }

        getLog().info("Execution is root, executing container stop operation.");
        new DockerComposeContainer(new File(dockerComposeFilePath)).withLocalCompose(true).stop();
    }
}
