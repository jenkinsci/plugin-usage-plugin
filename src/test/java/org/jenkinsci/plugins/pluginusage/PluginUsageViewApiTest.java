package org.jenkinsci.plugins.pluginusage;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.jenkinsci.plugins.pluginusage.api.Plugin;
import org.jenkinsci.plugins.pluginusage.api.PluginProjects;
import org.jenkinsci.plugins.pluginusage.api.PluginUsage;
import org.jenkinsci.plugins.pluginusage.api.Project;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PluginUsageViewApiTest {
    private final Gson gson = new Gson();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @LocalData("plugin-usage.xml")
    @WithPlugin({"structs-1.17.hpi", "ant-1.9.hpi"})
    public void shouldRenderJson() throws Exception {
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
                new PluginProjects(new Plugin("ant", "1.9"), Lists.newArrayList(
                        new Project("a"),
                        new Project("b")
                )),
                new PluginProjects(new Plugin("junit", "1265.v65b_14fa_f12f0"), Lists.newArrayList()),
                new PluginProjects(new Plugin("mailer", "472.vf7c289a_4b_420"), Lists.newArrayList())
                ), Lists.newArrayList(
                        new Plugin("asm-api", "9.6-3.v2e1fa_b_338cd7"),
                        new Plugin("apache-httpcomponents-client-4-api", "4.5.14-208.v438351942757"),
                        new Plugin("bootstrap5-api", "5.3.3-1"),
                        new Plugin("caffeine-api", "3.1.8-133.v17b_1ff2e0599"),
                        new Plugin("checks-api", "2.0.2"),
                        new Plugin("command-launcher", "107.v773860566e2e"),
                        new Plugin("display-url-api", "2.200.vb_9327d658781"),
                        new Plugin("echarts-api", "5.5.0-1"),
                        new Plugin("font-awesome-api", "6.5.1-3"),
                        new Plugin("gson-api", "2.10.1-15.v0d99f670e0a_7"),
                        new Plugin("instance-identity", "185.v303dc7c645f9"),
                        new Plugin("ionicons-api", "70.v2959a_b_74e3cf"),
                        new Plugin("jaxb", "2.3.9-1"),
                        new Plugin("jquery3-api", "3.7.1-2"),
                        new Plugin("json-api", "20240303-41.v94e11e6de726"),
                        new Plugin("jackson2-api", "2.17.0-379.v02de8ec9f64c"),
                        new Plugin("jakarta-activation-api", "2.1.3-1"),
                        new Plugin("jakarta-mail-api", "2.1.3-1"),
                        new Plugin("javax-activation-api", "1.2.0-6"),
                        new Plugin("javax-mail-api", "1.6.2-9"),
                        new Plugin("matrix-auth", "3.2.2"),
                        new Plugin("mina-sshd-api-common", "2.12.1-101.v85b_e08b_780dd"),
                        new Plugin("mina-sshd-api-core", "2.12.1-101.v85b_e08b_780dd"),
                        new Plugin("antisamy-markup-formatter", "162.v0e6ec0fcfcf6"),
                        new Plugin("jdk-tool", "73.vddf737284550"),
                        new Plugin("workflow-api", "1291.v51fd2a_625da_7"),
                        new Plugin("workflow-step-api", "657.v03b_e8115821b_"),
                        new Plugin("workflow-support", "896.v175a_a_9c5b_78f"),
                        new Plugin("plugin-util-api", "4.1.0"),
                        new Plugin("scm-api", "689.v237b_6d3a_ef7f"),
                        new Plugin("sshd", "3.322.v159e91f6a_550"),
                        new Plugin("script-security", "1326.vdb_c154de8669"),
                        new Plugin("snakeyaml-api", "2.2-111.vc6598e30cc65"),
                        new Plugin("structs", "337.v1b_04ea_4df7c8"),
                        new Plugin("structs", "1.17"),
                        new Plugin("trilead-api", "2.142.v748523a_76693"),
                        new Plugin("bouncycastle-api", "2.30.1.77-225.v26ea_c9455fd9"),
                        new Plugin("commons-lang3-api", "3.13.0-62.v7d18e55f51e2"),
                        new Plugin("commons-text-api", "1.11.0-95.v22a_d30ee5d36")
                    )
        );

        // when the api is fetched
        String body = j.getJSON("pluginusage/api/json?depth=2").getContentAsString();
        PluginUsage actual = gson.fromJson(body, PluginUsage.class);

        // then
        assertEquals(expected, actual);
    }
}
