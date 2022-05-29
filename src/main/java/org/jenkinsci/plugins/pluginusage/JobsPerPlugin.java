package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.Job;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExportedBean
public class JobsPerPlugin {
	
	private PluginWrapper plugin;
	private Map<String, Job> jobMap = new HashMap<String, Job>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(Job project) {
		this.jobMap.put(project.getFullDisplayName(), project);
	}
	
	@Exported
	public List<Job> getProjects() {
		ArrayList<Job> projects = new ArrayList<Job>();
		projects.addAll(jobMap.values());
		projects.sort(Comparator.comparing(Job::getName));
		return projects;
	}
	
	public String getPluginName() {
		return plugin.getLongName();
	}

	public String getPluginVersion() {
		return plugin.getVersion();
	}
	
	public int getNumberOfJobs() {
		return jobMap.size();
	}
	
	@Exported
	public PluginWrapper getPlugin()
	{
		return plugin;
	}

	public boolean hasDependants(){
		return plugin.hasDependants();
	}

}
