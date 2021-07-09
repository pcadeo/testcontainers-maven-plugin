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

    @Parameter(property = "autoListenDeclaredServices")
    String autoListenDeclaredServices = "true";

    @Parameter(property = "servicesToListen")
    Map<String,String> servicesToListen = new HashMap<>();

    public void execute() throws MojoExecutionException {

        if ("pom".equals(project.getPackaging())) {
            getLog().debug("Ignoring pom packaging.");
            return;
        }

        if(!project.isExecutionRoot()){
            getLog().info("Execution in submodule, skipping containers operation.");
            return;
        }

        getLog().info("Execution is root, executing container start operation.");

        try (InputStream inputStream = new FileInputStream(dockerComposeFilePath)){

            final Map<String,Integer> servicesPortListenContext = new HashMap<>();
            final DockerComposeContainer dockerComposeContainer = new DockerComposeContainer(new File(dockerComposeFilePath));

            addAutoParsedListenToContext(servicesPortListenContext, inputStream);
            addCustomListenToContext(servicesPortListenContext);

            servicesPortListenContext.keySet().stream().forEach( service -> {
                getLog().info(String.format("Add listen to service ´%s´ port ´%d´ configured in docker-compose.yml.", service, servicesPortListenContext.get(service) ) );
                dockerComposeContainer.withExposedService(service, servicesPortListenContext.get(service), Wait.forListeningPort() );
            });

            dockerComposeContainer.withLocalCompose(Boolean.valueOf(useLocalDockerCompose)).start();

        } catch (IOException e) {
            throw new MojoExecutionException( String.format("Error while parsing yaml docker-compose file on path ´%s´",dockerComposeFilePath),e);
        }
    }

    private void addAutoParsedListenToContext(Map<String, Integer> servicePortsMap, InputStream inputStream) {
        if( Boolean.valueOf(autoListenDeclaredServices) ) {
            final Map<String, Object> obj = new Yaml().load(inputStream);
            final LinkedHashMap<String, LinkedHashMap> services = (LinkedHashMap<String, LinkedHashMap>) obj.get("services");
            services.keySet().stream().forEach(serviceKey -> {
                List<String> ports = (List) services.get(serviceKey).get("ports");
                String portStr = ports.get(0).split(":")[1];
                servicePortsMap.put(serviceKey, Integer.parseInt(portStr));
            });
        }
    }

    private void addCustomListenToContext(Map<String, Integer> servicePortsMap) {

        if( servicesToListen.isEmpty() ) { return; }

        servicesToListen.keySet().forEach(
                service -> {
                    getLog().info(String.format("Custom configuration listen to service ´%s´ and port ´%s´", service, servicesToListen.get(service)));
                    servicePortsMap.put(service, Integer.valueOf(servicesToListen.get(service)));
                });
    }
}
