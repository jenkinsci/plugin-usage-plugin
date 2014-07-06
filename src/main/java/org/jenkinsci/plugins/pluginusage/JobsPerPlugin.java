package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JobsPerPlugin {
	
	private PluginWrapper plugin;
	private HashMap<String, Project> jobMap = new HashMap<String, Project>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(Project project) {
		this.jobMap.put(project.getDisplayName(), project);
	}
	
	public List<Project> getProjects() {
		ArrayList<Project> projects = new ArrayList<Project>();
		projects.addAll(jobMap.values());
		
		return projects;
	}
	
	public String getPluginName() {
		return plugin.getLongName();
	}
	
	public int getNumberOfJobs() {
		return jobMap.size();
	}
	
	public PluginWrapper getPlugin()
	{
		return plugin;
	}

}
