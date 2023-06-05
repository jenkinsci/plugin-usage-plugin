package org.jenkinsci.plugins.pluginusage;

import static org.junit.Assert.*;

import org.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

import java.net.HttpURLConnection;

public class JobAnalyzerTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test
    public void first() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        PluginUsageView pluginUsageView = new PluginUsageView();
        PluginUsageModel data = pluginUsageView.getData();
        assertEquals(0,data.getNumberOfJobs());
    }

    @Test
    public void onlyReadOrAdminCanReadPluginUsage() throws Exception {
        final String PLUGIN_VIEW = "plugin-view-user";
        final String ANONYMOUS = "anonymous";
        final String ADMIN = "admin";

        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                // Plugin view access
                .grant(PluginUsageView.PLUGIN_VIEW).everywhere().to(PLUGIN_VIEW)
                // Read access
                .grant(Jenkins.READ).everywhere().to(PLUGIN_VIEW)
                // Admin access
                .grant(Jenkins.ADMINISTER).everywhere().to(ADMIN)
        );

        JenkinsRule.WebClient wc = j.createWebClient()
                .withThrowExceptionOnFailingStatusCode(false);

        { // user can access it
            wc.login(PLUGIN_VIEW);
            HtmlPage page = wc.goTo("pluginusage/");
            assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        }

        { // admin can access it
            wc.login(ADMIN);
            HtmlPage page = wc.goTo("pluginusage/");
            assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        }

        { // anonymous cannot see it
            wc.login(ANONYMOUS);
            HtmlPage page = wc.goTo("pluginusage/");
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, page.getWebResponse().getStatusCode());
        }
    }
}
