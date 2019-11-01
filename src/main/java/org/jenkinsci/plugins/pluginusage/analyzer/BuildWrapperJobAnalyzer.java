package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

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

	@Override
	protected void doJobAnalyze(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{
		super.doJobAnalyze(null, mapJobsPerPlugin);
		if (item instanceof BuildableItemWithBuildWrappers)
		{
			DescribableList<BuildWrapper,Descriptor<BuildWrapper>> buildWrappers = ((BuildableItemWithBuildWrappers)item).getBuildWrappersList();
			Map<Descriptor<BuildWrapper>,BuildWrapper> map = buildWrappers.toMap();
			for (Map.Entry<Descriptor<BuildWrapper>,BuildWrapper> entry : map.entrySet())
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
}
