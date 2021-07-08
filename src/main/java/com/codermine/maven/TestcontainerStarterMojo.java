package com.codermine.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = "start-containers", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class TestcontainerStarterMojo extends AbstractMojo{

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "dockerComposeFilePath",required = true)
    String dockerComposeFilePath;

    @Parameter(property = "useLocalDockerCompose")
    String useLocalDockerCompose = "true";

    public void execute() throws MojoExecutionException {

        if ("pom".equals(project.getPackaging())) {
            getLog().debug("Ignoring pom packaging.");
            return;
        }

        if(!project.isExecutionRoot()){
            getLog().info("Execution in submodule, skipping containers operation.");
        }

        getLog().info("Execution is root, executing container start operation.");

        final Map<String,Integer> servicePortsMap = new HashMap<>();
        try (InputStream inputStream = new FileInputStream(dockerComposeFilePath)){
            final Map<String, Object> obj = new Yaml().load(inputStream);
            final LinkedHashMap<String,LinkedHashMap> services = (LinkedHashMap<String, LinkedHashMap>) obj.get("services");
            services.keySet().stream().forEach(serviceKey -> {
                List<String> ports = (List) services.get(serviceKey).get("ports");
                String portStr = ports.get(0).split(":")[1];
                servicePortsMap.put(serviceKey,Integer.parseInt(portStr));
            });
        } catch (IOException e) {
            throw new MojoExecutionException( String.format("Error while parsing yaml docker-compose file on path ´%s´",dockerComposeFilePath),e);
        }

        DockerComposeContainer dockerComposeContainer = new DockerComposeContainer(new File(dockerComposeFilePath));

        servicePortsMap.keySet().stream().forEach( service -> {
            getLog().info(String.format("Add listen to service ´%s´ port ´%d´ configured in docker-compose.yml.", service, servicePortsMap.get(service) ) );
            dockerComposeContainer.withExposedService(service, servicePortsMap.get(service), Wait.forListeningPort() );
        });

        dockerComposeContainer.withLocalCompose(Boolean.valueOf(useLocalDockerCompose)).start();
    }
}
