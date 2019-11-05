package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class PublisherJobAnalyzer extends JobAnalyzer{
	
	public PublisherJobAnalyzer() {
		DescriptorExtensionList<Publisher, Descriptor<Publisher>> all = Publisher.all();
		for(Descriptor<Publisher> b: all)
		{
			PluginWrapper usedPlugin = getUsedPlugin(b.clazz);
			plugins.add(usedPlugin);
		}
	}

	@Override
	protected void doJobAnalyze(AbstractProject item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{	
		super.doJobAnalyze(null, mapJobsPerPlugin);
		if(item != null){
			DescribableList<Publisher,Descriptor<Publisher>> publisherList = item.getPublishersList();
			Map<Descriptor<Publisher>, Publisher> map = publisherList.toMap();
			for (Map.Entry<Descriptor<Publisher>, Publisher> entry : map.entrySet())
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
