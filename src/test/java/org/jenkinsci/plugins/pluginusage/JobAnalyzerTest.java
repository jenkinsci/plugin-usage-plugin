package org.jenkinsci.plugins.pluginusage;

import static org.junit.Assert.*;

import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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
