package org.jenkinsci.plugins.pluginusage;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.jenkinsci.plugins.pluginusage.api.Plugin;
import org.jenkinsci.plugins.pluginusage.api.PluginProjects;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;
import org.jenkinsci.plugins.pluginusage.api.Project;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static hudson.Functions.isWindows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

public class PluginUsageIT {

    private static final String IMAGE = "jenkins/jenkins:2.452.4";

    @Rule
    public Timeout timeout = Timeout.builder().withTimeout(5, TimeUnit.MINUTES).build();

    @Rule
    public GenericContainer<?> jenkins = new GenericContainer<>(DockerImageName.parse(IMAGE))
            .withLogConsumer(frame -> System.out.println(frame.getUtf8StringWithoutLineEnding()))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/"))
            .withCopyFileToContainer(MountableFile.forClasspathResource("update-center.2.452.4.json"), "/tmp/update-center.json")
            .withEnv("JAVA_OPTS",
                    "-Djenkins.install.runSetupWizard=false " +
                    "-Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true " +
                    "-Dhudson.model.UpdateCenter.updateCenterUrl=file:///tmp/ ");

    private JenkinsClient client;

    private final Duration maxTimeBackoff = Duration.ofMinutes(3);

    Function<PluginUsage, Set<String>> pluginNamesExtractor = p -> p.getJobsPerPlugin()
            .stream()
            .map(PluginProjects::getPlugin)
            .map(Plugin::getShortName)
            .collect(Collectors.toSet());

    Function<PluginUsage, Map<String, List<Project>>> projectsExtractor = p -> p.getJobsPerPlugin()
            .stream()
            .collect(Collectors.toMap(x -> x.getPlugin().getShortName(), PluginProjects::getProjects));

    @BeforeClass
    public static void setupAll(){
        assumeFalse(isWindows());
    }

    @Before
    public void setup(){
        client = new JenkinsClient(jenkins.getMappedPort(8080));

        attempt("waiting for available plugins",
                client::getAvailablePlugins,
                plugins -> !plugins.isEmpty());
    }

    @Test
    public void freestyle() {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("freestyle1", "freestyle1.xml"),
                plugins -> client.getJobs().contains("freestyle1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(
                        new Project("freestyle1")
                ))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void conditionalSingle() {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing conditional buildstep",
                () -> client.installPlugins("conditional-buildstep", "1.4.1"),
                plugins -> client.getInstalledPlugins().contains("conditional-buildstep"));

        attempt("creating job",
                () -> client.createJob("conditional-single1", "conditional-single1.xml"),
                plugins -> client.getJobs().contains("conditional-single1"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("conditional-single1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("conditional-single1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void conditionalMultiple() {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing conditional buildstep",
                () -> client.installPlugins("conditional-buildstep", "1.4.1"),
                plugins -> client.getInstalledPlugins().contains("conditional-buildstep"));

        attempt("creating job",
                () -> client.createJob("conditional-multiple1", "conditional-multiple1.xml"),
                plugins -> client.getJobs().contains("conditional-multiple1"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("conditional-buildstep", "1.4.1"), Lists.newArrayList(new Project("conditional-multiple1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("conditional-multiple1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void parameters() {

        attempt("installing Git Parameter",
                () -> client.installPlugins("git-parameter", "0.9.13"),
                plugins -> client.getInstalledPlugins().contains("git-parameter"));

        attempt("creating job",
                () -> client.createJob("parameter1", "parameter1.xml"),
                plugins -> client.getJobs().contains("parameter1"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git-parameter", "0.9.13"), Lists.newArrayList(new Project("parameter1"))),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList())
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void promotions() throws MalformedURLException, URISyntaxException {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing promoted-builds",
                () -> client.installPlugins("promoted-builds", "3.10"),
                plugins -> client.getInstalledPlugins().contains("promoted-builds"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("promotion-job1", "promotion-job1.xml"),
                plugins -> client.getJobs().contains("promotion-job1"));

        final URL url = client.getBaseURLBuilder().setPath("job/promotion-job1/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "promotion-job1-CI-process1.xml");

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("promoted-builds", "3.10"), Lists.newArrayList(new Project("promotion-job1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("promotion-job1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void buildWrappers() {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing timestamper",
                () -> client.installPlugins("timestamper", "1.13"),
                plugins -> client.getInstalledPlugins().contains("timestamper"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("timestamper1", "timestamper1.xml"),
                plugins -> client.getJobs().contains("timestamper1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("timestamper", "1.13"), Lists.newArrayList(new Project("timestamper1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("timestamper1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void publishers() {

        attempt("installing junit",
                () -> client.installPlugins("junit", "1.53"),
                plugins -> client.getInstalledPlugins().contains("junit"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("publisher1", "publisher1.xml"),
                plugins -> client.getJobs().contains("publisher1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList(new Project("publisher1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void publishersPromotions() throws URISyntaxException, MalformedURLException {

        attempt("installing junit",
                () -> client.installPlugins("junit", "1.53"),
                plugins -> client.getInstalledPlugins().contains("junit"));
        attempt("installing promoted-builds",
                () -> client.installPlugins("promoted-builds", "3.10"),
                plugins -> client.getInstalledPlugins().contains("promoted-builds"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("publisher2", "publisher2.xml"),
                plugins -> client.getJobs().contains("publisher2"));

        final URL url = client.getBaseURLBuilder().setPath("job/publisher2/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "publisher2-CI-process1.xml");

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList(new Project("publisher2"))),
                new PluginProjects(
                        new Plugin("promoted-builds", "3.10"), Lists.newArrayList(new Project("publisher2")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void scm() {

        attempt("installing git",
                () -> client.installPlugins("git", "4.9.0"),
                plugins -> client.getInstalledPlugins().contains("git"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("scm1", "scm1.xml"),
                plugins -> client.getJobs().contains("scm1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList(new Project("scm1"))),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList())
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void scmSource() {

        attempt("installing gitlab-branch-source",
                () -> client.installPlugins("gitlab-branch-source", "710.v6f19df32544b_"),
                plugins -> client.getInstalledPlugins().contains("gitlab-branch-source"));
        attempt("installing workflow-multibranch",
                () -> client.installPlugins("workflow-multibranch", "773.vc4fe1378f1d5"),
                plugins -> client.getInstalledPlugins().contains("workflow-multibranch"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("scmSource1", "scmSource1.xml"),
                plugins -> client.getJobs().contains("scmSource1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("git", "4.9.0"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("cloudbees-folder", "6.16"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("gitlab-branch-source", "710.v6f19df32544b_"), Lists.newArrayList(new Project("scmSource1"))),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-multibranch", "2.26"), Lists.newArrayList(new Project("scmSource1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void maven() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven1", "maven1.xml"),
                plugins -> client.getJobs().contains("maven1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void mavenPreBuilders() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven2", "maven2.xml"),
                plugins -> client.getJobs().contains("maven2"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven2"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven2")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void mavenPostBuilders() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven3", "maven3.xml"),
                plugins -> client.getJobs().contains("maven3"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("javadoc", "1.6"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven3"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven3")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void mavenParameter() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing Git Parameter",
                () -> client.installPlugins("git-parameter", "0.9.13"),
                plugins -> client.getInstalledPlugins().contains("git-parameter"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven4", "maven4.xml"),
                plugins -> client.getJobs().contains("maven4"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven4")))
        ));

        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void mavenSingleConditionalBuilder() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing conditional buildstep",
                () -> client.installPlugins("conditional-buildstep", "1.4.1"),
                plugins -> client.getInstalledPlugins().contains("conditional-buildstep"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven5", "maven5.xml"),
                plugins -> client.getJobs().contains("maven5"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven5"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven5")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void mavenMultiConditionalBuilder() {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing conditional buildstep",
                () -> client.installPlugins("conditional-buildstep", "1.4.1"),
                plugins -> client.getInstalledPlugins().contains("conditional-buildstep"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven6", "maven6.xml"),
                plugins -> client.getJobs().contains("maven6"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven6"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven6")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }


    @Test
    public void mavenPromotions() throws URISyntaxException, MalformedURLException {

        attempt("installing maven",
                () -> client.installPlugins("maven-plugin", "3.15"),
                plugins -> client.getInstalledPlugins().contains("maven-plugin"));
        attempt("installing promoted-builds",
                () -> client.installPlugins("promoted-builds", "3.10"),
                plugins -> client.getInstalledPlugins().contains("promoted-builds"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("maven7", "maven7.xml"),
                plugins -> client.getJobs().contains("maven7"));

        final URL url = client.getBaseURLBuilder().setPath("job/maven7/promotion/createProcess").setParameter("name", "CI").build().toURL();
        client.postFile(url, "maven7-CI-process1.xml");

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("maven-plugin", "3.15"), Lists.newArrayList(new Project("maven7"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("maven7")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void trigger() {

        attempt("installing urltrigger",
                () -> client.installPlugins("urltrigger", "0.49"),
                plugins -> client.getInstalledPlugins().contains("urltrigger"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("trigger1", "trigger1.xml"),
                plugins -> client.getJobs().contains("trigger1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("urltrigger", "0.49"), Lists.newArrayList(new Project("trigger1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void pipeline() {

        attempt("installing pipeline-model-definition",
                () -> client.installPlugins("pipeline-model-definition", "1.9.3"),
                plugins -> client.getInstalledPlugins().contains("pipeline-model-definition"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("pipeline1", "pipeline1.xml"),
                plugins -> client.getJobs().contains("pipeline1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("pipeline-model-definition", "1.9.3"), Lists.newArrayList()),
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
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-groovy-lib", "593.va_a_fc25d520e9"), Lists.newArrayList())
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void pipeline2() {

        attempt("installing pipeline-model-definition",
                () -> client.installPlugins("pipeline-model-definition", "1.9.3"),
                plugins -> client.getInstalledPlugins().contains("pipeline-model-definition"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("pipeline2", "pipeline2.xml"),
                plugins -> client.getJobs().contains("pipeline2"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

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
                        new Plugin("pipeline-model-definition", "1.9.3"), Lists.newArrayList()),
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
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-groovy-lib", "593.va_a_fc25d520e9"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("pipeline2")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void scriptedPipeline() {

        attempt("installing pipeline-model-definition",
                () -> client.installPlugins("pipeline-model-definition", "1.9.3"),
                ignore -> client.getInstalledPlugins().contains("pipeline-model-definition"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                ignore -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing junit",
                () -> client.installPlugins("junit", "1.54"),
                ignore -> client.getInstalledPlugins().contains("junit"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                ignore -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("scripted-pipeline", "scripted-pipeline.xml"),
                ignore -> client.getJobs().contains("scripted-pipeline"));

        attempt("trigger job",
                () -> client.triggerJob("scripted-pipeline"),
                ignore -> client.hasLastCompletedBuild("scripted-pipeline"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("junit", "1.54"), Lists.newArrayList(new Project("scripted-pipeline"))),
                new PluginProjects(
                        new Plugin("checks-api", "1.54"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("cloudbees-folder", "6.16"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-basic-steps", "2.24"), Lists.newArrayList(new Project("scripted-pipeline"))),
                new PluginProjects(
                        new Plugin("pipeline-model-definition", "1.9.3"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-cps", "2.94"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-input-step", "2.12"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-multibranch", "2.26"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-durable-task-step", "2.40"), Lists.newArrayList(new Project("scripted-pipeline"))),
                new PluginProjects(
                        new Plugin("workflow-scm-step", "2.13"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList(new Project("scripted-pipeline"))),
                new PluginProjects(
                        new Plugin("pipeline-groovy-lib", "593.va_a_fc25d520e9"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("scripted-pipeline")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void matrix() {

        attempt("installing matrix-project",
                () -> client.installPlugins("matrix-project", "1.19"),
                plugins -> client.getInstalledPlugins().contains("matrix-project"));
        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("matrix1", "matrix1.xml"),
                plugins -> client.getJobs().contains("matrix1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("junit", "1.53"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("matrix-project", "1.19"), Lists.newArrayList(new Project("matrix1"))),
                new PluginProjects(
                        new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(new Project("matrix1")))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void computedFolder() {

        attempt("installing pipeline-model-definition",
                () -> client.installPlugins("pipeline-model-definition", "1.9.3"),
                plugins -> client.getInstalledPlugins().contains("pipeline-model-definition"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("multibranch pipeline1", "multibranch_pipeline1.xml"),
                plugins -> client.getJobs().contains("multibranch pipeline1"));
        attempt("creating job",
                () -> client.createJob("multibranch pipeline2", "multibranch_pipeline2.xml"),
                plugins -> client.getJobs().contains("multibranch pipeline2"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(
                        new Plugin("credentials-binding", "1.27"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("cloudbees-folder", "6.16"),
                        Lists.newArrayList(new Project("multibranch pipeline2"))),
                new PluginProjects(
                        new Plugin("mailer", "1.34"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-basic-steps", "2.24"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-model-definition", "1.9.3"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-cps", "2.94"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-input-step", "2.12"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-multibranch", "2.26"),
                        Lists.newArrayList(
                                new Project("multibranch pipeline1"),
                                new Project("multibranch pipeline2"))),
                new PluginProjects(
                        new Plugin("workflow-durable-task-step", "2.40"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("workflow-scm-step", "2.13"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-stage-step", "2.5"), Lists.newArrayList()),
                new PluginProjects(
                        new Plugin("pipeline-groovy-lib", "593.va_a_fc25d520e9"), Lists.newArrayList())
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    @Test
    public void otherProjects() {

        attempt("installing visual basic 6",
                () -> client.installPlugins("visual-basic-6", "1.4"),
                plugins -> client.getInstalledPlugins().contains("visual-basic-6"));
        attempt("installing coordinator",
                () -> client.installPlugins("coordinator", "1.4.0"),
                plugins -> client.getInstalledPlugins().contains("coordinator"));

        attempt("installing plugin-usage",
                client::installPluginUsage,
                plugins -> client.getInstalledPlugins().contains("plugin-usage-plugin"));

        attempt("creating job",
                () -> client.createJob("freestyle1", "freestyle1.xml"),
                plugins -> client.getJobs().contains("freestyle1"));
        attempt("creating job",
                () -> client.createJob("coordinator1", "coordinator1.xml"),
                plugins -> client.getJobs().contains("coordinator1"));

        attempt("executing plugin usage work",
                () -> client.triggerPluginUsage(),
                plugins -> !client.getPluginUsage().getJobsPerPlugin().isEmpty());

        PluginUsage actual = client.getPluginUsage();
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("coordinator", "1.4.0"), Lists.newArrayList(
                        new Project("coordinator1")
                )),
                new PluginProjects(new Plugin("visual-basic-6", "1.4"), Lists.newArrayList(
                        new Project("freestyle1")
                ))
        ));
        assertEquals(pluginNamesExtractor.apply(expected), pluginNamesExtractor.apply(actual));
        assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual));
    }

    private <T> void attempt(String message, Supplier<T> action, Predicate<T> success) {
        ExponentialBackoffStrategy.attempt(message, action, success, maxTimeBackoff.toMillis());
    }
}
