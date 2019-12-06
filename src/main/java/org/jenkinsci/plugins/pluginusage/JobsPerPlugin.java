package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.Job;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsPerPlugin {
	
	private PluginWrapper plugin;
	private Map<String, Job> jobMap = new HashMap<String, Job>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(Job project) {
		this.jobMap.put(project.getFullDisplayName(), project);
	}
	
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
	
	public PluginWrapper getPlugin()
	{
		return plugin;
	}

}
