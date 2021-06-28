package org.jenkinsci.plugins.pluginusage;

import hudson.Extension;
import hudson.model.Api;
import hudson.model.RootAction;
import jenkins.model.Jenkins;

@Extension
public class PluginUsageView implements RootAction{

	public String getDisplayName() {
		return "Plugin Usage";
	}

	public String getIconFileName() {
		return (Jenkins.get().hasPermission(Jenkins.READ)) ? "plugin.png" : null;
	}

	public String getUrlName() {
		return (Jenkins.get().hasPermission(Jenkins.READ)) ? "pluginusage" : null;
	}

	public PluginUsageModel getData() {
		if (Jenkins.get().hasPermission(Jenkins.READ)) {
			PluginUsageModel pluginUsageModel = new PluginUsageModel();
			return pluginUsageModel;
		}
		return null;
	}

	public Api getApi() {
		if (Jenkins.get().hasPermission(Jenkins.READ)) {
			return new Api(getData());
		}
		return null;
	}
}
