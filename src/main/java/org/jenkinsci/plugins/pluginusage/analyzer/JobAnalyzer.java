package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class JobAnalyzer{

	protected List<PluginWrapper> plugins = new ArrayList<>();

	protected PluginWrapper getUsedPlugin(Class clazz) {
		return Jenkins.get().getPluginManager().whichPlugin(clazz);
	}

	protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
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
