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

	private final JobCollector jobCollector = new JobCollector();

	@Exported
	public List<JobsPerPlugin> getJobsPerPlugin() {
		List<JobsPerPlugin> list = new ArrayList<>(jobCollector.getJobsPerPlugin().values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}
	
	public int getNumberOfJobs()
	{
		return jobCollector.getNumberOfJobs();
	}
	
	public List<PluginWrapper> getOtherPlugins(){
		List<PluginWrapper> plugins = jobCollector.getOtherPlugins();
		plugins.sort(Comparator.comparing(PluginWrapper::getLongName));
		return plugins;
	}
	
}
