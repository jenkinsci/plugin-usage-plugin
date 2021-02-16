package org.jenkinsci.plugins.pluginusage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hudson.PluginWrapper;
import org.jenkinsci.plugins.pluginusage.analyzer.JobCollector;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class PluginUsageModel {
	
	@Exported
	public List<JobsPerPlugin> getJobsPerPlugin() {
		ArrayList<JobsPerPlugin> list = new ArrayList<JobsPerPlugin>();
		list.addAll(new JobCollector().getJobsPerPlugin().values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}
	
	public int getNumberOfJobs()
	{
		return new JobCollector().getNumberOfJobs();
	}
	
	public List<PluginWrapper> getOtherPlugins(){
		List<PluginWrapper> plugins = new JobCollector().getOtherPlugins();
		plugins.sort(Comparator.comparing(PluginWrapper::getLongName));
		return plugins;
	}
	
}
