# testcontainers-maven-plugin
Maven plugin for testcontainers library

- this plugin starts and stop a docker-compose file;
- this plugin will stop on JVM process exit, with or **without** calling stop goal;
- this plugin will run only in parent pom project;
- this plugin has a feature of auto listening to declared services on docker-compose.yml "services" collection, you can disable it by **autoListenDeclaredServices** configuration, see below;
- this plugin can configure manually some port's listen to some services;
- this plugin will run, by default, on **verify** maven phase;
- this is an example configuration:
```xml
<plugin>
        <groupId>com.codermine.maven</groupId>
        <artifactId>testcontainers-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
          <dockerComposeFilePath>src/test/resources/docker-compose.yml</dockerComposeFilePath>
          <useLocalDockerCompose>true</useLocalDockerCompose>
          <autoListenDeclaredServices>false</autoListenDeclaredServices>
          <servicesToListen>
            <redis>6379</redis>
            <grafana>3000</grafana>
            <rabbit>15672</rabbit>
          </servicesToListen>
        </configuration>
        <executions>
          <execution>
            <id>start-test-containers</id>
            <goals>
              <goal>start-containers</goal>
            </goals>
          </execution>
          <execution>
            <id>stop-test-containers</id>
            <goals>
              <goal>stop-containers</goal>
            </goals>
          </execution>
        </executions>
</plugin>
```

- this is enough, bye friend.
