package org.jenkinsci.plugins.pluginusage;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.jenkinsci.plugins.pluginusage.api.Plugin;
import org.jenkinsci.plugins.pluginusage.api.PluginProjects;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;
import org.jenkinsci.plugins.pluginusage.api.Project;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@WithJenkins
class PluginUsageViewApiTest {
    private final Gson gson = new Gson();

    Function<PluginUsage, Set<String>> otherPluginsPluginNamesExtractor = p -> p.getOtherPlugins()
            .stream()
            .map(Plugin::getShortName)
            .collect(Collectors.toSet());
    Function<PluginUsage, Set<String>> jobsPerPluginPluginNamesExtractor = p -> p.getJobsPerPlugin()
            .stream()
            .map(PluginProjects::getPlugin)
            .map(Plugin::getShortName)
            .collect(Collectors.toSet());

    Function<PluginUsage, Map<String, List<Project>>> projectsExtractor = p -> p.getJobsPerPlugin()
            .stream()
            .collect(Collectors.toMap(x -> x.getPlugin().getShortName(), PluginProjects::getProjects));

    @Test
    @LocalData("plugin-usage.xml")
    void shouldRenderJson(JenkinsRule j) throws Exception {
        // given two jobs are using the ant builder
        URL configUsingAnt = Resources.getResource("with-ant.xml");
        File config = new File(configUsingAnt.toURI());
        List<String> jobsUsingAnt = Lists.newArrayList("a", "b");
        for (String job : jobsUsingAnt) {
            try (FileInputStream is = new FileInputStream(config)) {
                j.jenkins.createProjectFromXML(job, is);
            }
        }
        // and one job is not
        j.createFreeStyleProject("c");
        PluginUsage expected = new PluginUsage(Lists.newArrayList(
                new PluginProjects(new Plugin("ant", "511.v0a_a_1a_334f41b_"), Lists.newArrayList(
                        new Project("a"),
                        new Project("b")
                )),
                new PluginProjects(new Plugin("junit", "1296.vb_f538b_c88630"), Lists.newArrayList()),
                new PluginProjects(new Plugin("mailer", "472.vf7c289a_4b_420"), Lists.newArrayList())
                ), Lists.newArrayList(
                        new Plugin("asm-api", "9.7-33.v4d23ef79fcc8"),
                        new Plugin("apache-httpcomponents-client-4-api", "4.5.14-208.v438351942757"),
                        new Plugin("bootstrap5-api", "5.3.3-1"),
                        new Plugin("branch-api", "2.1178.v969d9eb_c728e"),
                        new Plugin("caffeine-api", "3.1.8-133.v17b_1ff2e0599"),
                        new Plugin("checks-api", "2.2.1"),
                        new Plugin("credentials-binding", "681.vf91669a_32e45"),
                        new Plugin("credentials", "1371.vfee6b_095f0a_3"),
                        new Plugin("display-url-api", "2.204.vf6fddd8a_8b_e9"),
                        new Plugin("durable-task", "568.v8fb_5c57e8417"),
                        new Plugin("echarts-api", "5.5.1-1"),
                        new Plugin("eddsa-api", "0.3.0-4.v84c6f0f4969e"),
                        new Plugin("cloudbees-folder", "6.901.vb_4c7a_da_75da_3"),
                        new Plugin("font-awesome-api", "6.6.0-1"),
                        new Plugin("gson-api", "2.11.0-41.v019fcf6125dc"),
                        new Plugin("instance-identity", "185.v303dc7c645f9"),
                        new Plugin("ionicons-api", "74.v93d5eb_813d5f"),
                        new Plugin("jaxb", "2.3.9-1"),
                        new Plugin("jquery3-api", "3.7.1-2"),
                        new Plugin("json-api", "20240303-41.v94e11e6de726"),
                        new Plugin("json-path-api", "2.9.0-58.v62e3e85b_a_655"),
                        new Plugin("jsch", "0.2.16-86.v42e010d9484b_"),
                        new Plugin("jackson2-api", "2.17.0-379.v02de8ec9f64c"),
                        new Plugin("jakarta-activation-api", "2.1.3-1"),
                        new Plugin("jakarta-mail-api", "2.1.3-1"),
                        new Plugin("javax-activation-api", "1.2.0-7"),
                        new Plugin("javadoc", "280.v050b_5c849f69"),
                        new Plugin("joda-time-api", "2.12.7-29.v5a_b_e3a_82269a_"),
                        new Plugin("jsoup", "1.20.1-46.ve5f1416988c2"),
                        new Plugin("matrix-project", "832.va_66e270d2946"),
                        new Plugin("maven-plugin", "3.23"),
                        new Plugin("workflow-api", "1336.vee415d95c521"),
                        new Plugin("workflow-basic-steps", "1058.vcb_fc1e3a_21a_9"),
                        new Plugin("pipeline-model-definition", "2.2214.vb_b_34b_2ea_9b_83"),
                        new Plugin("pipeline-model-extensions", "2.2214.vb_b_34b_2ea_9b_83"),
                        new Plugin("workflow-cps", "3953.v19f11da_8d2fa_"),
                        new Plugin("pipeline-groovy-lib", "730.ve57b_34648c63"),
                        new Plugin("pipeline-input-step", "495.ve9c153f6067b_"),
                        new Plugin("workflow-job", "1400.v7fd111b_ec82f"),
                        new Plugin("pipeline-model-api", "2.2214.vb_b_34b_2ea_9b_83"),
                        new Plugin("workflow-multibranch", "773.vc4fe1378f1d5"),
                        new Plugin("workflow-durable-task-step", "1371.vb_7cec8f3b_95e"),
                        new Plugin("workflow-scm-step", "427.v4ca_6512e7df1"),
                        new Plugin("pipeline-stage-step", "312.v8cd10304c27a_"),
                        new Plugin("pipeline-stage-tags-metadata", "2.2214.vb_b_34b_2ea_9b_83"),
                        new Plugin("workflow-step-api", "678.v3ee58b_469476"),
                        new Plugin("workflow-support", "920.v59f71ce16f04"),
                        new Plugin("plain-credentials", "183.va_de8f1dd5a_2b_"),
                        new Plugin("plugin-usage-plugin", "4.7-SNAPSHOT"),
                        new Plugin("plugin-util-api", "4.1.0"),
                        new Plugin("run-condition", "1.7"),
                        new Plugin("scm-api", "696.v778d637b_a_762"),
                        new Plugin("ssh-credentials", "343.v884f71d78167"),
                        new Plugin("script-security", "1358.vb_26663c13537"),
                        new Plugin("snakeyaml-api", "2.3-123.v13484c65210a_"),
                        new Plugin("structs", "338.v848422169819"),
                        new Plugin("token-macro", "400.v35420b_922dcb_"),
                        new Plugin("trilead-api", "2.147.vb_73cc728a_32e"),
                        new Plugin("variant", "60.v7290fc0eb_b_cd"),
                        new Plugin("bouncycastle-api", "2.30.1.78.1-248.ve27176eb_46cb_"),
                        new Plugin("commons-lang3-api", "3.17.0-84.vb_b_938040b_078"),
                        new Plugin("commons-text-api", "1.12.0-129.v99a_50df237f7"),
                        new Plugin("conditional-buildstep", "1.2"),
                        new Plugin("promoted-builds", "965.vcda_c6a_e0998f"),
                        new Plugin("oss-symbols-api", "296.v4981240eeb_1a_")
                    )
        );

        // when the api is fetched
        String body = j.getJSON("pluginusage/api/json?depth=2").getContentAsString();
        PluginUsage actual = gson.fromJson(body, PluginUsage.class);

        // then
        assertAll(
                () -> assertEquals(otherPluginsPluginNamesExtractor.apply(expected), otherPluginsPluginNamesExtractor.apply(actual)),
                () -> assertEquals(jobsPerPluginPluginNamesExtractor.apply(expected), jobsPerPluginPluginNamesExtractor.apply(actual)),
                () -> assertEquals(projectsExtractor.apply(expected), projectsExtractor.apply(actual))
        );
    }
}
