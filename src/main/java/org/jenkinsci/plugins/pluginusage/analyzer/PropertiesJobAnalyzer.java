package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractProject;

import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;


public class PropertiesJobAnalyzer extends JobAnalyzer{
	

	protected void doJobAnalyze(AbstractProject item, HashMap<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{	
		Map<JobPropertyDescriptor,JobProperty> properties = item.getProperties();
		for (Map.Entry<JobPropertyDescriptor,JobProperty> entry : properties.entrySet())
		{
		    PluginWrapper usedPlugin = getUsedPlugin(entry.getKey().clazz);
		    if(usedPlugin!=null)
		    {
		    	JobsPerPlugin jobsPerPlugin = mapJobsPerPlugin.get(usedPlugin);
		    	if(jobsPerPlugin!=null)
		    	{
		    		jobsPerPlugin.addProject(item);
		    	}
		    	else
		    	{
		    		JobsPerPlugin jobsPerPlugin2 = new JobsPerPlugin(usedPlugin);
		    		jobsPerPlugin2.addProject(item);
		    		mapJobsPerPlugin.put(usedPlugin, jobsPerPlugin2);
		    	}
		    }
		}
	}
}
