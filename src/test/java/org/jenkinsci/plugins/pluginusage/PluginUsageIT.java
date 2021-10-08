package org.jenkinsci.plugins.pluginusage;

import com.google.common.collect.Lists;
import org.jenkinsci.plugins.pluginusage.api.Plugin;
import org.jenkinsci.plugins.pluginusage.api.PluginProjects;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;
import org.jenkinsci.plugins.pluginusage.api.Project;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static org.jenkinsci.plugins.pluginusage.ExponentialBackoffStrategy.attempt;
import static org.junit.Assert.assertEquals;

public class PluginUsageIT {

    private static final String IMAGE = "jenkins/jenkins:2.138.4";

    @Rule
    public GenericContainer jenkins = new GenericContainer(DockerImageName.parse(IMAGE))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/"))
            .withEnv("JAVA_OPTS",
                    "-Djenkins.install.runSetupWizard=false " +
                    "-Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

    @Test
    public void freestyle() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt(client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt(client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt(() -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt(() -> client.createJob("freestyle1", "freestyle1.xml"), plugins -> client.getJobs().contains("freestyle1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(
                        new Project("freestyle1")
                ))
        ));
        assertEquals(expected, actual);
    }


}
