package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

import hudson.PluginWrapper;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;


public abstract class JobAnalyzer{

	protected List<PluginWrapper> plugins = new ArrayList<>();

	protected PluginWrapper getUsedPlugin(Class clazz) {
		return Jenkins.get().getPluginManager().whichPlugin(clazz);
	}

	protected void doJobAnalyze(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{
		for(PluginWrapper plugin: plugins)
		{
			if(plugin!=null)
			{
				JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(plugin);
				if(jobsPerPlugin==null)
				{
					JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(plugin);
		    		mapJobsPerPlugin.put(plugin, jobsPerPlugin2);
				}
			}		
		}
	}

	public List<PluginWrapper> getPlugins() {
		return plugins;
	}

}
