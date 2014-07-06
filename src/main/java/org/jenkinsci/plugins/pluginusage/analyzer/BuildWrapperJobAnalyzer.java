package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class BuildWrapperJobAnalyzer extends JobAnalyzer{
	
	public BuildWrapperJobAnalyzer() {
		DescriptorExtensionList<BuildWrapper,Descriptor<BuildWrapper>> all = BuildWrapper.all();
		for(Descriptor<BuildWrapper> b: all)
		{
			PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
			plugins.add(usedPlugin);
		}
	}
	
	protected void doJobAnalyze(Project item, HashMap<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{
		super.doJobAnalyze(null, mapJobsPerPlugin);
		Map<Descriptor<BuildWrapper>,BuildWrapper> buildWrappers = item.getBuildWrappers();
	    for (Map.Entry<Descriptor<BuildWrapper>,BuildWrapper>  entry : buildWrappers.entrySet())
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
