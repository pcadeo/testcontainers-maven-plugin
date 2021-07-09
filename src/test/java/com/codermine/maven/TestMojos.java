package com.codermine.maven;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.codehaus.plexus.ContainerConfiguration;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestMojos {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void shouldStopAndStartConfiguredContainers() throws Exception {

        rule.setupContainerConfiguration();
        File pom = getTestFile( "src/test/resources/unit/project-to-test/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        TestcontainerStarterMojo starterMojo = (TestcontainerStarterMojo) rule.lookupMojo( "start-containers", pom );
        assertNotNull( starterMojo );
        starterMojo.execute();

        TestContainersStopMojo stopMojo = (TestContainersStopMojo) rule.lookupMojo( "stop-containers", pom );
        assertNotNull( stopMojo );
        stopMojo.execute();
    }

}
