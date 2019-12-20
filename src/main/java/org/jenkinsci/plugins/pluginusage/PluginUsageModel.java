package org.jenkinsci.plugins.pluginusage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import hudson.PluginWrapper;
import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;

public class PluginUsageModel {

	private JobCollector JC;

	public PluginUsageModel(){
		JC = new JobCollector();
	}
	
	public List<JobsPerPlugin> getJobsPerPlugin() {
		ArrayList<JobsPerPlugin> list = new ArrayList<JobsPerPlugin>();
		Map<PluginWrapper, JobsPerPlugin> jobs = JC.getJobsPerPlugin();
		JC.removeDependencies(jobs);
		list.addAll(jobs.values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}
	
	public int getNumberOfJobs()
	{
		return JC.getNumberOfJobs();
	}
	
	public List<PluginWrapper> getOtherPlugins(){
		List<PluginWrapper> plugins = JC.getOtherPlugins();
		JC.removeDependencies(plugins);
		plugins.sort(Comparator.comparing(PluginWrapper::getLongName));
		return plugins;
	}
	
}
