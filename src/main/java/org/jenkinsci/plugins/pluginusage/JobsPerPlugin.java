package org.jenkinsci.plugins.pluginusage;

import hudson.PluginWrapper;
import hudson.model.Item;
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
	private Map<String, Item> jobMap = new HashMap<>();
	
	
	public JobsPerPlugin(PluginWrapper plugin) {
		this.plugin = plugin;
	}
	
	public void addProject(Item project) {
		this.jobMap.put(project.getFullDisplayName(), project);
	}
	
	@Exported
	public List<Item> getProjects() {
		ArrayList<Item> projects = new ArrayList<>(jobMap.values());
		projects.sort(Comparator.comparing(Item::getName));
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
