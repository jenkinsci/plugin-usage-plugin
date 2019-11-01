package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class TriggerJobAnalyzer extends JobAnalyzer{
	
	public TriggerJobAnalyzer() {
		DescriptorExtensionList<Trigger<?>, TriggerDescriptor> all = Trigger.all();
		for(TriggerDescriptor b: all)
		{
			PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
			plugins.add(usedPlugin);
		}
	}

	@Override
	protected void doJobAnalyze(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{		
		super.doJobAnalyze(null, mapJobsPerPlugin);
		Map<TriggerDescriptor,Trigger> triggers = item.getTriggers();
		for (Map.Entry<TriggerDescriptor,Trigger> entry : triggers.entrySet())
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
