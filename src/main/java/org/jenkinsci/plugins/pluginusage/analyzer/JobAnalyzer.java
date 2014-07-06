package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.ArrayList;
import java.util.HashMap;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

import hudson.PluginWrapper;
import hudson.model.Project;
import jenkins.model.Jenkins;


public abstract class JobAnalyzer{
	
	protected ArrayList<PluginWrapper> plugins = new ArrayList<PluginWrapper>();

	protected PluginWrapper getUsedPlugin(Class clazz) {
		Jenkins instance = Jenkins.getInstance();
		return instance.getPluginManager().whichPlugin(clazz);
	}

	protected void doJobAnalyze(Project item, HashMap<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
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

}
