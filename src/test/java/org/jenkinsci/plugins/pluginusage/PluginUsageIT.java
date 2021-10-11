package org.jenkinsci.plugins.pluginusage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList()),
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
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList()),
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
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git-parameter", "0.9.13"), Lists.newArrayList(new Project("parameter1"))),
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList())
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void promotions() throws MalformedURLException, URISyntaxException {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing promoted-builds", () -> client.installPlugins("promoted-builds", "3.10"), plugins -> client.getInstalledPlugins().contains("promoted-builds"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("promotion-job1", "promotion-job1.xml"), plugins -> client.getJobs().contains("promotion-job1"), maxTimeBackoffMillis);

        final URL url = client.getBaseURLBuilder().setPath("job/promotion-job1/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "promotion-job1-CI-process1.xml");

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("promoted-builds", "3.10"), Lists.newArrayList(new Project("promotion-job1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("promotion-job1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void buildWrappers() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing timestamper", () -> client.installPlugins("timestamper", "1.13"), plugins -> client.getInstalledPlugins().contains("timestamper"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("timestamper1", "timestamper1.xml"), plugins -> client.getJobs().contains("timestamper1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("timestamper", "1.13"), Lists.newArrayList(new Project("timestamper1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("timestamper1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void publishers() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing junit", () -> client.installPlugins("junit", "1.53"), plugins -> client.getInstalledPlugins().contains("junit"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("publisher1", "publisher1.xml"), plugins -> client.getJobs().contains("publisher1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList(new Project("publisher1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void publishersPromotions() throws URISyntaxException, MalformedURLException {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing junit", () -> client.installPlugins("junit", "1.53"), plugins -> client.getInstalledPlugins().contains("junit"), maxTimeBackoffMillis);
        attempt("installing promoted-builds", () -> client.installPlugins("promoted-builds", "3.10"), plugins -> client.getInstalledPlugins().contains("promoted-builds"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("publisher2", "publisher2.xml"), plugins -> client.getJobs().contains("publisher2"), maxTimeBackoffMillis);

        final URL url = client.getBaseURLBuilder().setPath("job/publisher2/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "publisher2-CI-process1.xml");

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList(new Project("publisher2"))),
                new PluginProjects(
                        new Plugin("promoted-builds", "3.10"), Lists.newArrayList(new Project("publisher2")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void scm() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing git", () -> client.installPlugins("git", "4.9.0"), plugins -> client.getInstalledPlugins().contains("git"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("scm1", "scm1.xml"), plugins -> client.getJobs().contains("scm1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList(new Project("scm1"))),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList())
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void maven() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 3 * 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven1", "maven1.xml"), plugins -> client.getJobs().contains("maven1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void trigger() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 3 * 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing urltrigger", () -> client.installPlugins("urltrigger", "0.49"), plugins -> client.getInstalledPlugins().contains("urltrigger"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("trigger1", "trigger1.xml"), plugins -> client.getJobs().contains("trigger1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("urltrigger", "0.49"), Lists.newArrayList(new Project("trigger1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void pipeline() {

        JenkinsClient client = new JenkinsClient(jenkins.getMappedPort(8080));
        final int maxTimeBackoffMillis = 3 * 60 * 1000;

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);

        attempt("installing pipeline-model-definition", () -> client.installPlugins("pipeline-model-definition", "1.9.2"), plugins -> client.getInstalledPlugins().contains("pipeline-model-definition"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("pipeline1", "pipeline1.xml"), plugins -> client.getJobs().contains("pipeline1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("cloudbees-folder", "6.16"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-basic-steps", "2.24"), Lists.newArrayList(new Project("pipeline1"))),
                new PluginProjects(
                        new Plugin("pipeline-model-definition", "1.9.2"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-cps", "2.94"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-input-step", "2.12"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-multibranch", "2.26"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-durable-task-step", "2.40"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-scm-step", "2.13"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-cps-global-lib", "2.21"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList())
        ));
        assertEquals(expected, actual);
    }
}
