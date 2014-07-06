package org.jenkinsci.plugins.pluginusage;

import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;

public class PluginUsageModel {
	
	public List<JobsPerPlugin> getJobsPerPlugin() {
		ArrayList<JobsPerPlugin> list = new ArrayList<JobsPerPlugin>();
		list.addAll(new JobCollector().getJobsPerPlugin().values());
		
		return list;
	}
	
	public int getNumberOfJobs()
	{
		return new JobCollector().getNumberOfJobs();
	}
	
	
	
}
