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
                new PluginProjects(new Plugin("junit", "1160.vf1f01a_a_ea_b_7f"), Lists.newArrayList()),
                new PluginProjects(new Plugin("mailer", "408.vd726a_1130320"), Lists.newArrayList())
        ));

        // when the api is fetched
        String body = j.getJSON("pluginusage/api/json?depth=2").getContentAsString();
        PluginUsage actual = gson.fromJson(body, PluginUsage.class);

        // then
        assertEquals(expected, actual);
    }
}
