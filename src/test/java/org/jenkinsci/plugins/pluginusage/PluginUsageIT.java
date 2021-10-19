package org.jenkinsci.plugins.pluginusage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.collect.Lists;
import org.jenkinsci.plugins.pluginusage.api.Plugin;
import org.jenkinsci.plugins.pluginusage.api.PluginProjects;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;
import org.jenkinsci.plugins.pluginusage.api.Project;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static hudson.Functions.isWindows;
import static org.jenkinsci.plugins.pluginusage.ExponentialBackoffStrategy.attempt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

public class PluginUsageIT {

    private static final String IMAGE = "jenkins/jenkins:2.303.2";

    @Rule
    public GenericContainer jenkins = new GenericContainer(DockerImageName.parse(IMAGE))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/"))
            .withEnv("JAVA_OPTS",
                    "-Djenkins.install.runSetupWizard=false " +
                    "-Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

    private JenkinsClient client;
    private final int maxTimeBackoffMillis = 5 * 60 * 1000;

    @BeforeClass
    public static void setupAll(){
        assumeFalse(isWindows());
    }

    @Before
    public void setup(){
        client = new JenkinsClient(jenkins.getMappedPort(8080));

        attempt("waiting for available plugins", client::getAvailablePlugins, plugins -> !plugins.isEmpty(), maxTimeBackoffMillis);
    }

    @Test
    public void freestyle() {

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

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
    public void mavenPreBuilders() {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven2", "maven2.xml"), plugins -> client.getJobs().contains("maven2"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven2"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven2")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void mavenPostBuilders() {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven3", "maven3.xml"), plugins -> client.getJobs().contains("maven3"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven3"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven3")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void mavenParameter() {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing Git Parameter", () -> client.installPlugins("git-parameter", "0.9.13"), plugins -> client.getInstalledPlugins().contains("git-parameter"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven4", "maven4.xml"), plugins -> client.getJobs().contains("maven4"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git-parameter", "0.9.13"), Lists.newArrayList(new Project("maven4"))),
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven4")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void mavenSingleConditionalBuilder() {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing conditional buildstep", () -> client.installPlugins("conditional-buildstep", "1.4.1"), plugins -> client.getInstalledPlugins().contains("conditional-buildstep"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven5", "maven5.xml"), plugins -> client.getJobs().contains("maven5"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("maven5"))),
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven5"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven5")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void mavenMultiConditionalBuilder() {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing conditional buildstep", () -> client.installPlugins("conditional-buildstep", "1.4.1"), plugins -> client.getInstalledPlugins().contains("conditional-buildstep"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven6", "maven6.xml"), plugins -> client.getJobs().contains("maven6"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("maven6"))),
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven6"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven6")))
        ));
        assertEquals(expected, actual);
    }


    @Test
    public void mavenPromotions() throws URISyntaxException, MalformedURLException {

        attempt("installing maven", () -> client.installPlugins("maven-plugin", "3.13"), plugins -> client.getInstalledPlugins().contains("maven-plugin"), maxTimeBackoffMillis);
        attempt("installing promoted-builds", () -> client.installPlugins("promoted-builds", "3.10"), plugins -> client.getInstalledPlugins().contains("promoted-builds"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("maven7", "maven7.xml"), plugins -> client.getJobs().contains("maven7"), maxTimeBackoffMillis);

        final URL url = client.getBaseURLBuilder().setPath("job/maven7/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "maven7-CI-process1.xml");

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("promoted-builds", "3.10"), Lists.newArrayList(new Project("maven7"))),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.13"), Lists.newArrayList(new Project("maven7"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven7")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void trigger() {

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

    @Test
    public void pipeline2() {

        attempt("installing pipeline-model-definition", () -> client.installPlugins("pipeline-model-definition", "1.9.2"), plugins -> client.getInstalledPlugins().contains("pipeline-model-definition"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("pipeline2", "pipeline2.xml"), plugins -> client.getJobs().contains("pipeline2"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("cloudbees-folder", "6.16"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-basic-steps", "2.24"), Lists.newArrayList()),
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
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("pipeline2")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void matrix() {

        attempt("installing matrix-project", () -> client.installPlugins("matrix-project", "1.19"), plugins -> client.getInstalledPlugins().contains("matrix-project"), maxTimeBackoffMillis);
        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("matrix1", "matrix1.xml"), plugins -> client.getJobs().contains("matrix1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("matrix-project", "1.19"), Lists.newArrayList(new Project("matrix1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("matrix1")))
        ));
        assertEquals(expected, actual);
    }

    @Test
    public void otherProjects() {

        attempt("installing visual basic 6", () -> client.installPlugins("visual-basic-6", "1.4"), plugins -> client.getInstalledPlugins().contains("visual-basic-6"), maxTimeBackoffMillis);
        attempt("installing coordinator", () -> client.installPlugins("coordinator", "1.4.0"), plugins -> client.getInstalledPlugins().contains("coordinator"), maxTimeBackoffMillis);

        attempt("installing plugin-usage", client::installPluginUsage, plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"), maxTimeBackoffMillis);

        attempt("creating job", () -> client.createJob("freestyle1", "freestyle1.xml"), plugins -> client.getJobs().contains("freestyle1"), maxTimeBackoffMillis);
        attempt("creating job", () -> client.createJob("coordinator1", "coordinator1.xml"), plugins -> client.getJobs().contains("coordinator1"), maxTimeBackoffMillis);

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("coordinator", "1.4.0"), Lists.newArrayList(
                        new Project("coordinator1")
                )),
                new PluginProjects(new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(
                        new Project("freestyle1")
                ))
        ));
        assertEquals(expected, actual);
    }
}
