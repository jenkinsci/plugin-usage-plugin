package org.jenkinsci.plugins.pluginusage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import hudson.PluginWrapper;
import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;

public class PluginUsageModel {

	private JobCollector JC;
	private Map<PluginWrapper, JobsPerPlugin> pluginsWithJobs;
	private Map<PluginWrapper, JobsPerPlugin> pluginsWithJobsDependencies;
	private List<PluginWrapper> otherPlugins;
	private List<PluginWrapper> otherPluginsDependencies;

	public PluginUsageModel(){
		JC = new JobCollector();
		pluginsWithJobs = JC.getJobsPerPlugin();
		pluginsWithJobsDependencies = JC.splitByDependencies(pluginsWithJobs);
		otherPlugins = JC.getOtherPlugins();
		otherPluginsDependencies = JC.removeDependencies(otherPlugins);
	}
	
	public List<JobsPerPlugin> getJobsPerPlugin() {
		ArrayList<JobsPerPlugin> list = new ArrayList<JobsPerPlugin>();
		//get a copy of pluginsWithJobs and then sort it.
		list.addAll(pluginsWithJobs.values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}

	public List<JobsPerPlugin> getDependenciesPerPlugin() {
		ArrayList<JobsPerPlugin> list = new ArrayList<JobsPerPlugin>();
		//get a copy of pluginsWithJobsDependencies and then sort it.
		list.addAll(pluginsWithJobsDependencies.values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}
	
	public int getNumberOfJobs()
	{
		return JC.getNumberOfJobs();
	}
	
	public List<PluginWrapper> getOtherPlugins(){
		List<PluginWrapper> plugins = new ArrayList<PluginWrapper>();
		//get a copy of otherPlugins and sort it
		plugins.addAll(otherPlugins);
		plugins.sort(Comparator.comparing(PluginWrapper::getLongName));
		return plugins;
	}

    public List<PluginWrapper> getOtherDependencies(){
		List<PluginWrapper> plugins = new ArrayList<PluginWrapper>();
		//get a copy of otherPluginsDependencies and sort it
		plugins.addAll(otherPluginsDependencies);
		plugins.sort(Comparator.comparing(PluginWrapper::getLongName));
		return plugins;
	}
	
}
