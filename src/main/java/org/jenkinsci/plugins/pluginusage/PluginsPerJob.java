package org.jenkinsci.plugins.pluginusage;


import hudson.PluginWrapper;
import hudson.model.UpdateSite.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginsPerJob {
	
	private String jobName;
	private ArrayList<PluginWrapper> plugins;
	
	public PluginsPerJob(String jobName, ArrayList<PluginWrapper> plugins) {
		this.jobName = jobName;
		this.plugins = plugins;
	}
	
	public String getJobName() {
		return jobName;
	}
	
	public List<PluginWrapper> getPlugins() {
		return plugins;
	}
	
	public List<String> getPluginNames() {
		ArrayList<String> nameList = new ArrayList<String>();
		for(PluginWrapper plugin: plugins)
		{
			nameList.add(plugin.getLongName());
		}
		
		return nameList;
	}

}
