package org.jenkinsci.plugins.pluginusage.analyzer;

import hudson.PluginWrapper;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.pluginusage.JobsPerPlugin;

public class JobCollector {
	
	private ArrayList<JobAnalyzer> analysers = new ArrayList<JobAnalyzer>();
	
	public JobCollector() {
		analysers.add(new BuilderJobAnalyzer());
		analysers.add(new BuildWrapperJobAnalyzer());
		analysers.add(new PropertiesJobAnalyzer());
		analysers.add(new PublisherJobAnalyzer());
		analysers.add(new SCMJobAnalyzer());
		analysers.add(new TriggerJobAnalyzer());
	}

	public HashMap<PluginWrapper, JobsPerPlugin> getJobsPerPlugin()
	{
		HashMap<PluginWrapper, JobsPerPlugin> mapJobsPerPlugin = new HashMap<PluginWrapper, JobsPerPlugin>();
		Jenkins jenkins = Jenkins.getInstance();
		List<AbstractProject> allItems = jenkins.getAllItems(AbstractProject.class);
		
		for(AbstractProject item: allItems)
		{
			for(JobAnalyzer analyser: analysers)
			{
				analyser.doJobAnalyze(item, mapJobsPerPlugin);
			}
		}

		List<PluginWrapper> allPlugins = jenkins.getPluginManager().getPlugins();
		for(PluginWrapper plugin: allPlugins)
		{
			if (mapJobsPerPlugin.get(plugin) == null)
			{
				mapJobsPerPlugin.put(plugin, new JobsPerPlugin(plugin));
			}
		}

		return mapJobsPerPlugin;
	}
	
	public int getNumberOfJobs() {
		List<AbstractProject> allItems = Jenkins.getInstance().getAllItems(AbstractProject.class);
		return allItems.size();	
	}
	
	
}
