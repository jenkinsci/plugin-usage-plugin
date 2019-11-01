package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobsPerPlugin {
	
	private PluginWrapper plugin;
	private Map<String, AbstractProject> jobMap = new HashMap<String, AbstractProject>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(AbstractProject project) {
		this.jobMap.put(project.getFullDisplayName(), project);
	}
	
	public List<AbstractProject> getProjects() {
		ArrayList<AbstractProject> projects = new ArrayList<AbstractProject>();
		projects.addAll(jobMap.values());
		projects.sort(Comparator.comparing(AbstractProject::getName));
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
