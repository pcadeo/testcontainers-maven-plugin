package com.codermine.maven.stub;

import org.apache.maven.Maven;

import java.io.File;

public class TestContainedProjectStub extends org.apache.maven.plugin.testing.stubs.MavenProjectStub {

    public TestContainedProjectStub(){
        readModel(new File("src/test/resources/unit/project-to-test/pom.xml"));
        setExecutionRoot(true);
    }
}
