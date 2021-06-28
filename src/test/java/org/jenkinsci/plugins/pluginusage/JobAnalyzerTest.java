package org.jenkinsci.plugins.pluginusage;

import static org.junit.Assert.*;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
	    assertEquals(1,data.getNumberOfJobs());
	}

    @Test
    public void onlyReadCanReadPluginUsage() throws Exception {
        final String READONLY = "readonly";
        final String ANONYMOUS = "anonymous";

        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy()
                // Read access
                .grant(Jenkins.READ).everywhere().to(READONLY)
        );

        JenkinsRule.WebClient wc = j.createWebClient()
                .withThrowExceptionOnFailingStatusCode(false);

        { // user can access it
            wc.login(READONLY);
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
