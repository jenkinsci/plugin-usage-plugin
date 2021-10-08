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

    private static final String IMAGE = "jenkins/jenkins:2.303.2";

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

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("freestyle1", "freestyle1.xml"), plugins -> client.getJobs().contains("freestyle1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(
                        new Project("freestyle1")
                ))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void conditionalSingle() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing conditional buildstep", () -> client.installPlugins("conditional-buildstep", "1.4.1"), plugins -> client.getInstalledPlugins().contains("conditional-buildstep"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("conditional-single1", "conditional-single1.xml"), plugins -> client.getJobs().contains("conditional-single1"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("conditional-single1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("conditional-single1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void conditionalMultiple() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing conditional buildstep", () -> client.installPlugins("conditional-buildstep", "1.4.1"), plugins -> client.getInstalledPlugins().contains("conditional-buildstep"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("conditional-multiple1", "conditional-multiple1.xml"), plugins -> client.getJobs().contains("conditional-multiple1"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("conditional-multiple1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("conditional-multiple1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void parameters() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing Git Parameter", () -> client.installPlugins("git-parameter", "0.9.13"), plugins -> client.getInstalledPlugins().contains("git-parameter"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("parameter1", "parameter1.xml"), plugins -> client.getJobs().contains("parameter1"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("git-parameter", "0.9.13"), Lists.newArrayList(new Project("parameter1")))
        ));
        assertEquals(expected, actual);
    }
}
