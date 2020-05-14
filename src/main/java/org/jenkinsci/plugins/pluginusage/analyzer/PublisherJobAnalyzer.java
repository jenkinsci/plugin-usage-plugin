package org.jenkinsci.plugins.pluginusage.analyzer;

import java.util.List;
import java.util.Map;

import hudson.DescriptorExtensionList;
import hudson.PluginWrapper;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.promoted_builds.PromotedProjectAction;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.tasks.BuildStep;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
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
	protected void doJobAnalyze(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin)
	{	
		super.doJobAnalyze(null, mapJobsPerPlugin);
		if(item instanceof AbstractProject){
			DescribableList<Publisher,Descriptor<Publisher>> publisherList = ((AbstractProject)item).getPublishersList();
			Map<Descriptor<Publisher>, Publisher> map = publisherList.toMap();
			for (Map.Entry<Descriptor<Publisher>, Publisher> entry : map.entrySet())
			{
				PluginWrapper usedPlugin = getUsedPlugin(entry.getKey().clazz);
				addItem(item, mapJobsPerPlugin, usedPlugin);
			}
			processPromotedBuilds(item, mapJobsPerPlugin);
		}
	}

	private void processPromotedBuilds(Job item, Map<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin) {
		if (Jenkins.get().getPlugin("promoted-builds") != null){
			PromotedProjectAction action = item.getAction(PromotedProjectAction.class);
			if (action != null){
				List<PromotionProcess> processes = action.getProcesses();
				for (PromotionProcess process: processes) {
					List<BuildStep> buildSteps = process.getBuildSteps();
					for (BuildStep buildStep: buildSteps) {
						if (buildStep instanceof Publisher){
							Publisher innerPublisher = (Publisher) buildStep;
							PluginWrapper usedPlugin = getUsedPlugin(innerPublisher.getDescriptor().clazz);
							addItem(item, mapJobsPerPlugin, usedPlugin);
						}
					}
				}
			}
		}
	}

}
