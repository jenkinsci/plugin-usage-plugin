package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JobsPerPlugin {
	
	private PluginWrapper plugin;
	private HashMap<String, AbstractProject> jobMap = new HashMap<String, AbstractProject>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(AbstractProject project) {
		this.jobMap.put(project.getDisplayName(), project);
	}
	
	public List<AbstractProject> getProjects() {
		ArrayList<AbstractProject> projects = new ArrayList<AbstractProject>();
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
