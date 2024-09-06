package org.jenkinsci.plugins.pluginusage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hudson.PluginWrapper;
import hudson.Util;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class PluginUsageModel {

	private transient PersistedModel persistedModel;
	@Exported
	public List<JobsPerPlugin> getJobsPerPlugin() {
 		List<JobsPerPlugin> list = new ArrayList<>(getPersistedModel().getJobsPerPlugin().values());
		list.sort(Comparator.comparing(JobsPerPlugin::getPluginName));
		return list;
	}

	public int getNumberOfJobs()
	{
		return getPersistedModel().getJobsPerPlugin()
				.values()
				.stream()
				.flatMap(jobsPerPlugin -> jobsPerPlugin.getProjects().stream())
				.collect(Collectors.toSet())
				.size();
	}

	@Exported
	public List<PluginWrapper> getOtherPlugins(){
		List<PluginWrapper> allPlugins = Jenkins.get().getPluginManager().getPlugins();
		List<PluginWrapper> others = new ArrayList<>(allPlugins);

		others.removeAll(getPersistedModel().getJobsPerPlugin().keySet());

		others.sort(Comparator.comparing(PluginWrapper::getDisplayName));
		return others;
	}

	public Instant getTimestamp() {
		return getPersistedModel().getTimestamp();
	}

	public String getTimestampStr() {
		if (getTimestamp() != null){
			return Util.getTimeSpanString(System.currentTimeMillis() - getTimestamp().toEpochMilli());
		}
		return null;
	}


	private Map<PluginWrapper, JobsPerPlugin> getMapJobsPerPlugin(){
		return getPersistedModel().getJobsPerPlugin();
	}

	private PersistedModel getPersistedModel(){
		if (persistedModel == null){
			persistedModel = new PersistedModel();
			persistedModel.load();
		}
		return persistedModel;
	}
}
