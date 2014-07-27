package org.jenkinsci.plugins.pluginusage;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class PluginUsageView implements RootAction{
	
	public String getDisplayName() {
		return "Plugin Usage";
	}

	public String getIconFileName() {
		return "plugin.png";
	}

	public String getUrlName() {
		return "pluginusage";
	}
	
	public PluginUsageModel getData() {
		PluginUsageModel pluginUsageModel = new PluginUsageModel();
		return pluginUsageModel;
	}

}
