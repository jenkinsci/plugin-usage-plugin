package org.jenkinsci.plugins.pluginusage;

import static org.junit.Assert.*;

import java.util.List;

import jenkins.model.Jenkins;
import hudson.PluginWrapper;
import hudson.model.FreeStyleProject;
import hudson.scm.SubversionSCM;
import hudson.tasks.Builder;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class JobAnalyzerTest {

	@Rule public JenkinsRule j = new JenkinsRule();
	
	@Test 
	public void first() throws Exception {
	    FreeStyleProject project = j.createFreeStyleProject();
	    PluginUsageView pluginUsageView = new PluginUsageView();
	    PluginUsageModel data = pluginUsageView.getData();
	    assertEquals(1,data.getNumberOfJobs());    
	  }

}
